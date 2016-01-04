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

package org.sonar.server.technicaldebt;

import org.junit.Test;
import org.sonar.api.utils.internal.WorkDuration;
import org.sonar.core.i18n.DefaultI18n;

import java.util.Locale;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DebtFormatterTest {

  private static final Locale DEFAULT_LOCALE = Locale.getDefault();

  DefaultI18n i18n = mock(DefaultI18n.class);
  DebtFormatter formatter = new DebtFormatter(i18n);

  @Test
  public void format() {
    when(i18n.message(DEFAULT_LOCALE, "issue.technical_debt.x_days", null, 5)).thenReturn("5 days");
    when(i18n.message(DEFAULT_LOCALE, "issue.technical_debt.x_hours", null, 2)).thenReturn("2 hours");
    when(i18n.message(DEFAULT_LOCALE, "issue.technical_debt.x_minutes", null, 1)).thenReturn("1 minutes");

    assertThat(formatter.format(DEFAULT_LOCALE, WorkDuration.createFromValueAndUnit(5, WorkDuration.UNIT.DAYS, 8))).isEqualTo("5 days");
    assertThat(formatter.format(DEFAULT_LOCALE, WorkDuration.createFromValueAndUnit(2, WorkDuration.UNIT.HOURS, 8))).isEqualTo("2 hours");
    assertThat(formatter.format(DEFAULT_LOCALE, WorkDuration.createFromValueAndUnit(1, WorkDuration.UNIT.MINUTES, 8))).isEqualTo("1 minutes");

    assertThat(formatter.format(DEFAULT_LOCALE, WorkDuration.create(5, 2, 0, 8))).isEqualTo("5 days 2 hours");
    assertThat(formatter.format(DEFAULT_LOCALE, WorkDuration.create(0, 2, 1, 8))).isEqualTo("2 hours 1 minutes");
    assertThat(formatter.format(DEFAULT_LOCALE, WorkDuration.create(5, 2, 10, 8))).isEqualTo("5 days 2 hours");
  }

}
