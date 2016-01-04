/*
 * SonarQube, open source software quality management tool.
 * Copyright (C) 2008-2014 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * SonarQube is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * SonarQube is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.server.issue;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import org.apache.commons.lang.StringUtils;
import org.sonar.api.ServerComponent;
import org.sonar.api.issue.ActionPlan;
import org.sonar.api.issue.Issue;
import org.sonar.api.issue.IssueQuery;
import org.sonar.api.issue.IssueQueryResult;
import org.sonar.api.issue.internal.DefaultIssue;
import org.sonar.api.issue.internal.IssueChangeContext;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rule.Severity;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.user.User;
import org.sonar.api.user.UserFinder;
import org.sonar.api.web.UserRole;
import org.sonar.core.issue.DefaultIssueBuilder;
import org.sonar.core.issue.IssueNotifications;
import org.sonar.core.issue.IssueUpdater;
import org.sonar.core.issue.db.IssueDao;
import org.sonar.core.issue.db.IssueStorage;
import org.sonar.core.issue.workflow.IssueWorkflow;
import org.sonar.core.issue.workflow.Transition;
import org.sonar.core.persistence.DbSession;
import org.sonar.core.preview.PreviewCache;
import org.sonar.core.resource.ResourceDao;
import org.sonar.core.resource.ResourceDto;
import org.sonar.core.resource.ResourceQuery;
import org.sonar.core.rule.RuleDto;
import org.sonar.core.user.AuthorizationDao;
import org.sonar.server.issue.actionplan.ActionPlanService;
import org.sonar.server.user.UserSession;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @since 3.6
 */
public class IssueService implements ServerComponent {

  private final DefaultIssueFinder finder;
  private final IssueWorkflow workflow;
  private final IssueUpdater issueUpdater;
  private final IssueStorage issueStorage;
  private final IssueNotifications issueNotifications;
  private final ActionPlanService actionPlanService;
  private final RuleFinder ruleFinder;
  private final ResourceDao resourceDao;
  private final IssueDao issueDao;
  private final AuthorizationDao authorizationDao;
  private final UserFinder userFinder;
  private final PreviewCache dryRunCache;


  public IssueService(DefaultIssueFinder finder,
                      IssueWorkflow workflow,
                      IssueStorage issueStorage,
                      IssueUpdater issueUpdater,
                      IssueNotifications issueNotifications,
                      ActionPlanService actionPlanService,
                      RuleFinder ruleFinder,
                      ResourceDao resourceDao,
                      IssueDao issueDao,
                      AuthorizationDao authorizationDao,
                      UserFinder userFinder,
                      PreviewCache dryRunCache) {
    this.finder = finder;
    this.workflow = workflow;
    this.issueStorage = issueStorage;
    this.issueUpdater = issueUpdater;
    this.actionPlanService = actionPlanService;
    this.ruleFinder = ruleFinder;
    this.issueNotifications = issueNotifications;
    this.resourceDao = resourceDao;
    this.issueDao = issueDao;
    this.authorizationDao = authorizationDao;
    this.userFinder = userFinder;
    this.dryRunCache = dryRunCache;
  }

  /**
   * List of available transitions.
   * <p/>
   * Never return null, but return an empty list if the issue does not exist.
   */
  public List<Transition> listTransitions(String issueKey, UserSession userSession) {
    Issue issue = loadIssue(issueKey).first();
    return listTransitions(issue, userSession);
  }

  /**
   * Never return null, but an empty list if the issue does not exist.
   * No security check is done since it should already have been done to get the issue
   */
  public List<Transition> listTransitions(@Nullable Issue issue, UserSession userSession) {
    if (issue == null) {
      return Collections.emptyList();
    }
    List<Transition> outTransitions = workflow.outTransitions(issue);
    List<Transition> allowedTransitions = new ArrayList<Transition>();
    for (Transition transition : outTransitions) {
      DefaultIssue defaultIssue = (DefaultIssue) issue;
      String projectKey = defaultIssue.projectKey();
      if (StringUtils.isBlank(transition.requiredProjectPermission()) ||
        (projectKey != null && userSession.hasProjectPermission(transition.requiredProjectPermission(), projectKey))) {
        allowedTransitions.add(transition);
      }
    }
    return allowedTransitions;
  }

