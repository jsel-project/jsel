package techmoc.extensibility;

import techmoc.extensibility.test_interfaces.Bird;


public class GreyBird implements Bird {

  @Override
  public String chirp() {
    return this.getClass().getSimpleName();
  }
}
