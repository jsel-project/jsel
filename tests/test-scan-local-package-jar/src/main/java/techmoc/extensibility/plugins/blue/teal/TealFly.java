package techmoc.extensibility.plugins.blue.teal;

import techmoc.extensibility.interfaces.Fly;


public class TealFly implements Fly {

  @Override
  public String buzz() {
    return this.getClass().getSimpleName();
  }
}
