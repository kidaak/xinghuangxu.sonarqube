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
package org.sonar.plugins.core.sensors;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import org.sonar.api.CoreProperties;
import org.sonar.api.PropertyType;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.Qualifiers;
import org.sonar.api.resources.Resource;
import org.sonar.api.utils.WildcardPattern;
import org.sonar.core.measure.MeasurementFilter;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class CoverageMeasurementFilter implements MeasurementFilter {

  public static final String PROPERTY_COVERAGE_EXCLUSIONS = "sonar.coverage.exclusions";
  public static final String PROPERTY_COVERAGE_INCLUSIONS = "sonar.coverage.inclusions";

  private Settings settings;
  private CoverageDecorator decorator;
  private Collection<WildcardPattern> resourcePatterns;

  public CoverageMeasurementFilter(Settings settings, CoverageDecorator decorator) {
    this.settings = settings;
    this.decorator = decorator;

    initPatterns();
  }

  @Override
  public boolean accept(Resource<?> resource, Measure measure) {
    if (this.decorator.usedMetrics().contains(measure.getMetric())) {
      return !hasMatchingPattern(resource);
    } else {
      return true;
    }
  }

  private boolean hasMatchingPattern(Resource<?> resource) {
    boolean found = false;
    Iterator<WildcardPattern> iterator = resourcePatterns.iterator();
    while (!found && iterator.hasNext()) {
      found = resource.matchFilePattern(iterator.next().toString());
    }
    return found;
  }

  @VisibleForTesting
  void initPatterns() {
    Builder<WildcardPattern> builder = ImmutableList.<WildcardPattern>builder();
    for (String pattern : settings.getStringArray(PROPERTY_COVERAGE_EXCLUSIONS)) {
      builder.add(WildcardPattern.create(pattern));
    }
    resourcePatterns = builder.build();
  }

  public static List<? extends PropertyDefinition> getPropertyDefinitions() {
    return ImmutableList.of(
      PropertyDefinition.builder("sonar.coverage.exclusions")
        .category(CoreProperties.CATEGORY_EXCLUSIONS)
        .subCategory(CoreProperties.CATEGORY_CODE_COVERAGE) // TODO Replace with dedicated subcat
        .type(PropertyType.STRING)
        .multiValues(true)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .build()
      );
  }
}
