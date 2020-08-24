package techmoc.extensibility.plugins;

import techmoc.extensibility.interfaces.Fly;


public class HorseFly implements Fly {

  @Override
  public String buzz() {
    return this.getClass().getSimpleName();
  }
}
