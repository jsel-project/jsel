package techmoc.extensibility.plugins.blue;

import techmoc.extensibility.interfaces.Fly;


public class BlueFly implements Fly {

  @Override
  public String buzz() {
    return this.getClass().getSimpleName();
  }
}
