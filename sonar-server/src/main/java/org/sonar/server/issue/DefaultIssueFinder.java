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

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.component.Component;
import org.sonar.api.issue.*;
import org.sonar.api.issue.internal.DefaultIssue;
import org.sonar.api.issue.internal.DefaultIssueComment;
import org.sonar.api.rules.Rule;
import org.sonar.api.user.User;
import org.sonar.api.user.UserFinder;
import org.sonar.api.utils.Paging;
<<<<<<< HEAD
=======
import org.sonar.api.utils.internal.WorkDurationFactory;
>>>>>>> refs/remotes/xinghuangxu/remotes/origin/branch-4.2
import org.sonar.core.component.ComponentDto;
import org.sonar.core.issue.DefaultIssueQueryResult;
import org.sonar.core.issue.db.IssueChangeDao;
import org.sonar.core.issue.db.IssueDao;
import org.sonar.core.issue.db.IssueDto;
import org.sonar.core.persistence.MyBatis;
import org.sonar.core.resource.ResourceDao;
import org.sonar.server.rule.DefaultRuleFinder;
import org.sonar.server.issue.actionplan.ActionPlanService;
import org.sonar.server.user.UserSession;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

import java.util.*;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;

/**
 * @since 3.6
 */
public class DefaultIssueFinder implements IssueFinder {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultIssueFinder.class);
  private final MyBatis myBatis;
  private final IssueDao issueDao;
  private final IssueChangeDao issueChangeDao;
  private final DefaultRuleFinder ruleFinder;
  private final UserFinder userFinder;
  private final ResourceDao resourceDao;
  private final ActionPlanService actionPlanService;

  public DefaultIssueFinder(MyBatis myBatis,
                            IssueDao issueDao, IssueChangeDao issueChangeDao,
                            DefaultRuleFinder ruleFinder,
                            UserFinder userFinder,
                            ResourceDao resourceDao,
                            ActionPlanService actionPlanService) {
    this.myBatis = myBatis;
    this.issueDao = issueDao;
    this.issueChangeDao = issueChangeDao;
    this.ruleFinder = ruleFinder;
    this.userFinder = userFinder;
    this.resourceDao = resourceDao;
    this.actionPlanService = actionPlanService;
  }

  DefaultIssue findByKey(String issueKey, String requiredRole) {
    IssueDto dto = issueDao.selectByKey(issueKey);
    if (dto == null) {
      throw new IllegalStateException("Unknown issue: " + issueKey);
    }
    if (!UserSession.get().hasProjectPermission(requiredRole, dto.getRootComponentKey())) {
      throw new IllegalStateException("User does not have the required role required to change the issue: " + issueKey);
    }

    return dto.toDefaultIssue();
  }

  @Override
  public IssueQueryResult find(IssueQuery query) {
    LOG.debug("IssueQuery : {}", query);
    long start = System.currentTimeMillis();
    SqlSession sqlSession = myBatis.openSession(false);
    try {
      // 1. Select the authorized ids of all the issues that match the query
      List<IssueDto> authorizedIssues = issueDao.selectIssueIds(query, UserSession.get().userId(), sqlSession);

      // 2. Sort all authorized issues
      List<IssueDto> authorizedSortedIssues = sort(authorizedIssues, query, authorizedIssues.size());

      // 3. Apply pagination
      Paging paging = Paging.create(query.pageSize(), query.pageIndex(), authorizedSortedIssues.size());
      Set<Long> pagedIssueIds = pagedIssueIds(authorizedSortedIssues, paging);

      // 4. Load issues and their related data (rules, components, projects, comments, action plans, ...) and sort then again
      List<IssueDto> pagedIssues = issueDao.selectByIds(pagedIssueIds, sqlSession);
      List<IssueDto> pagedSortedIssues = sort(pagedIssues, query, authorizedIssues.size());

      Map<String, DefaultIssue> issuesByKey = newHashMap();
      List<Issue> issues = newArrayList();
      Set<Integer> ruleIds = newHashSet();
      Set<Long> componentIds = newHashSet();
      Set<String> actionPlanKeys = newHashSet();
      Set<String> users = newHashSet();
      for (IssueDto dto : pagedSortedIssues) {
        DefaultIssue defaultIssue = dto.toDefaultIssue();
        issuesByKey.put(dto.getKee(), defaultIssue);
        issues.add(defaultIssue);
        ruleIds.add(dto.getRuleId());
        componentIds.add(dto.getComponentId());
        actionPlanKeys.add(dto.getActionPlanKey());
        if (dto.getReporter() != null) {
          users.add(dto.getReporter());
        }
        if (dto.getAssignee() != null) {
          users.add(dto.getAssignee());
        }
      }
      List<DefaultIssueComment> comments = issueChangeDao.selectCommentsByIssues(sqlSession, issuesByKey.keySet());
      for (DefaultIssueComment comment : comments) {
        DefaultIssue issue = issuesByKey.get(comment.issueKey());
        issue.addComment(comment);
        if (comment.userLogin() != null) {
          users.add(comment.userLogin());
        }
      }

      Collection<Component> components = findComponents(componentIds);
<<<<<<< HEAD
      Collection<Component> groupComponents = findSubProjects(components);
      Collection<Component> rootComponents = findProjects(components);
=======
      Collection<Component> groupComponents = findGroupComponents(components);
      Collection<Component> rootComponents = findRootComponents(components);
>>>>>>> refs/remotes/xinghuangxu/remotes/origin/branch-4.2

      Set<Component> allComponents = newHashSet(components);
      allComponents.addAll(groupComponents);
      allComponents.addAll(rootComponents);

      return new DefaultIssueQueryResult(issues)
        .setMaxResultsReached(authorizedIssues.size() == query.maxResults())
        .addRules(hideRules(query) ? Collections.<Rule>emptyList() : findRules(ruleIds))
        .addComponents(allComponents)
        .addProjects(rootComponents)
        .addActionPlans(findActionPlans(actionPlanKeys))
        .addUsers(findUsers(users))
        .setPaging(paging);
    } finally {
      MyBatis.closeQuietly(sqlSession);
      LOG.debug("IssueQuery execution time : {} ms", System.currentTimeMillis() - start);
    }
  }

  private boolean hideRules(IssueQuery query) {
    Boolean hideRules = query.hideRules();
    return hideRules != null ? hideRules : false;
  }

  private List<IssueDto> sort(List<IssueDto> issues, IssueQuery query, int allIssuesSize) {
    if (allIssuesSize < query.maxResults()) {
      return new IssuesFinderSort(issues, query).sort();
    }
    return issues;
  }

  private Set<Long> pagedIssueIds(Collection<IssueDto> issues, Paging paging) {
    Set<Long> issueIds = Sets.newLinkedHashSet();
    int index = 0;
    for (IssueDto issue : issues) {
      if (index >= paging.offset() && issueIds.size() < paging.pageSize()) {
        issueIds.add(issue.getId());
      } else if (issueIds.size() >= paging.pageSize()) {
        break;
      }
      index++;
    }
    return issueIds;
  }

  private Collection<Rule> findRules(Set<Integer> ruleIds) {
    return ruleFinder.findByIds(ruleIds);
  }

  private Collection<User> findUsers(Set<String> logins) {
    return userFinder.findByLogins(Lists.newArrayList(logins));
  }

  private Collection<Component> findComponents(Set<Long> componentIds) {
    return Lists.<Component>newArrayList(resourceDao.selectComponentsByIds(componentIds));
  }

