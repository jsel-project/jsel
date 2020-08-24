package techmoc;

import techmoc.extensibility.interfaces.Fly;


public class BumbleBee implements Fly {

  @Override
  public String buzz() {
    return this.getClass().getSimpleName();
  }
}
