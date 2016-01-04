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
package org.sonar.core.issue.db;

import org.junit.Test;
import org.sonar.api.issue.internal.DefaultIssue;
import org.sonar.api.issue.internal.DefaultIssueComment;
import org.sonar.api.issue.internal.IssueChangeContext;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.RuleQuery;
import org.sonar.api.utils.DateUtils;
<<<<<<< HEAD
import org.sonar.api.utils.Duration;
=======
import org.sonar.api.utils.internal.WorkDuration;
>>>>>>> refs/remotes/xinghuangxu/remotes/origin/branch-4.2
import org.sonar.core.persistence.AbstractDaoTestCase;
import org.sonar.core.persistence.MyBatis;

import java.util.Collection;
import java.util.Date;

public class IssueStorageTest extends AbstractDaoTestCase {

  IssueChangeContext context = IssueChangeContext.createUser(new Date(), "emmerik");

  @Test
  public void should_insert_new_issues() throws Exception {
    FakeSaver saver = new FakeSaver(getMyBatis(), new FakeRuleFinder());

    DefaultIssueComment comment = DefaultIssueComment.create("ABCDE", "emmerik", "the comment");
    // override generated key
    comment.setKey("FGHIJ");

    Date date = DateUtils.parseDate("2013-05-18");
    DefaultIssue issue = new DefaultIssue()
      .setKey("ABCDE")
      .setNew(true)

      .setRuleKey(RuleKey.of("squid", "AvoidCycle"))
      .setLine(5000)
      .setDebt(Duration.create(10L))
      .setReporter("emmerik")
      .setResolution("OPEN")
      .setStatus("OPEN")
      .setSeverity("BLOCKER")
      .setAttribute("foo", "bar")
      .addComment(comment)
      .setCreationDate(date)
      .setUpdateDate(date)
      .setCloseDate(date);

    saver.save(issue);

    checkTables("should_insert_new_issues", new String[]{"id", "created_at", "updated_at", "issue_change_creation_date"}, "issues", "issue_changes");
  }

  @Test
  public void should_update_issues() throws Exception {
    setupData("should_update_issues");

    FakeSaver saver = new FakeSaver(getMyBatis(), new FakeRuleFinder());

    DefaultIssueComment comment = DefaultIssueComment.create("ABCDE", "emmerik", "the comment");
    // override generated key
    comment.setKey("FGHIJ");

    Date date = DateUtils.parseDate("2013-05-18");
    DefaultIssue issue = new DefaultIssue()
      .setKey("ABCDE")
      .setNew(false)
      .setChanged(true)

        // updated fields
      .setLine(5000)
      .setDebt(Duration.create(10L))
      .setChecksum("FFFFF")
      .setAuthorLogin("simon")
      .setAssignee("loic")
      .setFieldChange(context, "severity", "INFO", "BLOCKER")
      .setReporter("emmerik")
      .setResolution("FIXED")
      .setStatus("RESOLVED")
      .setSeverity("BLOCKER")
      .setAttribute("foo", "bar")
      .addComment(comment)
      .setCreationDate(date)
      .setUpdateDate(date)
      .setCloseDate(date)

        // unmodifiable fields
      .setRuleKey(RuleKey.of("xxx", "unknown"))
      .setComponentKey("not:a:component");

    saver.save(issue);

    checkTables("should_update_issues", new String[]{"id", "created_at", "updated_at", "issue_change_creation_date"}, "issues", "issue_changes");
  }

  @Test
  public void should_resolve_conflicts_on_updates() throws Exception {
    setupData("should_resolve_conflicts_on_updates");

    FakeSaver saver = new FakeSaver(getMyBatis(), new FakeRuleFinder());

    Date date = DateUtils.parseDate("2013-05-18");
    DefaultIssue issue = new DefaultIssue()
      .setKey("ABCDE")
      .setNew(false)
      .setChanged(true)
      .setCreationDate(DateUtils.parseDate("2005-05-12"))
      .setUpdateDate(date)
      .setRuleKey(RuleKey.of("squid", "AvoidCycles"))
      .setComponentKey("struts:Action")

        // issue in database has been updated in 2013, after the loading by scan
      .setSelectedAt(DateUtils.parseDate("2005-01-01"))

        // fields to be updated
      .setLine(444)
      .setSeverity("BLOCKER")
      .setChecksum("FFFFF")
      .setAttribute("JIRA", "http://jira.com")

        // fields overridden by end-user -> do not save
      .setAssignee("looser")
      .setResolution(null)
      .setStatus("REOPEN");

    saver.save(issue);

    checkTables("should_resolve_conflicts_on_updates", new String[]{"id", "created_at", "updated_at", "issue_change_creation_date"}, "issues");
  }

  static class FakeSaver extends IssueStorage {
    protected FakeSaver(MyBatis mybatis, RuleFinder ruleFinder) {
      super(mybatis, ruleFinder);
    }

    @Override
    protected long componentId(DefaultIssue issue) {
      return 100l;
    }

    @Override
    protected long projectId(DefaultIssue issue) {
      return 10l;
    }
  }

  static class FakeRuleFinder implements RuleFinder {

    @Override
    public Rule findById(int ruleId) {
      return null;
    }

    @Override
    public Rule findByKey(String repositoryKey, String key) {
      return null;
    }

    @Override
    public Rule findByKey(RuleKey key) {
      Rule rule = new Rule().setRepositoryKey(key.repository()).setKey(key.rule());
      rule.setId(200);
      return rule;
    }

    @Override
    public Rule find(RuleQuery query) {
      return null;
    }

    @Override
    public Collection<Rule> findAll(RuleQuery query) {
      return null;
    }
  }
}
