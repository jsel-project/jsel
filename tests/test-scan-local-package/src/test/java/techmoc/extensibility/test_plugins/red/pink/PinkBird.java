package techmoc.extensibility.test_plugins.red.pink;

import techmoc.extensibility.test_interfaces.Bird;


public class PinkBird implements Bird {

  @Override
  public String chirp() {
    return this.getClass().getSimpleName();
  }
}
