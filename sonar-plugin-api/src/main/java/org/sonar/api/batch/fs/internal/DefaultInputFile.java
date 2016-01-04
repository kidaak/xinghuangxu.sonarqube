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
package org.sonar.api.batch.fs.internal;

<<<<<<< HEAD
=======
import org.apache.commons.io.FilenameUtils;
>>>>>>> refs/remotes/xinghuangxu/remotes/origin/branch-4.2
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.utils.PathUtils;

import javax.annotation.CheckForNull;

import java.io.File;
import java.io.Serializable;

/**
 * @since 4.2
 */
public class DefaultInputFile implements InputFile, Serializable {

  private final String relativePath;
  private String absolutePath;
  private String language;
  private Type type = Type.MAIN;
  private Status status;
  private String hash;
  private int lines;
  private String key;

  public DefaultInputFile(String relativePath) {
    this.relativePath = PathUtils.sanitize(relativePath);
  }

  @Override
  public String relativePath() {
    return relativePath;
  }

  /**
   * Marked as nullable just for the unit tests that do not call {@link #setFile(java.io.File)}
   * previously.
   */
  @Override
  @CheckForNull
  public String absolutePath() {
    return absolutePath;
  }

  @Override
  public File file() {
    if (absolutePath == null) {
      throw new IllegalStateException("Can not return the java.io.File because absolute path is not set (see method setFile(java.io.File))");
    }
    return new File(absolutePath);
  }

  /**
   * Marked as nullable just for the unit tests that do not call {@link #setLanguage(String)}
   * previously.
   */
  @CheckForNull
  @Override
  public String language() {
    return language;
  }

  @Override
  public Type type() {
    return type;
  }

  /**
   * Marked as nullable just for the unit tests that do not previously call
   * {@link #setStatus(org.sonar.api.batch.fs.InputFile.Status)}
   */
  @CheckForNull
  @Override
  public Status status() {
    return status;
  }

  /**
   * Digest hash of the file. Marked as nullable just for the unit tests
   * that do not previously call {@link #setHash(String)}
   */
  @CheckForNull
  public String hash() {
    return hash;
  }

  @Override
  public int lines() {
    return lines;
  }

  /**
   * Component key. It's marked as nullable just for the unit tests that
   * do not previously call {@link #setKey(String)}.
   */
  @CheckForNull
  public String key() {
    return key;
  }

  public DefaultInputFile setAbsolutePath(String s) {
    this.absolutePath = PathUtils.sanitize(s);
    return this;
  }

  public DefaultInputFile setLanguage(String language) {
    this.language = language;
    return this;
  }

  public DefaultInputFile setFile(File file) {
    setAbsolutePath(file.getAbsolutePath());
    return this;
  }

  public DefaultInputFile setType(Type type) {
    this.type = type;
    return this;
  }

  public DefaultInputFile setStatus(Status status) {
    this.status = status;
    return this;
  }

  public DefaultInputFile setHash(String hash) {
    this.hash = hash;
    return this;
  }

  public DefaultInputFile setLines(int lines) {
    this.lines = lines;
    return this;
  }

  public DefaultInputFile setKey(String s) {
    this.key = s;
    return this;
  }

<<<<<<< HEAD
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DefaultInputFile)) {
      return false;
    }

    DefaultInputFile that = (DefaultInputFile) o;
    return relativePath.equals(that.relativePath);
  }

  @Override
  public int hashCode() {
    return relativePath.hashCode();
  }

=======
  /**
   * Key used before version 4.2. It can be different than {@link #key} on Java files.
   */
  public String deprecatedKey() {
    return deprecatedKey;
  }

  public DefaultInputFile setDeprecatedKey(String s) {
    this.deprecatedKey = s;
    return this;
  }

  /**
   * Used only for backward-compatibility. Meaningless since version 4.2.
   */
  public String sourceDirAbsolutePath() {
    return sourceDirAbsolutePath;
  }

  public DefaultInputFile setSourceDirAbsolutePath(String s) {
    this.sourceDirAbsolutePath = FilenameUtils.normalize(s, true);
    return this;
  }

  /**
   * Used only for backward-compatibility. Meaningless since version 4.2.
   */

  public String pathRelativeToSourceDir() {
    return pathRelativeToSourceDir;
  }

  public DefaultInputFile setPathRelativeToSourceDir(String s) {
    this.pathRelativeToSourceDir = FilenameUtils.normalize(s, true);
    return this;
  }

  /**
   * @deprecated in 4.2. Replaced by {@link org.sonar.api.batch.fs.FileSystem#baseDir()}
   */
  @Deprecated
  @Override
  public File getFileBaseDir() {
    return new File(basedir);
  }

  public void setBasedir(File basedir) {
    this.basedir = PathUtils.sanitize(basedir.getAbsolutePath());
  }

  /**
   * @deprecated in 4.2. Use {@link #file()}
   */
  @Deprecated
  @Override
  public File getFile() {
    return file();
  }

  /**
   * @deprecated in 4.2. Use {@link #relativePath()}
   */
  @Deprecated
>>>>>>> refs/remotes/xinghuangxu/remotes/origin/branch-4.2
  @Override
  public String toString() {
    return "[relative=" + relativePath + ", abs=" + absolutePath + "]";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    DefaultInputFile that = (DefaultInputFile) o;
    return relativePath.equals(that.relativePath);
  }

  @Override
  public int hashCode() {
    return relativePath.hashCode();
  }

  @Override
  public String toString() {
    return "[relative=" + relativePath + ", abs=" + absolutePath + "]";
  }
}
