/*
 * SonarQube, open source software quality management tool.
<<<<<<< HEAD
 * Copyright (C) 2008-2014 SonarSource
=======
 * Copyright (C) 2008-2013 SonarSource
>>>>>>> refs/remotes/xinghuangxu/remotes/origin/branch-4.2
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

<<<<<<< HEAD
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.dbutils.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.core.persistence.Database;
import org.sonar.core.persistence.dialect.MySql;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
=======
import org.apache.commons.dbutils.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.utils.MessageException;
import org.sonar.core.persistence.Database;
import org.sonar.core.persistence.dialect.MySql;

import java.sql.*;
>>>>>>> refs/remotes/xinghuangxu/remotes/origin/branch-4.2

/**
 * Update a table by iterating a sub-set of rows. For each row a SQL UPDATE request
 * is executed.
 */
public class MassUpdater {

  private static final Logger LOGGER = LoggerFactory.getLogger(MassUpdater.class);
<<<<<<< HEAD
  private static final int DEFAULT_GROUP_SIZE = 250;
  private final Database db;
  private final int groupSize;

  public MassUpdater(Database db) {
    this(db, DEFAULT_GROUP_SIZE);
  }

  public MassUpdater(Database db, int groupSize) {
    this.db = db;
    this.groupSize = groupSize;
=======
  private static final String FAILURE_MESSAGE = "Fail to migrate data";
  private static final int GROUP_SIZE = 1000;
  private final Database db;

  public MassUpdater(Database db) {
    this.db = db;
>>>>>>> refs/remotes/xinghuangxu/remotes/origin/branch-4.2
  }

  public static interface InputLoader<S> {
    String selectSql();

<<<<<<< HEAD
    @CheckForNull
=======
>>>>>>> refs/remotes/xinghuangxu/remotes/origin/branch-4.2
    S load(ResultSet rs) throws SQLException;
  }

  public static interface InputConverter<S> {
    String updateSql();

<<<<<<< HEAD
    /**
     * Return false if you do not want to update this statement
     */
    boolean convert(S input, PreparedStatement updateStatement) throws SQLException;
  }

  public static interface PeriodicUpdater {

    /**
     * Return false if you do not want to update this statement
     */
    boolean update(Connection writeConnection) throws SQLException;
  }

  public List<Long> selectLong(String sql) {
    Connection readConnection = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      readConnection = db.getDataSource().getConnection();
      readConnection.setAutoCommit(false);

      stmt = initStatement(sql, readConnection);
      rs = stmt.executeQuery();
      List<Long> rows = new ArrayList<Long>();
      while (rs.next()) {
        if (!rs.wasNull()) {
          rows.add(rs.getLong(1));
        }
      }
      return rows;
    } catch (Exception e) {
      throw processError(e);
    } finally {
      DbUtils.closeQuietly(readConnection, stmt, rs);
    }
  }

  public <S> void execute(InputLoader<S> inputLoader, InputConverter<S> converter) {
    execute(inputLoader, converter, null);
  }

  public <S> void execute(InputLoader<S> inputLoader, InputConverter<S> converter, @Nullable PeriodicUpdater periodicUpdater) {
    long count = 0;
    Connection readConnection = null, writeConnection = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    PreparedStatement writeStatement = null;
    try {
      readConnection = db.getDataSource().getConnection();
      readConnection.setAutoCommit(false);
      if (readConnection.getMetaData().supportsTransactionIsolationLevel(Connection.TRANSACTION_READ_UNCOMMITTED)) {
        readConnection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
      }

      stmt = initStatement(convertSelectSql(inputLoader.selectSql(), db), readConnection);
      rs = stmt.executeQuery();

      int cursor = 0;
      while (rs.next()) {
        S row = inputLoader.load(rs);
        if (row == null) {
          continue;
        }

        if (writeConnection == null) {
          // do not open the write connection too early
          // else if the select  on read connection is long, then mysql
          // write connection fails with communication failure error because
          // no activity during a long period
          writeConnection = db.getDataSource().getConnection();
          writeConnection.setAutoCommit(false);
          writeStatement = writeConnection.prepareStatement(converter.updateSql());
        }

        if (converter.convert(row, writeStatement)) {
          writeStatement.addBatch();
          writeStatement.clearParameters();
          cursor++;
          count++;
        }

        if (cursor == groupSize) {
          commit(writeStatement, periodicUpdater);
          cursor = 0;
        }
      }
      if (cursor > 0) {
        commit(writeStatement, periodicUpdater);
      }

    } catch (SQLException e) {
      SqlUtil.log(LOGGER, e);
      throw processError(e);
    } catch (Exception e) {
      throw processError(e);
    } finally {
      DbUtils.closeQuietly(writeStatement);
      DbUtils.closeQuietly(writeConnection);
      DbUtils.closeQuietly(readConnection, stmt, rs);
      LOGGER.info("{} rows have been updated", count);
    }
  }

