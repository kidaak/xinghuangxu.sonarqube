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
package org.sonar.batch.index;

import com.google.common.annotations.VisibleForTesting;
import org.sonar.api.database.model.MeasureMapper;
import org.sonar.api.database.model.MeasureModel;
import org.sonar.api.database.model.Snapshot;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.RuleMeasure;
import org.sonar.api.resources.Resource;
import org.sonar.api.resources.ResourceUtils;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.technicaldebt.batch.Characteristic;
import org.sonar.batch.index.Cache.Entry;
import org.sonar.batch.scan.measure.MeasureCache;
import org.sonar.core.persistence.DbSession;
import org.sonar.core.persistence.MyBatis;

import javax.annotation.Nullable;

public final class MeasurePersister implements ScanPersister {
  private final MyBatis mybatis;
  private final RuleFinder ruleFinder;
  private final MeasureCache measureCache;
  private final SnapshotCache snapshotCache;
  private final ResourceCache resourceCache;

  public MeasurePersister(MyBatis mybatis, RuleFinder ruleFinder,
    MeasureCache measureCache, SnapshotCache snapshotCache, ResourceCache resourceCache) {
    this.mybatis = mybatis;
    this.ruleFinder = ruleFinder;
    this.measureCache = measureCache;
    this.snapshotCache = snapshotCache;
    this.resourceCache = resourceCache;
  }

  @Override
  public void persist() {
    DbSession session = mybatis.openSession(true);
    try {
      MeasureMapper mapper = session.getMapper(MeasureMapper.class);

      for (Entry<Measure> entry : measureCache.entries()) {
        String effectiveKey = entry.key()[0].toString();
        Measure measure = entry.value();
        Resource resource = resourceCache.get(effectiveKey);

        if (shouldPersistMeasure(resource, measure)) {
          Snapshot snapshot = snapshotCache.get(effectiveKey);
          MeasureModel measureModel = model(measure).setSnapshotId(snapshot.getId());
          mapper.insert(measureModel);
        }
      }

      session.commit();
    } catch (Exception e) {
<<<<<<< HEAD
      throw new IllegalStateException("Unable to save some measures", e);
    } finally {
      MyBatis.closeQuietly(session);
=======
      // SONAR-4066
      throw new SonarException(String.format("Unable to save measure for metric [%s] on component [%s]", measure.getMetricKey(), resource.getKey()), e);
    }
    if (model != null) {
      memoryOptimizer.evictDataMeasure(measure, model);
    }
  }

  private MeasureModel insertOrUpdate(Resource resource, Measure measure) {
    Snapshot snapshot = resourcePersister.getSnapshotOrFail(resource);
    if (measure.getId() != null) {
      return update(measure, snapshot);
    }
    if (shouldPersistMeasure(resource, measure)) {
      MeasureModel insert = insert(measure, snapshot);
      measure.setId(insert.getId());
      return insert;
>>>>>>> refs/remotes/xinghuangxu/remotes/origin/branch-4.2
    }
  }

  @VisibleForTesting
  static boolean shouldPersistMeasure(@Nullable Resource resource, @Nullable Measure measure) {
    if (resource == null || measure == null) {
      return false;
    }
    return measure.getPersistenceMode().useDatabase() &&
      !(ResourceUtils.isEntity(resource) && measure.isBestValue()) && isMeasureNotEmpty(measure);
  }

  private static boolean isMeasureNotEmpty(Measure measure) {
    boolean isNotEmpty = false;
    for (int i = 1; i <= 5; i++) {
      isNotEmpty = isNotEmpty || measure.getVariation(i) != null;
    }
    return measure.getValue() != null || measure.getData() != null
      || isNotEmpty;
  }

