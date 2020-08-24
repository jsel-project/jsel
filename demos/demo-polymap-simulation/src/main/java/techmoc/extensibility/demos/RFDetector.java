package techmoc.extensibility.demos;

import techmoc.extensibility.polymorphicmap.PolymorphicMap;

import java.awt.*;

abstract class RFDetector implements Updatable, Renderable {

  protected double frequency;
  protected String frequencyUnits;
  protected double power;
  protected String powerUnits;
  protected String signalType;
  protected int x;
  protected int y;
  boolean detected;

  public abstract void detect(PolymorphicMap emission);

  abstract public PolymorphicMap getDetections();

  @Override
  abstract public void render(Graphics2D graphics, float interpolation);

  @Override
  abstract public void update();

  abstract public String getName();

  public boolean detected() {
    return detected;
  }

  @Override
  public void setPosition(int x, int y) {
    this.x = x;
    this.y = y;
  }

}
