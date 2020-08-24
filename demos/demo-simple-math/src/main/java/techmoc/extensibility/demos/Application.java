package techmoc.extensibility.demos;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import techmoc.extensibility.pluginlibrary.PluginRegistry;


public class Application {

  public static void main(String[] args)
      throws ClassNotFoundException, IOException {

    PluginRegistry pr = new PluginRegistry();
    pr.registerPluginInterfaces(MathOperationInterface.class);
    pr.scan();
    List<MathOperationInterface> plugins = pr.getAll(MathOperationInterface.class);

    System.out.println("\n\n\n----------------------------------------");
    for (MathOperationInterface plugin : plugins) {
      System.out.println("Plugin: " + plugin.getPluginName());

      double result = plugin.calculate(10, 4);
      System.out.println("calculate(10, 4): " + result);
    }
  }
}
