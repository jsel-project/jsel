package techmoc.extensibility.test_plugins.blue.teal;

import techmoc.extensibility.test_interfaces.Bird;


public class TealBird implements Bird {

  @Override
  public String chirp() {
    return this.getClass().getSimpleName();
  }
}
