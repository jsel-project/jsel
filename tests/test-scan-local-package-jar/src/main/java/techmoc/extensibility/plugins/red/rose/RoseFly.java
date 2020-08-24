package techmoc.extensibility.plugins.red.rose;

import techmoc.extensibility.interfaces.Fly;


public class RoseFly implements Fly {

  @Override
  public String buzz() {
    return this.getClass().getSimpleName();
  }
}
