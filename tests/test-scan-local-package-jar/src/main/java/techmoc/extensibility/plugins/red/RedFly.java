package techmoc.extensibility.plugins.red;

import techmoc.extensibility.interfaces.Fly;


public class RedFly implements Fly {

  @Override
  public String buzz() {
    return this.getClass().getSimpleName();
  }
}
