package techmoc.testing;

import techmoc.extensibility.test_interfaces.Bird;


public class Woodpecker implements Bird {

  @Override
  public String chirp() {
    return "Tap tap tap!";
  }
}
