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
package org.sonar.server.qualitygate;

import org.sonar.server.exceptions.ForbiddenException;

import org.sonar.server.exceptions.UnauthorizedException;
import org.sonar.server.user.UserSessionTestUtils;
import org.sonar.core.permission.GlobalPermissions;
import org.sonar.server.user.MockUserSession;
import org.sonar.server.user.UserSession;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sonar.core.qualitygate.db.QualityGateDao;
import org.sonar.core.qualitygate.db.QualityGateDto;
import org.sonar.server.exceptions.BadRequestException;

import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class QualityGatesTest {

  @Mock
  private QualityGateDao dao;

  private QualityGates qGates;

  UserSession authorizedUserSession = MockUserSession.create().setLogin("gaudol").setName("Olivier").setGlobalPermissions(GlobalPermissions.QUALITY_PROFILE_ADMIN);
  UserSession unauthenticatedUserSession = MockUserSession.create();
  UserSession unauthorizedUserSession = MockUserSession.create().setLogin("polop").setName("Polop");

  @Before
  public void initialize() {
    qGates = new QualityGates(dao);
    UserSessionTestUtils.setUserSession(authorizedUserSession);
  }

  @Test
  public void should_list_qgates() throws Exception {
    List<QualityGateDto> allQgates = Lists.newArrayList(new QualityGateDto().setName("Gate One"), new QualityGateDto().setName("Gate Two"));
    when(dao.selectAll()).thenReturn(allQgates);
    assertThat(qGates.list()).isEqualTo(allQgates);
  }

  @Test
  public void should_create_qgate() throws Exception {
    String name = "SG-1";
    QualityGateDto sg1 = qGates.create(name);
    assertThat(sg1.getName()).isEqualTo(name);
    verify(dao).selectByName(name);
    verify(dao).insert(sg1);
  }

  @Test(expected = UnauthorizedException.class)
  public void should_fail_create_on_anonymous() throws Exception {
    UserSessionTestUtils.setUserSession(unauthenticatedUserSession);
    qGates.create("polop");
  }

  @Test(expected = ForbiddenException.class)
  public void should_fail_create_on_missing_permission() throws Exception {
    UserSessionTestUtils.setUserSession(unauthorizedUserSession);
    qGates.create("polop");
  }


  @Test(expected = BadRequestException.class)
  public void should_fail_create_on_empty_name() throws Exception {
    qGates.create("");
  }

  @Test(expected = BadRequestException.class)
  public void should_fail_create_on_duplicate_name() throws Exception {
    String name = "SG-1";
    when(dao.selectByName(name)).thenReturn(new QualityGateDto().setName(name).setId(42L));
    qGates.create(name);
  }
}