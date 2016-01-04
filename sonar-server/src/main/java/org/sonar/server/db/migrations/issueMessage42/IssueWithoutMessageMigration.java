/*
 * SonarQube, open source software quality management tool.
 * Copyright (C) 2008-2013 SonarSource
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
package org.sonar.server.db.migrations.issueMessage42;

import org.sonar.core.persistence.Database;
import org.sonar.server.db.migrations.DatabaseMigration;
import org.sonar.server.db.migrations.MassUpdater;
import org.sonar.server.db.migrations.SqlUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Used in Rails migration 497
 *
 * @since 4.2
 */
public class IssueWithoutMessageMigration implements DatabaseMigration {

  private final Database db;

  public IssueWithoutMessageMigration(Database database) {
    this.db = database;
  }

  @Override
  public void execute() {
    new MassUpdater(db).execute(
      new MassUpdater.InputLoader<Row>() {
        @Override
        public String selectSql() {
          return "SELECT i.id, r.name FROM issues i INNER JOIN rules r ON r.id=i.rule_id WHERE i.message IS NULL";
        }

        @Override
        public Row load(ResultSet rs) throws SQLException {
          Row row = new Row();
          row.issueId = SqlUtil.getLong(rs, 1);
          row.ruleName = rs.getString(2);
          return row;
        }
      },
      new MassUpdater.InputConverter<Row>() {
        @Override
        public String updateSql() {
          return "UPDATE issues SET message=? WHERE id=?";
        }

        @Override
        public void convert(Row row, PreparedStatement updateStatement) throws SQLException {
          updateStatement.setString(1, row.ruleName);
          updateStatement.setLong(2, row.issueId);
        }
      }
    );
  }

  private static class Row {
    private Long issueId;
    private String ruleName;
  }
}
