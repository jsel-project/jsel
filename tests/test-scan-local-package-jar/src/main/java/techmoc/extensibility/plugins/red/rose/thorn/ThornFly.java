package techmoc.extensibility.plugins.red.rose.thorn;

import techmoc.extensibility.interfaces.Fly;


public class ThornFly implements Fly {

  @Override
  public String buzz() {
    return this.getClass().getSimpleName();
  }
}
