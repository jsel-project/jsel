package techmoc;

import techmoc.extensibility.test_interfaces.Bird;


public class Dove implements Bird {

  @Override
  public String chirp() {
    return this.getClass().getSimpleName();
  }
}
