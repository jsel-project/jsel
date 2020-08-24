package techmoc.extensibility.demos;

import techmoc.extensibility.polymorphicmap.PolymorphicMap;

import java.awt.*;

import static techmoc.extensibility.demos.DemoColors.*;
import static techmoc.extensibility.demos.DetectionConstants.*;

public class HawkeyeDetector implements Renderable, Updatable {
  public static final String RF_IR_ACQUIRED = "RFIR";
  public static final String IR_ACQUIRED = "IR";
  public static final String NO_ACQUISITION = "None";
  private final double MIN_DETECTABLE_POWER = 1.0;
  private final double MIN_DETECTABLE_TEMPERATURE = 34.0;
  private int x;
  private int y;
  private int iconRadius = 15;
  private PolymorphicMap emission;
  private boolean positiveScan;
  private double frequency;
  private double power;
  private String frequencyUnits;
  private String powerUnits;
  private String signalType;
  private String temperature;

  @Override
  public void render(Graphics2D graphics, float interpolation) {
    graphics.setColor(scannerGray);
    /* Draw the detection fan */
    int scale = 12;
    int correction = -(scale * iconRadius) / 2 + (iconRadius / 2);
    graphics.fillArc(x + correction, y + correction, scale * iconRadius, scale * iconRadius, 0, 360);


    if (positiveScan && isTargeted()) {
      graphics.setColor(detectionGreen);
    } else if (positiveScan) {
      graphics.setColor(darkYellow);
    } else {
      graphics.setColor(graphite);
    }
    int iconCorrection =  (iconRadius / 2) + 2;
    graphics.fillOval(x - iconCorrection, y + iconCorrection, iconRadius, iconRadius);
    graphics.fillOval(x + iconCorrection, y + iconCorrection, iconRadius, iconRadius);
    graphics.fillOval(x - iconCorrection, y - iconCorrection, iconRadius, iconRadius);
    graphics.fillOval(x + iconCorrection, y - iconCorrection, iconRadius, iconRadius);

  }

  /**
   * Allows a parent in the rendering process to assign coordinates for children to be rendered on demand.
   *
   * @param x X screen coordinate
   * @param y Y screen coordinate
   */
  @Override
  public void setPosition(int x, int y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public void update() {
    Double detectedPower = emission.get(POWER, Double.class);
    Double detectedTemperature = emission.get(TEMPERATURE, Double.class);
    /* Only detected if within frequency limits and pointed at */
    positiveScan = detectedPower > MIN_DETECTABLE_POWER && detectedTemperature > MIN_DETECTABLE_TEMPERATURE;
    compileScanResults();
  }

  public PolymorphicMap getScanResult() {
    PolymorphicMap scanResult = new PolymorphicMap();
    scanResult.put("ScanResult", positiveScan);

    if (positiveScan) {
      scanResult.put(FREQUENCY, frequency);
      scanResult.put(FREQUENCY_UNITS, frequencyUnits);
      scanResult.put(POWER, power);
      scanResult.put(POWER_UNITS, powerUnits);
      scanResult.put(SIGNAL_TYPE, signalType);
      scanResult.put(TEMPERATURE, temperature);
    }
    return scanResult;
  }

  private void compileScanResults() {
    String emissionUnits = emission.get(FREQUENCY_UNITS, String.class);
    Double emissionFrequency = emission.get(FREQUENCY, Double.class);

    double scaleFactor = .2;
    /* HawkEye will still detect if not targeted, but only a small amount */
    if (isTargeted()) {
      scaleFactor = 1.0;
      signalType = "RF-IR";
    } else {
      signalType = "IR";
    }
    switch (emissionUnits) {
      case HERTZ:
        frequency = emissionFrequency * 1e-6;
        break;
      case KILOHERTZ:
        frequency = emissionFrequency * 1e-3;
        break;
      case MEGAHERTZ:
        frequency = emissionFrequency;
        break;
      case GIGAHERTZ:
        frequency = emissionFrequency * 1e3;
        break;
    }

    /* Pack the values for the detection */
    frequencyUnits = MEGAHERTZ;
    power = scaleFactor * emission.get(POWER, Double.class);
    powerUnits = emission.get(POWER_UNITS, String.class);

    Double temperature = emission.get(TEMPERATURE, Double.class);
    if (temperature < MIN_DETECTABLE_TEMPERATURE) {
      this.temperature = "COLD";
    } else if (temperature < 100.0) {
      this.temperature = "NORMAL";
    } else {
      this.temperature = "HOT";
    }

  }

  private boolean isTargeted() {
    Integer emissionX = emission.get(EMITTER_X, Integer.class);
    Integer emissionY = emission.get(EMITTER_Y, Integer.class);

    /* Figure out a relative bearing to the emission source */
    Integer deltaX = this.x - emissionX;
    Integer deltaY = this.y - emissionY;
    Double beamAngle = emission.get(BEAM_ANGLE, Double.class);
    if (beamAngle == null) {
      beamAngle = Double.MAX_VALUE;
    }

    /* Determine any part of the beam is pointing towards the detector */
    double detectorCenter;
    final double detectorWidth = 45.0;
    if (deltaX <= 0 && deltaY < 0) {
      /* North Detector */
      detectorCenter = 90.0;
      return (beamAngle > detectorCenter - detectorWidth) && (beamAngle < detectorCenter + detectorWidth);
    } else if (deltaX <= 0 && deltaY > 0) {
      /* South Detector */
      detectorCenter = 270.0;
      return (beamAngle > detectorCenter - detectorWidth) && (beamAngle < detectorCenter + detectorWidth);
    } else if (deltaX > 0 && deltaY >= 0) {
      /* East Detector */
      detectorCenter = 0.0;
      return (beamAngle > detectorCenter - detectorWidth + 360.0) || (beamAngle < detectorCenter + detectorWidth);
    } else if (deltaX < 0 && deltaY >= 0) {
      /* West Detector */
      detectorCenter = 180.0;
      return (beamAngle > detectorCenter - detectorWidth) && (beamAngle < detectorCenter + detectorWidth);
    } else {
      return false;
    }

  }

  public void scan(PolymorphicMap emission) {
    this.emission = emission;
  }

  public void setLocation(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public String getScanState() {
    if (positiveScan && isTargeted()) {
      return RF_IR_ACQUIRED;
    }
    if (positiveScan) {
      return IR_ACQUIRED;
    }
    return NO_ACQUISITION;
  }

  public String getIdent() {
    return "HawkEye-1000";
  }
}
