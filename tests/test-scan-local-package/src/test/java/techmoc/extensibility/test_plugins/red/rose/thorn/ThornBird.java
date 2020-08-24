package techmoc.extensibility.test_plugins.red.rose.thorn;

import techmoc.extensibility.test_interfaces.Bird;


public class ThornBird implements Bird {

  @Override
  public String chirp() {
    return this.getClass().getSimpleName();
  }
}