<<<<<<< HEAD
  private Collection<Component> findSubProjects(Collection<Component> components) {
    return findComponents(newHashSet(Iterables.transform(components, new Function<Component, Long>() {
      @Override
      public Long apply(@Nullable Component input) {
        return input != null ? ((ComponentDto) input).subProjectId() : null;
=======
  private Collection<Component> findGroupComponents(Collection<Component> components) {
    return findComponents(newHashSet(Iterables.transform(components, new Function<Component, Long>() {
      @Override
      public Long apply(Component input) {
        return ((ComponentDto) input).subProjectId();
>>>>>>> refs/remotes/xinghuangxu/remotes/origin/branch-4.2
      }
    })));
  }

<<<<<<< HEAD
  private Collection<Component> findProjects(Collection<Component> components) {
    return findComponents(newHashSet(Iterables.transform(components, new Function<Component, Long>() {
      @Override
      public Long apply(@Nullable Component input) {
        return input != null ? ((ComponentDto) input).projectId() : null;
=======
  private Collection<Component> findRootComponents(Collection<Component> components) {
    return findComponents(newHashSet(Iterables.transform(components, new Function<Component, Long>() {
      @Override
      public Long apply(Component input) {
        return ((ComponentDto) input).projectId();
>>>>>>> refs/remotes/xinghuangxu/remotes/origin/branch-4.2
      }
    })));
  }

  private Collection<ActionPlan> findActionPlans(Set<String> actionPlanKeys) {
    return actionPlanService.findByKeys(actionPlanKeys);
  }

  @CheckForNull
  public Issue findByKey(String key) {
    IssueDto dto = issueDao.selectByKey(key);
    return dto != null ? dto.toDefaultIssue() : null;
  }

}
