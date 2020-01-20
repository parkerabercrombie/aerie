package gov.nasa.jpl.ammos.mpsa.aerie.plan.controllers;

import gov.nasa.jpl.ammos.mpsa.aerie.plan.exceptions.NoSuchPlanException;
import gov.nasa.jpl.ammos.mpsa.aerie.plan.models.ActivityInstance;
import gov.nasa.jpl.ammos.mpsa.aerie.plan.models.NewPlan;
import gov.nasa.jpl.ammos.mpsa.aerie.plan.models.Plan;
import gov.nasa.jpl.ammos.mpsa.aerie.plan.remotes.AdaptationService;
import gov.nasa.jpl.ammos.mpsa.aerie.plan.remotes.PlanRepository;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.unmodifiableList;

public final class PlanValidator {
  private final PlanRepository planRepository;
  private final AdaptationService adaptationService;

  private final BreadcrumbCursor breadcrumbCursor = new BreadcrumbCursor();
  private final List<Pair<List<Breadcrumb>, String>> messages = new ArrayList<>();

  public PlanValidator(final PlanRepository planRepository, final AdaptationService adaptationService) {
    this.planRepository = planRepository;
    this.adaptationService = adaptationService;
  }

  public void validateActivity(final ActivityInstance activityInstance) {
    if (activityInstance.startTimestamp == null) with("startTimestamp", () -> addError("must be non-null"));
    if (activityInstance.type == null) with("type", () -> addError("must be non-null"));
  }

  public void validateActivityList(final Collection<ActivityInstance> activityInstances) {
    int index = 0;
    for (final ActivityInstance activityInstance : activityInstances) {
      with(index++, () -> validateActivity(activityInstance));
    }
  }

  public void validateActivityMap(final Map<String, ActivityInstance> activityInstances) {
    for (final var entry : activityInstances.entrySet()) {
      final String activityId = entry.getKey();
      final ActivityInstance activityInstance = entry.getValue();

      with(activityId, () -> validateActivity(activityInstance));
    }
  }

  public void validateNewPlan(final NewPlan plan) {
    if (plan.name == null) with("name", () -> addError("must be non-null"));
    if (plan.startTimestamp == null) with("startTimestamp", () -> addError("must be non-null"));
    if (plan.endTimestamp == null) with("endTimestamp", () -> addError("must be non-null"));
    if (plan.adaptationId == null) with("adaptationId", () -> addError("must be non-null"));
    if (plan.activityInstances != null) with("activityInstances", () -> validateActivityList(plan.activityInstances));
  }

  public void validatePlanPatch(final String planId, final Plan patch) throws NoSuchPlanException {
    if (patch.activityInstances != null) {
      final Set<String> validActivityIds = this.planRepository
          .getAllActivitiesInPlan(planId)
          .map(Pair::getKey)
          .collect(Collectors.toSet());

      with("activityInstances", () -> {
        for (final String activityId : patch.activityInstances.keySet()) {
          if (!validActivityIds.contains(activityId)) {
            with(activityId, () -> addError("no activity with id in plan"));
          }
        }

        validateActivityMap(patch.activityInstances);
      });
    }
  }

  public List<Pair<List<Breadcrumb>, String>> getMessages() {
    return List.copyOf(this.messages);
  }

  private void addError(final String message) {
    this.messages.add(Pair.of(unmodifiableList(this.breadcrumbCursor.getPath()), message));
  }

  private void with(final int index, final Runnable block) {
    this.breadcrumbCursor.descend(index);
    try {
      block.run();
    } finally {
      this.breadcrumbCursor.ascend();
    }
  }

  private void with(final String index, final Runnable block) {
    this.breadcrumbCursor.descend(index);
    try {
      block.run();
    } finally {
      this.breadcrumbCursor.ascend();
    }
  }
}
