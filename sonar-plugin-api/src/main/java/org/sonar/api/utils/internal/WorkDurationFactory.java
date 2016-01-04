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

package org.sonar.api.utils.internal;

import org.sonar.api.BatchComponent;
import org.sonar.api.CoreProperties;
import org.sonar.api.ServerComponent;
import org.sonar.api.config.Settings;

/**
 * Please do not use this class, it will be refactored in 4.3
 *
 * @since 4.2
 */
public final class WorkDurationFactory implements BatchComponent, ServerComponent {

  private final Settings settings;

  public WorkDurationFactory(Settings settings) {
    this.settings = settings;
  }

  public WorkDuration createFromWorkingValue(int value, WorkDuration.UNIT unit) {
    return WorkDuration.createFromValueAndUnit(value, unit, hoursInDay());
  }

  public WorkDuration createFromWorkingLong(long duration) {
    return WorkDuration.createFromLong(duration, hoursInDay());
  }

  private int hoursInDay(){
    return settings.getInt(CoreProperties.HOURS_IN_DAY);
  }

}