  private PreparedStatement initStatement(String sql, Connection readConnection) throws SQLException {
    PreparedStatement stmt = readConnection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
    if (db.getDialect().getId().equals(MySql.ID)) {
      stmt.setFetchSize(Integer.MIN_VALUE);
    } else {
      stmt.setFetchSize(1000);
    }
    return stmt;
  }

  private void commit(PreparedStatement writeStatement, @Nullable PeriodicUpdater periodicUpdater) throws SQLException {
    writeStatement.executeBatch();
    if (periodicUpdater != null) {
      periodicUpdater.update(writeStatement.getConnection());
    }
    writeStatement.getConnection().commit();
  }

  private static RuntimeException processError(Exception e) {
    throw new IllegalStateException(e);
  }

  @VisibleForTesting
  static String convertSelectSql(String selectSql, Database db) {
    String newSelectSql = selectSql;
    newSelectSql = newSelectSql.replace("${_true}", db.getDialect().getTrueSqlValue());
    newSelectSql = newSelectSql.replace("${_false}", db.getDialect().getFalseSqlValue());
    return newSelectSql;
=======
    void convert(S input, PreparedStatement updateStatement) throws SQLException;
  }

  public <S> void execute(InputLoader<S> inputLoader, InputConverter<S> converter) {
    long count = 0;
    try {
      Connection readConnection = db.getDataSource().getConnection();
      Statement stmt = null;
      ResultSet rs = null;
      Connection writeConnection = db.getDataSource().getConnection();
      PreparedStatement writeStatement = null;
      try {
        readConnection.setAutoCommit(false);
        writeConnection.setAutoCommit(false);
        writeStatement = writeConnection.prepareStatement(converter.updateSql());
        stmt = readConnection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        stmt.setFetchSize(GROUP_SIZE);
        if (db.getDialect().getId().equals(MySql.ID)) {
          stmt.setFetchSize(Integer.MIN_VALUE);
        } else {
          stmt.setFetchSize(GROUP_SIZE);
        }
        rs = stmt.executeQuery(inputLoader.selectSql());

        int cursor = 0;
        while (rs.next()) {
          converter.convert(inputLoader.load(rs), writeStatement);
          writeStatement.addBatch();

          cursor++;
          count++;
          if (cursor == GROUP_SIZE) {
            writeStatement.executeBatch();
            writeConnection.commit();
            cursor = 0;
          }
        }
        if (cursor > 0) {
          writeStatement.executeBatch();
          writeConnection.commit();
        }
      } finally {
        DbUtils.closeQuietly(writeStatement);
        DbUtils.closeQuietly(writeConnection);
        DbUtils.closeQuietly(readConnection, stmt, rs);

        LOGGER.info("{} rows have been updated", count);
      }
    } catch (SQLException e) {
      LOGGER.error(FAILURE_MESSAGE, e);
      SqlUtil.log(LOGGER, e);
      throw MessageException.of(FAILURE_MESSAGE);

    } catch (Exception e) {
      LOGGER.error(FAILURE_MESSAGE, e);
      throw MessageException.of(FAILURE_MESSAGE);
    }
>>>>>>> refs/remotes/xinghuangxu/remotes/origin/branch-4.2
  }

}
