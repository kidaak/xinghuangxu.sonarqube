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

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.annotation.Nullable;

import java.io.Serializable;

/**
 * Please do not use this class, it will be refactored in 4.3
 *
 * @since 4.2
 */
public class WorkDuration implements Serializable {

  static final int DAY_POSITION_IN_LONG = 10000;
  static final int HOUR_POSITION_IN_LONG = 100;
  static final int MINUTE_POSITION_IN_LONG = 1;

  public static enum UNIT {
    DAYS, HOURS, MINUTES
  }

  private int hoursInDay;

  private long durationInSeconds;
  private int days;
  private int hours;
  private int minutes;

  private WorkDuration(long durationInSeconds, int days, int hours, int minutes, int hoursInDay) {
    this.durationInSeconds = durationInSeconds;
    this.days = days;
    this.hours = hours;
    this.minutes = minutes;
    this.hoursInDay = hoursInDay;
  }

  public static WorkDuration create(int days, int hours, int minutes, int hoursInDay) {
    long durationInSeconds = 3600L * days * hoursInDay;
    durationInSeconds += 3600L * hours;
    durationInSeconds += 60L * minutes;
    return new WorkDuration(durationInSeconds, days, hours, minutes, hoursInDay);
  }

  public static WorkDuration createFromValueAndUnit(int value, UNIT unit, int hoursInDay) {
    switch (unit) {
      case DAYS:
        return create(value, 0, 0, hoursInDay);
      case HOURS:
        return create(0, value, 0, hoursInDay);
      case MINUTES:
        return create(0, 0, value, hoursInDay);
      default:
        throw new IllegalStateException("Cannot create work duration");
    }
  }

  static WorkDuration createFromLong(long duration, int hoursInDay) {
    int days = 0, hours = 0, minutes = 0;

    long time = duration;
    Long currentTime = time / WorkDuration.DAY_POSITION_IN_LONG;
    if (currentTime > 0) {
      days = (currentTime.intValue());
      time = time - (currentTime * WorkDuration.DAY_POSITION_IN_LONG);
    }

    currentTime = time / WorkDuration.HOUR_POSITION_IN_LONG;
    if (currentTime > 0) {
      hours = currentTime.intValue();
      time = time - (currentTime * WorkDuration.HOUR_POSITION_IN_LONG);
    }

    currentTime = time / WorkDuration.MINUTE_POSITION_IN_LONG;
    if (currentTime > 0) {
      minutes = currentTime.intValue();
    }
    return WorkDuration.create(days, hours, minutes, hoursInDay);
  }

  static WorkDuration createFromSeconds(long seconds, int hoursInDay) {
    int days = (int) (seconds / hoursInDay / 60f / 60f);
    long currentDurationInSeconds = seconds - (3600L * days * hoursInDay);
    int hours = (int) (currentDurationInSeconds / 60f / 60f);
    currentDurationInSeconds = currentDurationInSeconds - (3600L * hours);
    int minutes = (int) (currentDurationInSeconds / 60f);
    return new WorkDuration(seconds, days, hours, minutes, hoursInDay);
  }

  /**
   * Return the duration in number of working days.
   * For instance, 3 days and 4 hours will return 3.5 days (if hoursIndDay is 8).
   */
  public double toWorkingDays() {
    return durationInSeconds / 60d / 60d / hoursInDay;
  }

  /**
   * Return the duration using the following format DDHHMM, where DD is the number of days, HH is the number of months, and MM the number of minutes.
   * For instance, 3 days and 4 hours will return 030400 (if hoursIndDay is 8).
   */
  public long toLong() {
    int workingDays = days;
    int workingHours = hours;
    if (hours >= hoursInDay) {
      int nbAdditionalDays = hours / hoursInDay;
      workingDays += nbAdditionalDays;
      workingHours = hours - (nbAdditionalDays * hoursInDay);
    }
    return workingDays * DAY_POSITION_IN_LONG + workingHours * HOUR_POSITION_IN_LONG + minutes * MINUTE_POSITION_IN_LONG;
  }

  public long toSeconds() {
    return durationInSeconds;
  }

  public WorkDuration add(@Nullable WorkDuration with) {
    if (with != null) {
      return WorkDuration.createFromSeconds(this.toSeconds() + with.toSeconds(), this.hoursInDay);
    } else {
      return this;
    }
  }

  public WorkDuration subtract(@Nullable WorkDuration with) {
    if (with != null) {
      return WorkDuration.createFromSeconds(this.toSeconds() - with.toSeconds(), this.hoursInDay);
    } else {
      return this;
    }
  }

  public WorkDuration multiply(int factor) {
    return WorkDuration.createFromSeconds(this.toSeconds() * factor, this.hoursInDay);
  }

  public int days() {
    return days;
  }

  public int hours() {
    return hours;
  }

  public int minutes() {
    return minutes;
  }

  @VisibleForTesting
  int hoursInDay() {
    return hoursInDay;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    WorkDuration that = (WorkDuration) o;
    if (durationInSeconds != that.durationInSeconds) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return (int) (durationInSeconds ^ (durationInSeconds >>> 32));
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
