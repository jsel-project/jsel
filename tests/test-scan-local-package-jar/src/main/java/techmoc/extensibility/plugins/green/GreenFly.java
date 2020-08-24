package techmoc.extensibility.plugins.green;

import techmoc.extensibility.interfaces.Fly;


public class GreenFly implements Fly {

  @Override
  public String buzz() {
    return this.getClass().getSimpleName();
  }
}
