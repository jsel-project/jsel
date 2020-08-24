package techmoc.extensibility.plugins.red.pink;

import techmoc.extensibility.interfaces.Fly;


public class PinkFly implements Fly {

  @Override
  public String buzz() {
    return this.getClass().getSimpleName();
  }
}
