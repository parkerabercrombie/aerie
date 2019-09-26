package gov.nasa.jpl.ammos.mpsa.aerie.plan.http;

import static org.assertj.core.api.Assertions.assertThat;

import gov.nasa.jpl.ammos.mpsa.aerie.plan.mocks.StubPlanController;
import gov.nasa.jpl.ammos.mpsa.aerie.plan.models.ActivityInstance;
import gov.nasa.jpl.ammos.mpsa.aerie.plan.models.NewPlan;
import gov.nasa.jpl.ammos.mpsa.aerie.plan.models.Plan;
import gov.nasa.jpl.ammos.mpsa.aerie.plan.utils.HttpRequester;
import io.javalin.Javalin;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.json.bind.JsonbBuilder;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PlanBindingsTest {
  private static Javalin app = null;

  @BeforeAll
  public static void setupServer() {
    final StubPlanController controller = new StubPlanController();
    final PlanBindings bindings = new PlanBindings(controller);

    app = Javalin.create();
    bindings.registerRoutes(app);
    app.start();
  }

  @AfterAll
  public static void shutdownServer() {
    app.stop();
  }

  private final HttpRequester client = new HttpRequester(
      HttpClient.newHttpClient(),
      URI.create("http://localhost:" + app.port()));

  @Test
  public void shouldGetPlans() throws IOException, InterruptedException {
    // GIVEN
    final String planId = StubPlanController.EXISTENT_PLAN_ID;
    final Plan plan = StubPlanController.EXISTENT_PLAN;

    // WHEN
    final HttpResponse<String> response = client.sendRequest("GET", "/plans");

    // THEN
    assertThat(response.statusCode()).isEqualTo(200);

    final Type PLAN_MAP_TYPE = new HashMap<String, Plan>(){}.getClass().getGenericSuperclass();
    final Map<String, Plan> plans = JsonbBuilder.create().fromJson(response.body(), PLAN_MAP_TYPE);

    assertThat(plans).containsEntry(planId, plan);
  }

  @Test
  public void shouldGetPlanById() throws IOException, InterruptedException {
    // GIVEN
    final String planId = StubPlanController.EXISTENT_PLAN_ID;

    // WHEN
    final HttpResponse<String> response = client.sendRequest("GET", "/plans/" + planId);

    // THEN
    assertThat(response.statusCode()).isEqualTo(200);
    JsonbBuilder.create().fromJson(response.body(), Plan.class);
  }

  @Test
  public void shouldReturn404OnNonexistentPlanById() throws IOException, InterruptedException {
    // GIVEN
    final String planId = StubPlanController.NONEXISTENT_PLAN_ID;

    // WHEN
    final HttpResponse<String> response = client.sendRequest("GET", "/plans/" + planId);

    // THEN
    assertThat(response.statusCode()).isEqualTo(404);
  }

  @Test
  public void shouldAddValidPlan() throws IOException, InterruptedException {
    // GIVEN
    final NewPlan plan = StubPlanController.VALID_NEW_PLAN;

    // WHEN
    final HttpResponse<String> response = client.sendRequest("POST", "/plans", plan);

    // THEN
    assertThat(response.statusCode()).isEqualTo(200);
  }

  @Test
  public void shouldNotAddInvalidPlan() throws IOException, InterruptedException {
    // GIVEN
    final NewPlan plan = StubPlanController.INVALID_NEW_PLAN;

    // WHEN
    final HttpResponse<String> response = client.sendRequest("POST", "/plans", plan);

    // THEN
    assertThat(response.statusCode()).isEqualTo(400);

    final Type STRING_LIST_TYPE = new ArrayList<String>(){}.getClass().getGenericSuperclass();
    JsonbBuilder.create().fromJson(response.body(), STRING_LIST_TYPE);
  }

  @Test
  public void shouldReplaceExistentPlan() throws IOException, InterruptedException {
    // GIVEN
    final String planId = StubPlanController.EXISTENT_PLAN_ID;
    final NewPlan plan = StubPlanController.VALID_NEW_PLAN;

    // WHEN
    final HttpResponse<String> response = client.sendRequest("PUT", "/plans/" + planId, plan);

    // THEN
    assertThat(response.statusCode()).isEqualTo(200);
  }

  @Test
  public void shouldNotReplaceNonexistentPlan() throws IOException, InterruptedException {
    // GIVEN
    final String planId = StubPlanController.NONEXISTENT_PLAN_ID;
    final NewPlan plan = StubPlanController.VALID_NEW_PLAN;

    // WHEN
    final HttpResponse<String> response = client.sendRequest("PUT", "/plans/" + planId, plan);

    // THEN
    assertThat(response.statusCode()).isEqualTo(404);
  }

  @Test
  public void shouldNotReplaceInvalidPlan() throws IOException, InterruptedException {
    // GIVEN
    final String planId = StubPlanController.EXISTENT_PLAN_ID;
    final NewPlan plan = StubPlanController.INVALID_NEW_PLAN;

    // WHEN
    final HttpResponse<String> response = client.sendRequest("PUT", "/plans/" + planId, plan);

    // THEN
    assertThat(response.statusCode()).isEqualTo(400);

    final Type STRING_LIST_TYPE = new ArrayList<String>(){}.getClass().getGenericSuperclass();
    JsonbBuilder.create().fromJson(response.body(), STRING_LIST_TYPE);
  }

  @Test
  public void shouldPatchExistentPlan() throws IOException, InterruptedException {
    // GIVEN
    final String planId = StubPlanController.EXISTENT_PLAN_ID;
    final Plan patch = StubPlanController.VALID_PATCH;

    // WHEN
    final HttpResponse<String> response = client.sendRequest("PATCH", "/plans/" + planId, patch);

    // THEN
    assertThat(response.statusCode()).isEqualTo(200);
  }

  @Test
  public void shouldNotPatchNonexistentPlan() throws IOException, InterruptedException {
    // GIVEN
    final String planId = StubPlanController.NONEXISTENT_PLAN_ID;
    final Plan patch = StubPlanController.VALID_PATCH;

    // WHEN
    final HttpResponse<String> response = client.sendRequest("PATCH", "/plans/" + planId, patch);

    // THEN
    assertThat(response.statusCode()).isEqualTo(404);
  }

  @Test
  public void shouldNotPatchInvalidPlan() throws IOException, InterruptedException {
    // GIVEN
    final String planId = StubPlanController.EXISTENT_PLAN_ID;
    final Plan patch= StubPlanController.INVALID_PATCH;

    // WHEN
    final HttpResponse<String> response = client.sendRequest("PATCH", "/plans/" + planId, patch);

    // THEN
    assertThat(response.statusCode()).isEqualTo(400);

    final Type STRING_LIST_TYPE = new ArrayList<String>(){}.getClass().getGenericSuperclass();
    JsonbBuilder.create().fromJson(response.body(), STRING_LIST_TYPE);
  }

  @Test
  public void shouldRemoveExistentPlan() throws IOException, InterruptedException {
    // GIVEN
    final String planId = StubPlanController.EXISTENT_PLAN_ID;

    // WHEN
    final HttpResponse<String> response = client.sendRequest("DELETE", "/plans/" + planId);

    // THEN
    assertThat(response.statusCode()).isEqualTo(200);
  }

  @Test
  public void shouldNotRemoveNonexistentPlan() throws IOException, InterruptedException {
    // GIVEN
    final String planId = StubPlanController.NONEXISTENT_PLAN_ID;

    // WHEN
    final HttpResponse<String> response = client.sendRequest("DELETE", "/plans/" + planId);

    // THEN
    assertThat(response.statusCode()).isEqualTo(404);
  }

  @Test
  public void shouldGetActivityInstances() throws IOException, InterruptedException {
    // GIVEN
    final String planId = StubPlanController.EXISTENT_PLAN_ID;
    final String activityId = StubPlanController.EXISTENT_ACTIVITY_ID;
    final ActivityInstance activity = StubPlanController.EXISTENT_ACTIVITY;

    // WHEN
    final HttpResponse<String> response = client.sendRequest("GET", "/plans/" + planId + "/activity_instances");

    // THEN
    assertThat(response.statusCode()).isEqualTo(200);

    final Type ACTIVITY_MAP_TYPE = new HashMap<String, ActivityInstance>(){}.getClass().getGenericSuperclass();
    final Map<String, ActivityInstance> activities = JsonbBuilder.create().fromJson(response.body(), ACTIVITY_MAP_TYPE);

    assertThat(activities).containsEntry(activityId, activity);
  }

  @Test
  public void shouldNotGetActivityInstancesForNonexistentPlan() throws IOException, InterruptedException {
    // GIVEN
    final String planId = StubPlanController.NONEXISTENT_PLAN_ID;

    // WHEN
    final HttpResponse<String> response = client.sendRequest("GET", "/plans/" + planId + "/activity_instances");

    // THEN
    assertThat(response.statusCode()).isEqualTo(404);
  }

  @Test
  public void shouldGetActivityInstanceById() throws IOException, InterruptedException {
    // GIVEN
    final String planId = StubPlanController.EXISTENT_PLAN_ID;
    final String activityInstanceId = StubPlanController.EXISTENT_ACTIVITY_ID;
    final ActivityInstance expectedActivityInstance = StubPlanController.EXISTENT_ACTIVITY;

    // WHEN
    final HttpResponse<String> response = client.sendRequest("GET", "/plans/" + planId + "/activity_instances/" + activityInstanceId);

    // THEN
    assertThat(response.statusCode()).isEqualTo(200);

    final ActivityInstance activityInstance = JsonbBuilder.create().fromJson(response.body(), ActivityInstance.class);
    assertThat(activityInstance).isEqualTo(expectedActivityInstance);
  }

  @Test
  public void shouldNotGetActivityInstanceFromNonexistentPlan() throws IOException, InterruptedException {
    // GIVEN
    final String planId = StubPlanController.NONEXISTENT_PLAN_ID;
    final String activityInstanceId = StubPlanController.EXISTENT_ACTIVITY_ID;

    // WHEN
    final HttpResponse<String> response = client.sendRequest("GET", "/plans/" + planId + "/activity_instances/" + activityInstanceId);

    // THEN
    assertThat(response.statusCode()).isEqualTo(404);
  }

  @Test
  public void shouldNotGetNonexistentActivityInstance() throws IOException, InterruptedException {
    // GIVEN
    final String planId = StubPlanController.EXISTENT_PLAN_ID;
    final String activityInstanceId = StubPlanController.NONEXISTENT_ACTIVITY_ID;

    // WHEN
    final HttpResponse<String> response = client.sendRequest("GET", "/plans/" + planId + "/activity_instances/" + activityInstanceId);

    // THEN
    assertThat(response.statusCode()).isEqualTo(404);
  }

  @Test
  public void shouldAddActivityInstancesToPlan() throws IOException, InterruptedException {
    // GIVEN
    final String planId = StubPlanController.EXISTENT_PLAN_ID;
    final ActivityInstance activityInstance = StubPlanController.VALID_ACTIVITY;

    // WHEN
    final HttpResponse<String> response = client.sendRequest("POST", "/plans/" + planId + "/activity_instances", List.of(activityInstance));

    // THEN
    assertThat(response.statusCode()).isEqualTo(200);

    final Type STRING_LIST_TYPE = new ArrayList<String>(){}.getClass().getGenericSuperclass();
    final List<String> activityIds = JsonbBuilder.create().fromJson(response.body(), STRING_LIST_TYPE);

    assertThat(activityIds).isEqualTo(List.of(StubPlanController.EXISTENT_ACTIVITY_ID));
  }

  @Test
  public void shouldNotAddActivityInstancesToNonexistentPlan() throws IOException, InterruptedException {
    // GIVEN
    final String planId = StubPlanController.NONEXISTENT_PLAN_ID;
    final ActivityInstance activityInstance = StubPlanController.VALID_ACTIVITY;

    // WHEN
    final HttpResponse<String> response = client.sendRequest("POST", "/plans/" + planId + "/activity_instances", List.of(activityInstance));

    // THEN
    assertThat(response.statusCode()).isEqualTo(404);
  }

  @Test
  public void shouldNotAddInvalidActivityInstancesToPlan() throws IOException, InterruptedException {
    // GIVEN
    final String planId = StubPlanController.EXISTENT_PLAN_ID;
    final ActivityInstance activityInstance = StubPlanController.INVALID_ACTIVITY;

    // WHEN
    final HttpResponse<String> response = client.sendRequest("POST", "/plans/" + planId + "/activity_instances", List.of(activityInstance));

    // THEN
    assertThat(response.statusCode()).isEqualTo(400);

    final Type STRING_LIST_TYPE = new ArrayList<String>(){}.getClass().getGenericSuperclass();
    JsonbBuilder.create().fromJson(response.body(), STRING_LIST_TYPE);
  }
}