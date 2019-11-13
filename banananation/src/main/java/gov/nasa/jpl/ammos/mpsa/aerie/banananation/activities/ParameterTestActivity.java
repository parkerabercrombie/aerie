package gov.nasa.jpl.ammos.mpsa.aerie.banananation.activities;

import gov.nasa.jpl.ammos.mpsa.aerie.banananation.state.BananaStates;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.activities.Activity;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.activities.annotations.ActivityType;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.activities.annotations.Parameter;

@ActivityType(name="ParameterTest", states=BananaStates.class)
public class ParameterTestActivity implements Activity<BananaStates> {
  @Parameter public double a = 3.141;
  @Parameter public float b = 1.618f;
  @Parameter public byte c = 16;
  @Parameter public short d = 32;
  @Parameter public int e = 64;
  @Parameter public long f = 128;
  @Parameter public char g = 'g';
  @Parameter public String h = "h";
}