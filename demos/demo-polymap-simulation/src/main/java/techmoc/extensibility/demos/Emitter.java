package techmoc.extensibility.demos;

import java.awt.*;

import static techmoc.extensibility.demos.DemoColors.darkYellow;
import static techmoc.extensibility.demos.DemoColors.scannerGray;

public class Emitter implements Updatable, Renderable {

  private final double MIN_FREQUENCY = 12.0;
  private final double MAX_FREQUENCY = 125.0;
  private double beamAngle = 1.0;
  private int beamWidth = 25;
  private double transmitterTemperature = 20.0;
  private double temperatureStep = 1.0;
  private TemperatureRange transmitterCondition = TemperatureRange.COLD;
  private int x;
  private int y;
  private int iconRadius = 25;
  private double powerMilliwatts = 1.0;
  private double frequencyDirection = 1.0;
  private double frequencyMHz = MIN_FREQUENCY;
  private long frameCount = 0;
  private int frameNumber = 0;
  private double powerMultiplier = 1.0;

  Double getPowerMilliwatts() {
    return powerMilliwatts;
  }

  double getFrequencyMHz() {
    return frequencyMHz;
  }

  @Override
  public void setPosition(int x, int y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public void render(Graphics2D graphics, float interpolation) {
    graphics.setColor(darkYellow);
    graphics.fillOval(x, y, iconRadius, iconRadius);
    graphics.setColor(scannerGray);
    int scale = 10;
    int correction = (-scale * iconRadius) / 2 + (iconRadius / 2);
    graphics.fillArc(x + correction, y + correction, scale * iconRadius, scale * iconRadius, (int) beamAngle, beamWidth / 2);
    graphics.fillArc(x + correction, y + correction, scale * iconRadius, scale * iconRadius, (int) beamAngle, -beamWidth / 2);
  }

  public boolean isCold() {
    return transmitterCondition == TemperatureRange.COLD;
  }

  public boolean isNormal() {
    return transmitterCondition == TemperatureRange.NORMAL;
  }

  public boolean isHot() {
    return transmitterCondition == TemperatureRange.HOT;
  }

  public String getTransmitterStatus() {
    switch (transmitterCondition) {
      case HOT:
        return "WARNING";
      case NORMAL:
        return "NORMAL";
      case COLD:
        return "COLD";
      default:
        return "UNKNOWN";
    }
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public double getBeamAngle() {
    return beamAngle;
  }

  public int getBeamWidth() {
    return beamWidth;
  }

  public Double getTransmitterTemperature() {
    return transmitterTemperature;
  }

  @Override
  public void update() {

    computeBeamAngle();
    computeFrequency();
    computePower();
    computeTemperatureState();

    frameCount++;
    frameNumber += (frameNumber + 1) % Application.FRAMES_PER_SECOND;

  }

  private void computePower() {
    double powerBandWidth = (MAX_FREQUENCY - MIN_FREQUENCY) / 5.0;

    if (frequencyMHz <= MIN_FREQUENCY + powerBandWidth) {
      powerMilliwatts = .275 * frequencyMHz;
      powerMultiplier = .75;
    } else if (frequencyMHz <= MIN_FREQUENCY + 2 * powerBandWidth) {
      powerMilliwatts = .343 * frequencyMHz;
      powerMultiplier = 1.0;
    } else if (frequencyMHz <= MIN_FREQUENCY + 3 * powerBandWidth) {
      powerMilliwatts = .673 * frequencyMHz;
      powerMultiplier = 1.25;
    } else if (frequencyMHz <= MIN_FREQUENCY + 4 * powerBandWidth) {
      powerMilliwatts = .989 * frequencyMHz;
      powerMultiplier = 1.45;
    } else {
      powerMilliwatts = 1.2 * frequencyMHz;
      powerMultiplier = 2.0;
    }
  }

  private void computeFrequency() {

    double frequencyStep = .375;

    frequencyMHz += frequencyStep * frequencyDirection;
    if (frequencyMHz >= MAX_FREQUENCY) {
      frequencyDirection *= -1.0;
      frequencyMHz = MAX_FREQUENCY;
    }
    if (frequencyMHz <= MIN_FREQUENCY) {
      frequencyDirection *= -1.0;
      frequencyMHz = MIN_FREQUENCY;
    }
  }

  private void computeBeamAngle() {
    beamAngle = (beamAngle + .25) % 360;
  }

  private void computeTemperatureState() {

    if (frameCount % 300 == 0) {
      transmitterTemperature += temperatureStep;
    }

    if (transmitterTemperature < 35.0) {
      transmitterCondition = TemperatureRange.COLD;
      temperatureStep = 2.5 * powerMultiplier;
    } else if (transmitterTemperature <= 65.0) {
      transmitterCondition = TemperatureRange.NORMAL;
      temperatureStep = 7.5 * powerMultiplier;
    } else if (transmitterTemperature > 85.0) {
      temperatureStep = 15 * powerMultiplier;
      transmitterCondition = TemperatureRange.HOT;
      transmitterTemperature = Math.min(transmitterTemperature, 125.);
    }

  }

  private enum TemperatureRange {COLD, NORMAL, HOT}
}
