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
package org.sonar.server.platform;

import org.slf4j.LoggerFactory;
import org.sonar.api.platform.ComponentContainer;
import org.sonar.api.platform.Server;
<<<<<<< HEAD
import org.sonar.core.persistence.DatabaseVersion;
=======
import org.sonar.api.profiles.AnnotationProfileParser;
import org.sonar.api.profiles.XMLProfileParser;
import org.sonar.api.profiles.XMLProfileSerializer;
import org.sonar.api.resources.Languages;
import org.sonar.api.resources.ResourceTypes;
import org.sonar.api.rules.AnnotationRuleParser;
import org.sonar.api.rules.XMLRuleParser;
import org.sonar.api.utils.HttpDownloader;
import org.sonar.api.utils.TimeProfiler;
import org.sonar.api.utils.UriReader;
import org.sonar.api.utils.internal.TempFolderCleaner;
import org.sonar.api.utils.internal.WorkDurationFactory;
import org.sonar.core.component.SnapshotPerspectives;
import org.sonar.core.config.Logback;
import org.sonar.core.i18n.DefaultI18n;
import org.sonar.core.i18n.GwtI18n;
import org.sonar.core.i18n.RuleI18nManager;
import org.sonar.core.issue.IssueFilterSerializer;
import org.sonar.core.issue.IssueNotifications;
import org.sonar.core.issue.IssueUpdater;
import org.sonar.core.issue.workflow.FunctionExecutor;
import org.sonar.core.issue.workflow.IssueWorkflow;
import org.sonar.core.measure.MeasureFilterEngine;
import org.sonar.core.measure.MeasureFilterExecutor;
import org.sonar.core.measure.MeasureFilterFactory;
import org.sonar.core.metric.DefaultMetricFinder;
import org.sonar.core.notification.DefaultNotificationManager;
import org.sonar.core.permission.PermissionFacade;
import org.sonar.core.persistence.*;
import org.sonar.core.preview.PreviewCache;
import org.sonar.core.profiling.Profiling;
import org.sonar.core.purge.PurgeProfiler;
import org.sonar.core.resource.DefaultResourcePermissions;
import org.sonar.core.rule.DefaultRuleFinder;
import org.sonar.core.technicaldebt.DefaultTechnicalDebtManager;
import org.sonar.core.technicaldebt.TechnicalDebtModelRepository;
import org.sonar.core.technicaldebt.TechnicalDebtModelSynchronizer;
import org.sonar.core.technicaldebt.TechnicalDebtXMLImporter;
import org.sonar.core.test.TestPlanPerspectiveLoader;
import org.sonar.core.test.TestablePerspectiveLoader;
import org.sonar.core.timemachine.Periods;
import org.sonar.core.user.DefaultUserFinder;
import org.sonar.core.user.HibernateUserFinder;
import org.sonar.jpa.dao.MeasuresDao;
import org.sonar.jpa.dao.ProfilesDao;
import org.sonar.jpa.dao.RulesDao;
import org.sonar.jpa.session.DatabaseSessionFactory;
import org.sonar.jpa.session.DatabaseSessionProvider;
import org.sonar.jpa.session.DefaultDatabaseConnector;
import org.sonar.jpa.session.ThreadLocalDatabaseSessionFactory;
import org.sonar.server.charts.ChartFactory;
import org.sonar.server.component.DefaultComponentFinder;
import org.sonar.server.component.DefaultRubyComponentService;
import org.sonar.server.db.EmbeddedDatabaseFactory;
import org.sonar.server.db.migrations.DatabaseMigration;
import org.sonar.server.db.migrations.DatabaseMigrations;
import org.sonar.server.db.migrations.DatabaseMigrator;
import org.sonar.server.es.ESIndex;
import org.sonar.server.es.ESNode;
import org.sonar.server.issue.*;
import org.sonar.server.issue.filter.IssueFilterService;
import org.sonar.server.issue.filter.IssueFilterWs;
import org.sonar.server.issue.ws.IssueShowWsHandler;
import org.sonar.server.issue.ws.IssuesWs;
import org.sonar.server.notifications.NotificationCenter;
import org.sonar.server.notifications.NotificationService;
import org.sonar.server.permission.InternalPermissionService;
import org.sonar.server.permission.InternalPermissionTemplateService;
import org.sonar.server.permission.PermissionFinder;
import org.sonar.server.plugins.*;
import org.sonar.server.qualityprofile.*;
import org.sonar.server.rule.*;
import org.sonar.server.rule.ws.*;
import org.sonar.server.source.CodeColorizers;
import org.sonar.server.source.DeprecatedSourceDecorator;
import org.sonar.server.source.HtmlSourceDecorator;
import org.sonar.server.source.SourceService;
import org.sonar.server.source.ws.SourcesShowWsHandler;
import org.sonar.server.source.ws.SourcesWs;
import org.sonar.server.startup.*;
import org.sonar.server.technicaldebt.DebtFormatter;
import org.sonar.server.technicaldebt.DebtService;
import org.sonar.server.text.MacroInterpreter;
import org.sonar.server.text.RubyTextService;
import org.sonar.server.ui.JRubyI18n;
import org.sonar.server.ui.JRubyProfiling;
import org.sonar.server.ui.PageDecorations;
import org.sonar.server.ui.Views;
import org.sonar.server.user.*;
import org.sonar.server.util.*;
import org.sonar.server.ws.ListingWs;
import org.sonar.server.ws.WebServiceEngine;
>>>>>>> refs/remotes/xinghuangxu/remotes/origin/branch-4.2

