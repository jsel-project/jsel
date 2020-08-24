package techmoc.extensibility.test_plugins.green;

import techmoc.extensibility.test_interfaces.Bird;


public class GreenBird implements Bird {

  @Override
  public String chirp() {
    return "green";
  }
}
