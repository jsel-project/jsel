package techmoc.extensibility.test_plugins;

import techmoc.extensibility.test_interfaces.Bird;


public class Raven implements Bird {

  @Override
  public String chirp() {
    return this.getClass().getSimpleName();
  }
}
