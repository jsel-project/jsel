package techmoc.extensibility.pluginlibrary.test_objects;

import techmoc.extensibility.pluginlibrary.PluginVersion;


public class Poodle201 implements Dog {

  @Override
  public String getPluginName() {
    return "Poodle";
  }

  @Override
  public PluginVersion getPluginVersion() {
    return new PluginVersion(2, 0, 1);
  }

  public String bark() {
    return "Yap yap!!!";
  }
}
