package techmoc.extensibility.test_plugins;

import techmoc.extensibility.test_interfaces.Bird;


public class Parakeet implements Bird {

  @Override
  public String chirp() {
    return "Tweet!";
  }
}
