package gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.constraints;

import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.time.Window;

import java.util.List;
import java.util.Set;

public final class ConstraintViolation {
    public final String id;
    public final String name;
    public final String message;
    public final String category;
    public final Set<String> associatedActivityIds;
    public final Set<String> associatedStateIds;
    public final List<Window> violationWindows;

    public ConstraintViolation(List<Window> violationWindows, ViolableConstraint violableConstraint) {
        this.violationWindows = List.copyOf(violationWindows);
        this.id = violableConstraint.id;
        this.name = violableConstraint.name;
        this.message = violableConstraint.message;
        this.category = violableConstraint.category;
        this.associatedActivityIds = Set.copyOf(violableConstraint.getActivityIds());
        this.associatedStateIds = Set.copyOf(violableConstraint.getStateIds());
    }
}