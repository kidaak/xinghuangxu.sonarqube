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
package org.sonar.batch.scan.filesystem;

import com.google.common.collect.Sets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.BatchComponent;
import org.sonar.api.batch.bootstrap.ProjectDefinition;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputFileFilter;
<<<<<<< HEAD
import org.sonar.api.batch.fs.internal.DeprecatedDefaultInputFile;
import org.sonar.api.resources.Project;
import org.sonar.api.utils.MessageException;
=======
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.resources.Project;
import org.sonar.api.utils.SonarException;
>>>>>>> refs/remotes/xinghuangxu/remotes/origin/branch-4.2

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Index input files into {@link InputFileCache}.
 */
public class FileIndexer implements BatchComponent {

<<<<<<< HEAD
  private static final Logger LOG = LoggerFactory.getLogger(FileIndexer.class);

=======
>>>>>>> refs/remotes/xinghuangxu/remotes/origin/branch-4.2
  private static final IOFileFilter DIR_FILTER = FileFilterUtils.and(HiddenFileFilter.VISIBLE, FileFilterUtils.notFileFilter(FileFilterUtils.prefixFileFilter(".")));
  private static final IOFileFilter FILE_FILTER = HiddenFileFilter.VISIBLE;

  private final List<InputFileFilter> filters;
  private final InputFileCache fileCache;
  private final boolean isAggregator;
  private final ExclusionFilters exclusionFilters;
  private final InputFileBuilderFactory inputFileBuilderFactory;

  public FileIndexer(List<InputFileFilter> filters, ExclusionFilters exclusionFilters, InputFileBuilderFactory inputFileBuilderFactory,
<<<<<<< HEAD
    InputFileCache cache, Project module, ProjectDefinition def) {
    this(filters, exclusionFilters, inputFileBuilderFactory, cache, !module.getModules().isEmpty());
  }

  /**
   * Used by scan2
   */
  public FileIndexer(List<InputFileFilter> filters, ExclusionFilters exclusionFilters, InputFileBuilderFactory inputFileBuilderFactory,
    InputFileCache cache, ProjectDefinition def) {
    this(filters, exclusionFilters, inputFileBuilderFactory, cache, !def.getSubProjects().isEmpty());
  }

  private FileIndexer(List<InputFileFilter> filters, ExclusionFilters exclusionFilters, InputFileBuilderFactory inputFileBuilderFactory,
    InputFileCache cache, boolean isAggregator) {
=======
                     InputFileCache cache, Project module) {
>>>>>>> refs/remotes/xinghuangxu/remotes/origin/branch-4.2
    this.filters = filters;
    this.exclusionFilters = exclusionFilters;
    this.inputFileBuilderFactory = inputFileBuilderFactory;
    this.fileCache = cache;
<<<<<<< HEAD
    this.isAggregator = isAggregator;
=======
    this.module = module;
>>>>>>> refs/remotes/xinghuangxu/remotes/origin/branch-4.2
  }

  void index(DefaultModuleFileSystem fileSystem) {
    if (isAggregator) {
      // No indexing for an aggregator module
      return;
    }
<<<<<<< HEAD
    LOG.info("Index files");
=======
    logger.info("Index files");
>>>>>>> refs/remotes/xinghuangxu/remotes/origin/branch-4.2
    exclusionFilters.prepare();

    Progress progress = new Progress(fileCache.byModule(fileSystem.moduleKey()));

    InputFileBuilder inputFileBuilder = inputFileBuilderFactory.create(fileSystem);
    if (!fileSystem.sourceFiles().isEmpty() || !fileSystem.testFiles().isEmpty()) {
      // Index only provided files
      indexFiles(inputFileBuilder, fileSystem, progress, fileSystem.sourceFiles(), InputFile.Type.MAIN);
      indexFiles(inputFileBuilder, fileSystem, progress, fileSystem.testFiles(), InputFile.Type.TEST);
    } else {
      for (File mainDir : fileSystem.sourceDirs()) {
        indexDirectory(inputFileBuilder, fileSystem, progress, mainDir, InputFile.Type.MAIN);
      }
      for (File testDir : fileSystem.testDirs()) {
        indexDirectory(inputFileBuilder, fileSystem, progress, testDir, InputFile.Type.TEST);
      }

    }

    // Remove files that have been removed since previous indexation
    for (InputFile removed : progress.removed) {
      fileCache.remove(fileSystem.moduleKey(), removed);
    }

    LOG.info(String.format("%d files indexed", progress.count()));

  }

