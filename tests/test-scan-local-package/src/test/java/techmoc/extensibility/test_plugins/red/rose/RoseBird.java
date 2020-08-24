package techmoc.extensibility.test_plugins.red.rose;

import techmoc.extensibility.test_interfaces.Bird;


public class RoseBird implements Bird {

  @Override
  public String chirp() {
    return this.getClass().getSimpleName();
  }
}
