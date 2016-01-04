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
package org.sonar.server.db.migrations;

import com.google.common.collect.ImmutableList;
<<<<<<< HEAD
import org.sonar.server.db.migrations.v36.ViolationMigration;
import org.sonar.server.db.migrations.v42.CompleteIssueMessageMigration;
import org.sonar.server.db.migrations.v42.PackageKeysMigration;
import org.sonar.server.db.migrations.v43.ConvertIssueDebtToMinutesMigration;
import org.sonar.server.db.migrations.v43.DevelopmentCostMeasuresMigration;
import org.sonar.server.db.migrations.v43.IssueChangelogMigration;
import org.sonar.server.db.migrations.v43.NotResolvedIssuesOnRemovedComponentsMigration;
import org.sonar.server.db.migrations.v43.RequirementMeasuresMigration;
import org.sonar.server.db.migrations.v43.TechnicalDebtMeasuresMigration;
import org.sonar.server.db.migrations.v44.ChangeLogMigration;
import org.sonar.server.db.migrations.v44.ConvertProfileMeasuresMigration;
import org.sonar.server.db.migrations.v44.FeedQProfileDatesMigration;
import org.sonar.server.db.migrations.v44.FeedQProfileKeysMigration;
import org.sonar.server.db.migrations.v44.IssueActionPlanKeyMigration;
import org.sonar.server.db.migrations.v44.MeasureDataMigration;
=======
import org.sonar.server.db.migrations.issueMessage42.IssueWithoutMessageMigration;
import org.sonar.server.db.migrations.packageKeys42.PackageKeysMigration;
import org.sonar.server.db.migrations.violation.ViolationMigration;
>>>>>>> refs/remotes/xinghuangxu/remotes/origin/branch-4.2

import java.util.List;

public interface DatabaseMigrations {

  List<Class<? extends DatabaseMigration>> CLASSES = ImmutableList.of(
<<<<<<< HEAD
    // 3.6
    ViolationMigration.class,

    // 4.2
    PackageKeysMigration.class, CompleteIssueMessageMigration.class,

    // 4.3
    ConvertIssueDebtToMinutesMigration.class,
    IssueChangelogMigration.class,
    TechnicalDebtMeasuresMigration.class,
    DevelopmentCostMeasuresMigration.class,
    RequirementMeasuresMigration.class,
    NotResolvedIssuesOnRemovedComponentsMigration.class,

    // 4.4
    IssueActionPlanKeyMigration.class,
    MeasureDataMigration.class,
    FeedQProfileKeysMigration.class,
    FeedQProfileDatesMigration.class,
    ChangeLogMigration.class,
    ConvertProfileMeasuresMigration.class
=======
    ViolationMigration.class,
    PackageKeysMigration.class, IssueWithoutMessageMigration.class
>>>>>>> refs/remotes/xinghuangxu/remotes/origin/branch-4.2
  );

}