  private void indexFiles(InputFileBuilder inputFileBuilder, DefaultModuleFileSystem fileSystem, Progress progress, List<File> sourceFiles, InputFile.Type type) {
    for (File sourceFile : sourceFiles) {
<<<<<<< HEAD
      DeprecatedDefaultInputFile inputFile = inputFileBuilder.create(sourceFile);
=======
      DefaultInputFile inputFile = inputFileBuilder.create(sourceFile);
>>>>>>> refs/remotes/xinghuangxu/remotes/origin/branch-4.2
      if (inputFile != null && exclusionFilters.accept(inputFile, type)) {
        indexFile(inputFileBuilder, fileSystem, progress, inputFile, type);
      }
    }
  }

  private void indexDirectory(InputFileBuilder inputFileBuilder, DefaultModuleFileSystem fileSystem, Progress status, File dirToIndex, InputFile.Type type) {
    Collection<File> files = FileUtils.listFiles(dirToIndex, FILE_FILTER, DIR_FILTER);
    for (File file : files) {
<<<<<<< HEAD
      DeprecatedDefaultInputFile inputFile = inputFileBuilder.create(file);
      if (inputFile != null && exclusionFilters.accept(inputFile, type)) {
        indexFile(inputFileBuilder, fileSystem, status, inputFile, type);
=======
      DefaultInputFile inputFile = inputFileBuilder.create(file);
      if (inputFile != null) {
        if (exclusionFilters.accept(inputFile, type)) {
          indexFile(inputFileBuilder, fileSystem, status, inputFile, type);
        }
>>>>>>> refs/remotes/xinghuangxu/remotes/origin/branch-4.2
      }
    }
  }

  private void indexFile(InputFileBuilder inputFileBuilder, DefaultModuleFileSystem fs,
<<<<<<< HEAD
    Progress status, DeprecatedDefaultInputFile inputFile, InputFile.Type type) {
    InputFile completedFile = inputFileBuilder.complete(inputFile, type);
    if (completedFile != null && accept(completedFile)) {
      fs.add(completedFile);
      status.markAsIndexed(completedFile);
=======
                         Progress status, DefaultInputFile inputFile, InputFile.Type type) {
    inputFile = inputFileBuilder.complete(inputFile, type);
    if (inputFile != null && accept(inputFile)) {
      fs.add(inputFile);
      status.markAsIndexed(inputFile);
>>>>>>> refs/remotes/xinghuangxu/remotes/origin/branch-4.2
    }
  }

  private boolean accept(InputFile inputFile) {
    // InputFileFilter extensions
    for (InputFileFilter filter : filters) {
      if (!filter.accept(inputFile)) {
        return false;
      }
    }
    return true;
  }

  private static class Progress {
    private final Set<InputFile> removed;
    private final Set<InputFile> indexed;

    Progress(Iterable<InputFile> removed) {
      this.removed = Sets.newHashSet(removed);
      this.indexed = new HashSet<InputFile>();
    }

    void markAsIndexed(InputFile inputFile) {
      if (indexed.contains(inputFile)) {
        throw MessageException.of("File " + inputFile + " can't be indexed twice. Please check that inclusion/exclusion patterns produce "
          + "disjoint sets for main and test files");
      }
      removed.remove(inputFile);
      indexed.add(inputFile);
    }

    int count() {
      return indexed.size();
    }
  }

}
