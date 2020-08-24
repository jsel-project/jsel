package techmoc.extensibility.test_plugins.red;

import techmoc.extensibility.test_interfaces.Bird;


public class RedBird implements Bird {

  @Override
  public String chirp() {
    return "red";
  }
}
