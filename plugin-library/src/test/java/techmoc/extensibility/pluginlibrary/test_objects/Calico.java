package techmoc.extensibility.pluginlibrary.test_objects;

import techmoc.extensibility.pluginlibrary.test_objects.some_plugin_interfaces.Cat;
import techmoc.extensibility.polymorphicmap.PolymorphicMap;


public class Calico implements Cat {

  @Override
  public String meow() {
    return "meow";
  }

  @Override
  public void initializePluginAttributes(PolymorphicMap pluginAttributes) {
    pluginAttributes.put("HasGreyWhiskers", true);
    pluginAttributes.put("HasPuffyTail", false);
    pluginAttributes.put("MeowVolume", 3);
  }
}
