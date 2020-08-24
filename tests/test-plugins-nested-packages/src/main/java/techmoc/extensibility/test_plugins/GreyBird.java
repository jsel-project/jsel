package techmoc.extensibility.test_plugins;

import techmoc.extensibility.test_interfaces.Bird;


public class GreyBird implements Bird {

  @Override
  public String chirp() {
    return "grey";
  }
}
