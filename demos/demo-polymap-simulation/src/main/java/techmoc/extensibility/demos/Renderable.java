package techmoc.extensibility.demos;

import java.awt.*;

public interface Renderable {
  void render(Graphics2D graphics, float interpolation);

  /**
   * Allows a parent in the rendering process to assign coordinates for children to be rendered on demand.
   * @param x X screen coordinate
   * @param y Y screen coordinate
   */
  default public void setPosition(int x, int y){ }
}
