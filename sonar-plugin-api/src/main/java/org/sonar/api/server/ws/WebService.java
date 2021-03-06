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
package org.sonar.api.server.ws;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.sonar.api.ServerExtension;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Defines a web service. Note that contrary to the deprecated {@link org.sonar.api.web.Webservice}
 * the ws is fully implemented in Java and does not require any Ruby on Rails code.
 *
 * <p/>
 * The classes implementing this extension point must be declared in {@link org.sonar.api.SonarPlugin#getExtensions()}.
 *
 * <h2>How to use</h2>
 * <pre>
 * public class HelloWs implements WebService {
 *   @Override
 *   public void define(Context context) {
 *     NewController controller = context.createController("api/hello");
 *     controller.setDescription("Web service example");
 *
 *     // create the URL /api/hello/show
 *     controller.createAction("show")
 *       .setDescription("Entry point")
 *       .setHandler(new RequestHandler() {
 *         @Override
 *         public void handle(Request request, Response response) {
 *           // read request parameters and generates response output
 *           response.newJsonWriter()
 *             .prop("hello", request.mandatoryParam("key"))
 *             .close();
 *         }
 *      })
 *      .createParam("key", "Example key");
 *
 *    // important to apply changes
 *    controller.done();
 *   }
 * }
 * </pre>
 * <h2>How to unit test</h2>
 * <pre>
 * public class HelloWsTest {
 *   WebService ws = new HelloWs();
 *
 *   @Test
 *   public void should_define_ws() throws Exception {
 *     // WsTester is available in the Maven artifact org.codehaus.sonar:sonar-plugin-api
 *     // with type "test-jar"
 *     WsTester tester = new WsTester(ws);
 *     WebService.Controller controller = tester.controller("api/hello");
 *     assertThat(controller).isNotNull();
 *     assertThat(controller.path()).isEqualTo("api/hello");
 *     assertThat(controller.description()).isNotEmpty();
 *     assertThat(controller.actions()).hasSize(1);
 *
 *     WebService.Action show = controller.action("show");
 *     assertThat(show).isNotNull();
 *     assertThat(show.key()).isEqualTo("show");
 *     assertThat(index.handler()).isNotNull();
 *   }
 * }
 * </pre>
 *
 * @since 4.2
 */
public interface WebService extends ServerExtension {

  class Context {
    private final Map<String, Controller> controllers = Maps.newHashMap();

    /**
     * Create a new controller.
     * <p/>
     * Structure of request URL is <code>http://&lt;server&gt;/&lt>controller path&gt;/&lt;action path&gt;?&lt;parameters&gt;</code>.
     *
     * @param path the controller path must not start or end with "/". It is recommended to start with "api/"
     */
    public NewController createController(String path) {
      return new NewController(this, path);
    }

    private void register(NewController newController) {
      if (controllers.containsKey(newController.path)) {
        throw new IllegalStateException(
          String.format("The web service '%s' is defined multiple times", newController.path)
        );
      }
      controllers.put(newController.path, new Controller(newController));
    }

    @CheckForNull
    public Controller controller(String key) {
      return controllers.get(key);
    }

    public List<Controller> controllers() {
      return ImmutableList.copyOf(controllers.values());
    }
  }

  class NewController {
    private final Context context;
    private final String path;
    private String description, since;
    private final Map<String, NewAction> actions = Maps.newHashMap();

    private NewController(Context context, String path) {
      if (StringUtils.isBlank(path)) {
        throw new IllegalArgumentException("WS controller path must not be empty");
      }
      if (StringUtils.startsWith(path, "/") || StringUtils.endsWith(path, "/")) {
        throw new IllegalArgumentException("WS controller path must not start or end with slash: " + path);
      }
      this.context = context;
      this.path = path;
    }

    /**
     * Important - this method must be called in order to apply changes and make the
     * controller available in {@link org.sonar.api.server.ws.WebService.Context#controllers()}
     */
    public void done() {
      context.register(this);
    }

    /**
     * Optional plain-text description
     */
    public NewController setDescription(@Nullable String s) {
      this.description = s;
      return this;
    }

    /**
     * Optional version when the controller was created
     */
    public NewController setSince(@Nullable String s) {
      this.since = s;
      return this;
    }

    public NewAction createAction(String actionKey) {
      if (actions.containsKey(actionKey)) {
        throw new IllegalStateException(
          String.format("The action '%s' is defined multiple times in the web service '%s'", actionKey, path)
        );
      }
      NewAction action = new NewAction(actionKey);
      actions.put(actionKey, action);
      return action;
    }
  }

  @Immutable
  class Controller {
    private final String path, description, since;
    private final Map<String, Action> actions;

    private Controller(NewController newController) {
      if (newController.actions.isEmpty()) {
        throw new IllegalStateException(
          String.format("At least one action must be declared in the web service '%s'", newController.path)
        );
      }
      this.path = newController.path;
      this.description = newController.description;
      this.since = newController.since;
      ImmutableMap.Builder<String, Action> mapBuilder = ImmutableMap.builder();
      for (NewAction newAction : newController.actions.values()) {
        mapBuilder.put(newAction.key, new Action(this, newAction));
      }
      this.actions = mapBuilder.build();
    }

    public String path() {
      return path;
    }

    @CheckForNull
    public String description() {
      return description;
    }

    @CheckForNull
    public String since() {
      return since;
    }

    @CheckForNull
    public Action action(String actionKey) {
      return actions.get(actionKey);
    }

    public Collection<Action> actions() {
      return actions.values();
    }
  }

  class NewAction {
    private final String key;
    private String description, since;
    private boolean post = false, isInternal = false;
    private RequestHandler handler;
    private Map<String, NewParam> newParams = Maps.newHashMap();

    private NewAction(String key) {
      this.key = key;
    }

    public NewAction setDescription(@Nullable String s) {
      this.description = s;
      return this;
    }

    public NewAction setSince(@Nullable String s) {
      this.since = s;
      return this;
    }

    public NewAction setPost(boolean b) {
      this.post = b;
      return this;
    }

    public NewAction setInternal(boolean b) {
      this.isInternal = b;
      return this;
    }

    public NewAction setHandler(RequestHandler h) {
      this.handler = h;
      return this;
    }

    public NewParam createParam(String paramKey) {
      if (newParams.containsKey(paramKey)) {
        throw new IllegalStateException(
          String.format("The parameter '%s' is defined multiple times in the action '%s'", paramKey, key)
        );
      }
      NewParam newParam = new NewParam(paramKey);
      newParams.put(paramKey, newParam);
      return newParam;
    }

    public NewAction createParam(String paramKey, @Nullable String description) {
      createParam(paramKey).setDescription(description);
      return this;
    }
  }

  @Immutable
  class Action {
    private final String key, path, description, since;
    private final boolean post, isInternal;
    private final RequestHandler handler;
    private final Map<String, Param> params;

    private Action(Controller controller, NewAction newAction) {
      this.key = newAction.key;
      this.path = String.format("%s/%s", controller.path(), key);
      this.description = newAction.description;
      this.since = StringUtils.defaultIfBlank(newAction.since, controller.since);
      this.post = newAction.post;
      this.isInternal = newAction.isInternal;

      if (newAction.handler == null) {
        throw new IllegalStateException("RequestHandler is not set on action " + path);
      }
      this.handler = newAction.handler;

      ImmutableMap.Builder<String, Param> mapBuilder = ImmutableMap.builder();
      for (NewParam newParam : newAction.newParams.values()) {
        mapBuilder.put(newParam.key, new Param(newParam));
      }
      this.params = mapBuilder.build();
    }

    public String key() {
      return key;
    }

    public String path() {
      return path;
    }

    @CheckForNull
    public String description() {
      return description;
    }

    /**
     * Set if different than controller.
     */
    @CheckForNull
    public String since() {
      return since;
    }

    public boolean isPost() {
      return post;
    }

    public boolean isInternal() {
      return isInternal;
    }

    public RequestHandler handler() {
      return handler;
    }

    @CheckForNull
    public Param param(String key) {
      return params.get(key);
    }

    public Collection<Param> params() {
      return params.values();
    }

    @Override
    public String toString() {
      return path;
    }
  }

  class NewParam {
    private String key, description;

    private NewParam(String key) {
      this.key = key;
    }

    public NewParam setDescription(@Nullable String s) {
      this.description = s;
      return this;
    }

    @Override
    public String toString() {
      return key;
    }
  }

  @Immutable
  class Param {
    private final String key, description;

    public Param(NewParam newParam) {
      this.key = newParam.key;
      this.description = newParam.description;
    }

    public String key() {
      return key;
    }

    @CheckForNull
    public String description() {
      return description;
    }

    @Override
    public String toString() {
      return key;
    }
  }

  /**
   * Executed once at server startup.
   */
  void define(Context context);

}
