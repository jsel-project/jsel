package techmoc.testing;

import techmoc.extensibility.test_interfaces.Bird;


public class Bluejay implements Bird {

  @Override
  public String chirp() {
    return "Chirp!";
  }
}
