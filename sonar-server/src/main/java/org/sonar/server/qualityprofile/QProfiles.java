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

package org.sonar.server.qualityprofile;

import com.google.common.base.Strings;
import org.sonar.api.ServerComponent;
import org.sonar.api.component.Component;
import org.sonar.server.exceptions.BadRequestException;
import org.sonar.server.user.UserSession;
import org.sonar.server.util.Validation;

import javax.annotation.CheckForNull;

import java.util.List;

public class QProfiles implements ServerComponent {

  private static final String LANGUAGE_PARAM = "language";

  private final QProfileProjectOperations projectOperations;
  private final QProfileProjectLookup projectLookup;
  private final QProfileLookup profileLookup;

  public QProfiles(QProfileProjectOperations projectOperations, QProfileProjectLookup projectLookup,
    QProfileLookup profileLookup) {
    this.projectOperations = projectOperations;
    this.projectLookup = projectLookup;
    this.profileLookup = profileLookup;
  }

  public List<QProfile> allProfiles() {
    return profileLookup.allProfiles();
  }

  public List<QProfile> profilesByLanguage(String language) {
    return profileLookup.profiles(language);
  }

  @CheckForNull
  public QProfile profile(int id) {
    return profileLookup.profile(id);
  }

  @CheckForNull
  public QProfile profile(String name, String language) {
    checkProfileNameParam(name);
    Validation.checkMandatoryParameter(language, LANGUAGE_PARAM);
    return profileLookup.profile(name, language);
  }

  @CheckForNull
  public QProfile parent(QProfile profile) {
    return profileLookup.parent(profile);
  }

  public List<QProfile> children(QProfile profile) {
    return profileLookup.children(profile);
  }

  public List<QProfile> ancestors(QProfile profile) {
    return profileLookup.ancestors(profile);
  }

  // PROJECTS

  public List<Component> projects(int profileId) {
    return projectLookup.projects(profileId);
  }

  public int countProjects(QProfile profile) {
    return projectLookup.countProjects(profile);
  }

  /**
   * Used in /project/profile
   */
  @CheckForNull
  public QProfile findProfileByProjectAndLanguage(long projectId, String language) {
    return projectLookup.findProfileByProjectAndLanguage(projectId, language);
  }

  public void addProject(int profileId, long projectId) {
    projectOperations.addProject(profileId, projectId, UserSession.get());
  }

  public void removeProject(int profileId, long projectId) {
    projectOperations.removeProject(profileId, projectId, UserSession.get());
  }

  public void removeProjectByLanguage(String language, long projectId) {
    projectOperations.removeProject(language, projectId, UserSession.get());
  }

  public void removeAllProjects(int profileId) {
    projectOperations.removeAllProjects(profileId, UserSession.get());
  }

<<<<<<< HEAD
=======

  // PROFILE RULES

  @CheckForNull
  public QProfileRule findByRule(int ruleId) {
    return rules.findByRuleId(ruleId);
  }

  @CheckForNull
  public QProfileRule findByActiveRuleId(int activeRuleId) {
    return rules.findByActiveRuleId(activeRuleId);
  }

  @CheckForNull
  public QProfileRule findByProfileAndRule(int profileId, int ruleId) {
    return rules.findByProfileIdAndRuleId(profileId, ruleId);
  }

  public QProfileRuleLookup.QProfileRuleResult searchProfileRules(ProfileRuleQuery query, Paging paging) {
    return rules.search(query, paging);
  }

  public long countProfileRules(ProfileRuleQuery query) {
    return rules.countProfileRules(query);
  }

  public QProfileRuleLookup.QProfileRuleResult searchInactiveProfileRules(ProfileRuleQuery query, Paging paging) {
    return rules.searchInactives(query, paging);
  }

  public long countInactiveProfileRules(ProfileRuleQuery query) {
    return rules.countInactiveProfileRules(query);
  }

  public long countProfileRules(QProfile profile) {
    return rules.countProfileRules(ProfileRuleQuery.create(profile.id()));
  }

  public long countOverridingProfileRules(QProfile profile) {
    return rules.countProfileRules(ProfileRuleQuery.create(profile.id()).setInheritance(QProfileRule.OVERRIDES));
  }

  public void activateRule(int profileId, int ruleId, String severity) {
    activeRuleOperations.activateRule(profileId, ruleId, severity, UserSession.get());
  }

  public int bulkActivateRule(ProfileRuleQuery query) {
    return activeRuleOperations.activateRules(query.profileId(), query, UserSession.get());
  }

  public void deactivateRule(int profileId, int ruleId) {
    activeRuleOperations.deactivateRule(profileId, ruleId, UserSession.get());
  }

  public int bulkDeactivateRule(ProfileRuleQuery query) {
    return activeRuleOperations.deactivateRules(query, UserSession.get());
  }

  public void updateActiveRuleParam(int activeRuleId, String key, @Nullable String value) {
    activeRuleOperations.updateActiveRuleParam(activeRuleId, key, value, UserSession.get());
  }

  public void revertActiveRule(int activeRuleId) {
    activeRuleOperations.revertActiveRule(activeRuleId, UserSession.get());
  }

  public void updateActiveRuleNote(int activeRuleId, String note) {
    activeRuleOperations.updateActiveRuleNote(activeRuleId, note, UserSession.get());
  }

  public void deleteActiveRuleNote(int activeRuleId) {
    activeRuleOperations.deleteActiveRuleNote(activeRuleId, UserSession.get());
  }

  @CheckForNull
  public QProfileRule parentProfileRule(QProfileRule rule) {
    return rules.findParentProfileRule(rule);
  }

  public long countActiveRules(int ruleId) {
    return rules.countProfileRules(ruleId);
  }

>>>>>>> refs/remotes/xinghuangxu/remotes/origin/branch-4.2
  private void checkProfileNameParam(String name) {
    if (Strings.isNullOrEmpty(name)) {
      throw new BadRequestException("quality_profiles.please_type_profile_name");
    }
  }

}
