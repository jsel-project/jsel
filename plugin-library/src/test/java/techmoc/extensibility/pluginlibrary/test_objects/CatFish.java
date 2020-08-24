package techmoc.extensibility.pluginlibrary.test_objects;


import techmoc.extensibility.pluginlibrary.PluginVersion;
import techmoc.extensibility.pluginlibrary.test_objects.some_plugin_interfaces.Cat;

public class CatFish implements Cat, Fish {

  @Override
  public String meow() {
    return bubble();
  }

  @Override
  public String bubble() {
    return "bubble";
  }

  @Override
  public PluginVersion getPluginVersion() {
    return new PluginVersion(1, 2, 345);
  }
}
