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
package org.sonar.batch.scan;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Java;
import org.sonar.api.resources.Languages;
import org.sonar.api.utils.MessageException;
<<<<<<< HEAD
import org.sonar.batch.languages.DeprecatedLanguagesReferential;
import org.sonar.batch.languages.LanguagesReferential;
=======
>>>>>>> refs/remotes/xinghuangxu/remotes/origin/branch-4.2

import static org.fest.assertions.Assertions.assertThat;

public class LanguageVerifierTest {

  Settings settings = new Settings();
<<<<<<< HEAD
  LanguagesReferential languages = new DeprecatedLanguagesReferential(new Languages(Java.INSTANCE));
=======
  Languages languages = new Languages(Java.INSTANCE);
>>>>>>> refs/remotes/xinghuangxu/remotes/origin/branch-4.2
  DefaultFileSystem fs = new DefaultFileSystem();

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void language_is_not_set() throws Exception {
    LanguageVerifier verifier = new LanguageVerifier(settings, languages, fs);
    verifier.start();

    // no failure and no language is forced
    assertThat(fs.languages()).isEmpty();

    verifier.stop();
  }

  @Test
  public void language_is_valid() throws Exception {
    settings.setProperty("sonar.language", "java");

    LanguageVerifier verifier = new LanguageVerifier(settings, languages, fs);
    verifier.start();

    // no failure and language is hardly registered
    assertThat(fs.languages()).contains("java");

    verifier.stop();
  }

  @Test
  public void language_is_not_valid() throws Exception {
    thrown.expect(MessageException.class);
    thrown.expectMessage("You must install a plugin that supports the language 'php'");

    settings.setProperty("sonar.language", "php");
    LanguageVerifier verifier = new LanguageVerifier(settings, languages, fs);
    verifier.start();
  }
}
