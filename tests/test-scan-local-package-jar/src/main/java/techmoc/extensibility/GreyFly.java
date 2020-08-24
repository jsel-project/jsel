package techmoc.extensibility;

import techmoc.extensibility.interfaces.Fly;


public class GreyFly implements Fly {

  @Override
  public String buzz() {
    return this.getClass().getSimpleName();
  }
}
