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
package org.sonar.server.issue.filter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sonar.api.server.ws.WebService;
<<<<<<< HEAD
import org.sonar.server.ws.WsTester;

import static org.fest.assertions.Assertions.assertThat;
=======
import org.sonar.api.server.ws.WsTester;
import org.sonar.core.issue.DefaultIssueFilter;
import org.sonar.server.exceptions.NotFoundException;
import org.sonar.server.user.MockUserSession;

import java.util.Arrays;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
>>>>>>> refs/remotes/xinghuangxu/remotes/origin/branch-4.2

@RunWith(MockitoJUnitRunner.class)
public class IssueFilterWsTest {

  @Mock
  IssueFilterService service;

  @Mock
  IssueFilterWriter issueFilterWriter;

  IssueFilterWs ws;

  WsTester tester;

  @Before
  public void setUp() throws Exception {
    ws = new IssueFilterWs(new AppAction(service, issueFilterWriter), new ShowAction(service, issueFilterWriter), new FavoritesAction(service));
    tester = new WsTester(ws);
  }

  @Test
  public void define_ws() throws Exception {
    WebService.Controller controller = tester.controller("api/issue_filters");
    assertThat(controller).isNotNull();
    assertThat(controller.description()).isNotEmpty();
    assertThat(controller.since()).isEqualTo("4.2");

<<<<<<< HEAD
    WebService.Action app = controller.action("app");
    assertThat(app).isNotNull();
    assertThat(app.params()).hasSize(1);
=======
    WebService.Action index = controller.action("page");
    assertThat(index).isNotNull();
    assertThat(index.handler()).isNotNull();
    assertThat(index.isPost()).isFalse();
    assertThat(index.isInternal()).isTrue();
  }

  @Test
  public void anonymous_page() throws Exception {
    MockUserSession.set().setLogin(null);
    tester.newRequest("page").execute().assertJson(getClass(), "anonymous_page.json");
  }
>>>>>>> refs/remotes/xinghuangxu/remotes/origin/branch-4.2

    WebService.Action show = controller.action("show");
    assertThat(show).isNotNull();
    assertThat(show.responseExampleAsString()).isNotEmpty();
    assertThat(show.params()).hasSize(1);

    WebService.Action favorites = controller.action("favorites");
    assertThat(favorites).isNotNull();
    assertThat(favorites.params()).isEmpty();
  }

<<<<<<< HEAD
=======
  @Test
  public void logged_in_page_with_selected_filter() throws Exception {
    MockUserSession session = MockUserSession.set().setLogin("eric").setUserId(123);
    when(service.find(13L, session)).thenReturn(
      new DefaultIssueFilter().setId(13L).setName("Blocker issues").setData("severity=BLOCKER").setUser("eric")
    );

    tester.newRequest("page").setParam("id", "13").execute()
      .assertJson(getClass(), "logged_in_page_with_selected_filter.json");
  }

  @Test
  public void selected_filter_can_not_be_modified() throws Exception {
    // logged-in user is 'eric' but filter is owned by 'simon'
    MockUserSession session = MockUserSession.set().setLogin("eric").setUserId(123).setGlobalPermissions("none");
    when(service.find(13L, session)).thenReturn(
      new DefaultIssueFilter().setId(13L).setName("Blocker issues").setData("severity=BLOCKER").setUser("simon").setShared(true)
    );

    tester.newRequest("page").setParam("id", "13").execute()
      .assertJson(getClass(), "selected_filter_can_not_be_modified.json");
  }

  @Test
  public void show_filter() throws Exception {
    // logged-in user is 'eric' but filter is owned by 'simon'
    MockUserSession session = MockUserSession.set().setLogin("eric").setUserId(123).setGlobalPermissions("none");
    when(service.find(13L, session)).thenReturn(
      new DefaultIssueFilter().setId(13L).setName("Blocker issues").setDescription("All Blocker Issues").setData("severity=BLOCKER").setUser("simon").setShared(true)
    );

    tester.newRequest("show").setParam("id", "13").execute()
      .assertJson(getClass(), "show_filter.json");
  }

  @Test
  public void show_unknown_filter() throws Exception {
    MockUserSession session = MockUserSession.set().setLogin("eric").setUserId(123).setGlobalPermissions("none");
    when(service.find(42L, session)).thenThrow(new NotFoundException("Filter 42 does not exist"));

    try {
      tester.newRequest("show").setParam("id", "42").execute();
      fail();
    } catch (NotFoundException e) {
      assertThat(e).hasMessage("Filter 42 does not exist");
    }
  }

>>>>>>> refs/remotes/xinghuangxu/remotes/origin/branch-4.2
}