  private MeasureModel model(Measure measure) {
    MeasureModel model = new MeasureModel();
    // we assume that the index has updated the metric
    model.setMetricId(measure.getMetric().getId());
    model.setDescription(measure.getDescription());
    model.setData(measure.getData());
    model.setAlertStatus(measure.getAlertStatus());
    model.setAlertText(measure.getAlertText());
    model.setTendency(measure.getTendency());
    model.setVariationValue1(measure.getVariation1());
    model.setVariationValue2(measure.getVariation2());
    model.setVariationValue3(measure.getVariation3());
    model.setVariationValue4(measure.getVariation4());
    model.setVariationValue5(measure.getVariation5());
    model.setUrl(measure.getUrl());
    Characteristic characteristic = measure.getCharacteristic();
    if (characteristic != null) {
      model.setCharacteristicId(characteristic.id());
    }
    model.setPersonId(measure.getPersonId());
    model.setValue(measure.getValue());
    if (measure instanceof RuleMeasure) {
      RuleMeasure ruleMeasure = (RuleMeasure) measure;
      model.setRulePriority(ruleMeasure.getSeverity());
      RuleKey ruleKey = ruleMeasure.ruleKey();
      if (ruleKey != null) {
        Rule ruleWithId = ruleFinder.findByKey(ruleKey);
        if (ruleWithId == null) {
          throw new IllegalStateException("Can not save a measure with unknown rule " + ruleMeasure);
        }
        model.setRuleId(ruleWithId.getId());
      }
    }
    return model;
  }
<<<<<<< HEAD
=======

  private void insert(Iterable<MeasureModelAndDetails> values) {
    SqlSession session = mybatis.openSession();
    try {
      MeasureMapper mapper = session.getMapper(MeasureMapper.class);

      for (MeasureModelAndDetails value : values) {
        try {
          mapper.insert(value.getMeasureModel());
          if (value.getMeasureModel().getMeasureData() != null) {
            mapper.insertData(value.getMeasureModel().getMeasureData());
          }
        } catch (Exception e) {
          // SONAR-4066
          throw new SonarException(String.format("Unable to save measure for metric [%s] on component [%s]", value.getMetricKey(), value.getResourceKey()), e);
        }
      }

      session.commit();
    } finally {
      MyBatis.closeQuietly(session);
    }
  }

  private MeasureModel insert(Measure measure, Snapshot snapshot) {
    MeasureModel value = model(measure);
    value.setSnapshotId(snapshot.getId());

    SqlSession session = mybatis.openSession();
    try {
      MeasureMapper mapper = session.getMapper(MeasureMapper.class);

      mapper.insert(value);
      if (value.getMeasureData() != null) {
        mapper.insertData(value.getMeasureData());
      }

      session.commit();
    } finally {
      MyBatis.closeQuietly(session);
    }

    return value;
  }

  private MeasureModel update(Measure measure, Snapshot snapshot) {
    MeasureModel value = model(measure);
    value.setId(measure.getId());
    value.setSnapshotId(snapshot.getId());

    SqlSession session = mybatis.openSession();
    try {
      MeasureMapper mapper = session.getMapper(MeasureMapper.class);

      mapper.update(value);
      mapper.deleteData(value);
      if (value.getMeasureData() != null) {
        mapper.insertData(value.getMeasureData());
      }

      session.commit();
    } finally {
      MyBatis.closeQuietly(session);
    }

    return value;
  }

  // SONAR-4066
  private static class MeasureModelAndDetails {
    private final MeasureModel measureModel;
    private final String resourceKey;
    private final String metricKey;

    public MeasureModelAndDetails(MeasureModel measureModel, String resourceKey, String metricKey) {
      this.measureModel = measureModel;
      this.resourceKey = resourceKey;
      this.metricKey = metricKey;
    }

    public MeasureModel getMeasureModel() {
      return measureModel;
    }

    public String getResourceKey() {
      return resourceKey;
    }

    public String getMetricKey() {
      return metricKey;
    }
  }
>>>>>>> refs/remotes/xinghuangxu/remotes/origin/branch-4.2
}
