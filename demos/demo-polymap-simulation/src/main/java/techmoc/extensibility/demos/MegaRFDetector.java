package techmoc.extensibility.demos;

import techmoc.extensibility.polymorphicmap.PolymorphicMap;

import java.awt.*;

import static techmoc.extensibility.demos.DemoColors.*;
import static techmoc.extensibility.demos.DetectionConstants.*;

public class MegaRFDetector extends RFDetector {

  final double detectorWidth = 45.0;
  private final double MIN_DETECT_FREQUENCY = 0.5;
  private final double MAX_DETECT_FREQUENCY = 145.5;
  private int iconWidth = 25;
  private int iconHeight = 25;
  private PolymorphicMap emission;
  private String radarBand = "None";
  private int detectorFanAngle = 0;

  @Override
  public void detect(PolymorphicMap emission) {
    this.emission = emission;
  }


  @Override
  public void render(Graphics2D graphics, float interpolation) {

    graphics.setColor(scannerGray);
    /* Draw the detection fan */
    int scale = 8;
    int wCorrection = -(scale * iconWidth) / 2 + (iconWidth / 2);
    int hCorrection = -(scale * iconHeight) / 2 + (iconHeight / 2);
    graphics.fillArc(x + wCorrection, y + hCorrection, scale * iconWidth, scale * iconHeight, detectorFanAngle, (int) (detectorWidth / 2));
    graphics.fillArc(x + wCorrection, y + hCorrection, scale * iconWidth, scale * iconHeight, detectorFanAngle, (int) (-detectorWidth / 2));


    if (detected) {
      graphics.setColor(detectionGreen);
    } else {
      graphics.setColor(graphite);
    }
    graphics.fillRect(x, y, iconWidth, iconHeight);
    graphics.fillOval(x - 10, y - 1, iconWidth + 20, iconHeight - 10);

  }

  @Override
  public void update() {
    Double detectedFrequency = emission.get(FREQUENCY, Double.class);
    /* Only detected if within frequency limits and pointed at */
    detected = detectedFrequency >= MIN_DETECT_FREQUENCY && detectedFrequency <= MAX_DETECT_FREQUENCY && isTargeted();
    packDetectionReport();
  }


  private void packDetectionReport() {

    String emissionUnits = emission.get(FREQUENCY_UNITS, String.class);
    Double emissionFrequency = emission.get(FREQUENCY, Double.class);
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
    power = emission.get(POWER, Double.class);
    powerUnits = emission.get(POWER_UNITS, String.class);
    signalType = "RF";

    if (emission.containsKey(RADAR_BAND)) {
      radarBand = emission.get(RADAR_BAND, String.class);
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

    if (deltaX <= 0 && deltaY < 0) {
      /* North Detector */
      detectorCenter = 90.0;
      detectorFanAngle = 270;
      return (beamAngle > detectorCenter - detectorWidth) && (beamAngle < detectorCenter + detectorWidth);
    } else if (deltaX <= 0 && deltaY > 0) {
      /* South Detector */
      detectorCenter = 270.0;
      detectorFanAngle = 90;
      return (beamAngle > detectorCenter - detectorWidth) && (beamAngle < detectorCenter + detectorWidth);
    } else if (deltaX > 0 && deltaY >= 0) {
      /* East Detector */
      detectorCenter = 0.0;
      detectorFanAngle = 180;
      return (beamAngle > detectorCenter - detectorWidth + 360.0) || (beamAngle < detectorCenter + detectorWidth);
    } else if (deltaX < 0 && deltaY >= 0) {
      /* West Detector */
      detectorCenter = 180.0;
      detectorFanAngle = 0;
      return (beamAngle > detectorCenter - detectorWidth) && (beamAngle < detectorCenter + detectorWidth);
    } else {
      return false;
    }

  }

  @Override
  public String getName() {
    return "MEGA Detector";
  }

  @Override
  public PolymorphicMap getDetections() {
    PolymorphicMap detections = new PolymorphicMap();
    detections.put("Detection", detected);

    if (detected) {
      detections.put(FREQUENCY, frequency);
      detections.put(FREQUENCY_UNITS, frequencyUnits);
      detections.put(POWER, power);
      detections.put(POWER_UNITS, powerUnits);
      detections.put(SIGNAL_TYPE, signalType);
      detections.put(RADAR_BAND, radarBand);
    }
    return detections;
  }
}