import javax.annotation.CheckForNull;
import java.util.Collection;
import java.util.Properties;

/**
 * @since 2.2
 */
public class Platform {

  private static final Platform INSTANCE = new Platform();

  private ServerComponents serverComponents;
  private ComponentContainer level1Container, level2Container, level3Container, level4Container;
  private ComponentContainer currentContainer;
  private boolean dbConnected = false;
  private boolean started = false;

  public Platform() {
  }

  public static Platform getInstance() {
    return INSTANCE;
  }

  /**
   * shortcut for ruby code
   */
  public static Server getServer() {
    return (Server) getInstance().getComponent(Server.class);
  }

  /**
   * Used by ruby code
   */
  @CheckForNull
  public static <T> T component(Class<T> type) {
    if (INSTANCE.started) {
      return INSTANCE.getContainer().getComponentByType(type);
    }
    return null;
  }

  public void init(Properties properties) {
    serverComponents = new ServerComponents(this, properties);
    if (!dbConnected) {
      startLevel1Container();
      startLevel2Container();
      dbConnected = true;
    }
  }

  // Platform is injected in Pico, so do not rename this method "start"
  public void doStart() {
    if (!started && getDatabaseStatus() == DatabaseVersion.Status.UP_TO_DATE) {
      startLevel34Containers();
      started = true;
    }
  }

  public boolean isStarted() {
    return started;
  }

  /**
   * Start level 1 only
   */
  private void startLevel1Container() {
    level1Container = new ComponentContainer();
    level1Container.addSingletons(serverComponents.level1Components());
    level1Container.startComponents();
    currentContainer = level1Container;
  }

  /**
   * Start level 2 only
   */
  private void startLevel2Container() {
    level2Container = level1Container.createChild();
    level2Container.addSingletons(serverComponents.level2Components());
    level2Container.startComponents();
    currentContainer = level2Container;
  }

  /**
   * Start level 3 and greater
   */
  private void startLevel34Containers() {
    level3Container = level2Container.createChild();
    level3Container.addSingletons(serverComponents.level3Components());
    level3Container.startComponents();
    currentContainer = level3Container;

    level4Container = level3Container.createChild();
    serverComponents.startLevel4Components(level4Container);
    currentContainer = level4Container;
    executeStartupTasks();
  }

  public void executeStartupTasks() {
    serverComponents.executeStartupTasks(level4Container);
  }

  public void restart() {
    // Do not need to initialize database connection, so level 1 is skipped
    if (level2Container != null) {
      level2Container.stopComponents();
      currentContainer = level1Container;
    }
    startLevel2Container();
    startLevel34Containers();
  }

  private DatabaseVersion.Status getDatabaseStatus() {
    DatabaseVersion version = getContainer().getComponentByType(DatabaseVersion.class);
    return version.getStatus();
  }

  // Do not rename "stop"
  public void doStop() {
    if (level1Container != null) {
      try {
        level1Container.stopComponents();
        level1Container = null;
        currentContainer = null;
        dbConnected = false;
        started = false;
      } catch (Exception e) {
        LoggerFactory.getLogger(getClass()).debug("Fail to stop server - ignored", e);
      }
    }
  }

  public void addComponents(Collection components) {
    serverComponents.addComponents(components);
  }

  public ComponentContainer getContainer() {
    return currentContainer;
  }

  public Object getComponent(Object key) {
    return getContainer().getComponentByKey(key);
  }
}
