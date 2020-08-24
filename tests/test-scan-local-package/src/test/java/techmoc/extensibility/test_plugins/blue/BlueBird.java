package techmoc.extensibility.test_plugins.blue;

import techmoc.extensibility.test_interfaces.Bird;


public class BlueBird implements Bird {

  @Override
  public String chirp() {
    return this.getClass().getSimpleName();
  }
}