  public Issue doTransition(String issueKey, String transitionKey, UserSession userSession) {
    verifyLoggedIn(userSession);
    IssueQueryResult queryResult = loadIssue(issueKey);
    DefaultIssue defaultIssue = (DefaultIssue) queryResult.first();
    IssueChangeContext context = IssueChangeContext.createUser(new Date(), userSession.login());
    checkTransitionPermission(transitionKey, userSession, defaultIssue);
    if (workflow.doTransition(defaultIssue, transitionKey, context)) {
      issueStorage.save(defaultIssue);
      issueNotifications.sendChanges(defaultIssue, context, queryResult);
      dryRunCache.reportResourceModification(defaultIssue.componentKey());
    }
    return defaultIssue;
  }

  private void checkTransitionPermission(String transitionKey, UserSession userSession, DefaultIssue defaultIssue) {
    List<Transition> outTransitions = workflow.outTransitions(defaultIssue);
    for (Transition transition : outTransitions) {
      String projectKey = defaultIssue.projectKey();
      if (transition.key().equals(transitionKey) && StringUtils.isNotBlank(transition.requiredProjectPermission()) && projectKey != null) {
        userSession.checkProjectPermission(transition.requiredProjectPermission(), projectKey);
      }
    }
  }

  public Issue assign(String issueKey, @Nullable String assignee, UserSession userSession) {
    verifyLoggedIn(userSession);
    IssueQueryResult queryResult = loadIssue(issueKey);
    DefaultIssue issue = (DefaultIssue) queryResult.first();
    User user = null;
    if (!Strings.isNullOrEmpty(assignee)) {
      user = userFinder.findByLogin(assignee);
      if (user == null) {
        throw new IllegalArgumentException("Unknown user: " + assignee);
      }
    }
    IssueChangeContext context = IssueChangeContext.createUser(new Date(), userSession.login());
    if (issueUpdater.assign(issue, user, context)) {
      issueStorage.save(issue);
      issueNotifications.sendChanges(issue, context, queryResult);
      dryRunCache.reportResourceModification(issue.componentKey());
    }
    return issue;
  }

  public Issue plan(String issueKey, @Nullable String actionPlanKey, UserSession userSession) {
    verifyLoggedIn(userSession);
    ActionPlan actionPlan = null;
    if (!Strings.isNullOrEmpty(actionPlanKey)) {
      actionPlan = actionPlanService.findByKey(actionPlanKey, userSession);
      if (actionPlan == null) {
        throw new IllegalArgumentException("Unknown action plan: " + actionPlanKey);
      }
    }
    IssueQueryResult queryResult = loadIssue(issueKey);
    DefaultIssue issue = (DefaultIssue) queryResult.first();

    IssueChangeContext context = IssueChangeContext.createUser(new Date(), userSession.login());
    if (issueUpdater.plan(issue, actionPlan, context)) {
      issueStorage.save(issue);
      issueNotifications.sendChanges(issue, context, queryResult);
      dryRunCache.reportResourceModification(issue.componentKey());
    }
    return issue;
  }

  public Issue setSeverity(String issueKey, String severity, UserSession userSession) {
    verifyLoggedIn(userSession);
    IssueQueryResult queryResult = loadIssue(issueKey);
    DefaultIssue issue = (DefaultIssue) queryResult.first();
    userSession.checkProjectPermission(UserRole.ISSUE_ADMIN, issue.projectKey());

    IssueChangeContext context = IssueChangeContext.createUser(new Date(), userSession.login());
    if (issueUpdater.setManualSeverity(issue, severity, context)) {
      issueStorage.save(issue);
      issueNotifications.sendChanges(issue, context, queryResult);
      dryRunCache.reportResourceModification(issue.componentKey());
    }
    return issue;
  }

  public DefaultIssue createManualIssue(String componentKey, RuleKey ruleKey, @Nullable Integer line, @Nullable String message, @Nullable String severity,
                                        @Nullable Double effortToFix, UserSession userSession) {
    verifyLoggedIn(userSession);
    ResourceDto component = resourceDao.getResource(ResourceQuery.create().setKey(componentKey));
    ResourceDto project = resourceDao.getRootProjectByComponentKey(componentKey);
    if (component == null || project == null) {
      throw new IllegalArgumentException("Unknown component: " + componentKey);
    }
<<<<<<< HEAD
    if (!authorizationDao.isAuthorizedComponentKey(component.getKey(), userSession.userId(), UserRole.USER)) {
=======
    // Force use of correct key in case deprecated key is used
    issue.setComponentKey(resourceDto.getKey());
    issue.setComponentId(resourceDto.getId());
    if (!authorizationDao.isAuthorizedComponentKey(resourceDto.getKey(), userSession.userId(), UserRole.USER)) {
>>>>>>> refs/remotes/xinghuangxu/remotes/origin/branch-4.2
      // TODO throw unauthorized
      throw new IllegalStateException("User does not have the required role");
    }
    if (!ruleKey.isManual()) {
      throw new IllegalArgumentException("Issues can be created only on rules marked as 'manual': " + ruleKey);
    }
    Rule rule = findRule(ruleKey);

    DefaultIssue issue = (DefaultIssue) new DefaultIssueBuilder()
      .componentKey(component.getKey())
      .projectKey(project.getKey())
      .line(line)
      .message(!Strings.isNullOrEmpty(message) ? message : rule.getName())
      .severity(Objects.firstNonNull(severity, Severity.MAJOR))
      .effortToFix(effortToFix)
      .ruleKey(ruleKey)
      .reporter(UserSession.get().login())
      .build();

    Date now = new Date();
    issue.setCreationDate(now);
    issue.setUpdateDate(now);
    issueStorage.save(issue);
    dryRunCache.reportResourceModification(component.getKey());
    return issue;
  }

  private Rule findRule(RuleKey ruleKey) {
    Rule rule = ruleFinder.findByKey(ruleKey);
    if (rule == null) {
      throw new IllegalArgumentException("Unknown rule: " + ruleKey);
    }
    return rule;
  }

  public IssueQueryResult loadIssue(String issueKey) {
    IssueQueryResult result = finder.find(IssueQuery.builder().issueKeys(Arrays.asList(issueKey)).requiredRole(UserRole.USER).build());
    if (result.issues().size() != 1) {
      // TODO throw 404
      throw new IllegalArgumentException("Issue not found: " + issueKey);
    }
    return result;
  }

  public List<String> listStatus() {
    return workflow.statusKeys();
  }

  private void verifyLoggedIn(UserSession userSession) {
    if (!userSession.isLoggedIn()) {
      // must be logged
      throw new IllegalStateException("User is not logged in");
    }
  }

  // TODO result should be replaced by an aggregation object in IssueIndex
  public RulesAggregation findRulesByComponent(String componentKey, @Nullable Date periodDate, DbSession session) {
    RulesAggregation rulesAggregation = new RulesAggregation();
    for (RuleDto ruleDto : issueDao.findRulesByComponent(componentKey, periodDate, session)) {
      rulesAggregation.add(ruleDto);
    }
    return rulesAggregation;
  }

  // TODO result should be replaced by an aggregation object in IssueIndex
  public Multiset<String> findSeveritiesByComponent(String componentKey, @Nullable Date periodDate, DbSession session) {
    Multiset<String> aggregation = HashMultiset.create();
    for (String severity : issueDao.findSeveritiesByComponent(componentKey, periodDate, session)) {
      aggregation.add(severity);
    }
    return aggregation;
  }

}
