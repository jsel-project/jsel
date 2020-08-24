package techmoc.extensibility.demos;

import static org.apache.logging.log4j.util.LambdaUtil.getAll;
import static spark.Spark.awaitInitialization;
import static spark.Spark.get;
import static spark.Spark.port;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import spark.Request;
import spark.Response;
import techmoc.extensibility.pluginlibrary.PluginRegistry;


public class Application {

  // Plugin Registry.
  private static final PluginRegistry pluginRegistry = new PluginRegistry();

  /**
   * Starts the Directory Monitor and the Web Server.
   *
   * @param args Path to the directory monitor's target directory (optional).
   * @throws IOException Web server cannot be started.
   */
  public static void main(String[] args) throws IOException {
    // Initialize the Plugin Registry.
    pluginRegistry.registerPluginInterfaces(Webpage.class);

    // Initialize the directory monitor.
    pluginRegistry.startDirectoryMonitor(validatePath(args));

    // Initialize the SparkJava web server.
    port(8080);

    // Root, where the plugin to run is specified
    get("/", Application::webpages);

    // Displays the table of plugins as plain text
    get("/plugins/text", Application::pluginsText);

    // Displays the table of plugins as an HTML page that periodically refreshes.
    get("/plugins", Application::pluginsHtml);

    awaitInitialization();
  }

  /**
   * Displays web pages loaded dynamically from Plugin JARs.
   *
   * @param request Spark request.
   * @param response Spark response.
   * @return Page content.
   */
  private static Object webpages(Request request, Response response) {

    // Check if the input is valid, and whether the plugin exists in the registry.
    String pluginName = request.queryParams("name");
    System.out.println(request.contextPath());
    if (pluginName == null ||
        pluginName.isBlank() ||
        !pluginRegistry.isRegisteredPlugin(pluginName)) {

      // Send response.
      response.status(400);
      return renderPage("Page not found.");
    }

    // Load the appropriate plugin.
    Webpage webpage = pluginRegistry.getLatestVersion(pluginName, Webpage.class);

    // Set the response type.
    response.type(webpage.getContentType());

    // Send response.
    return renderPage(webpage.getPageContent());
  }

  /**
   * Displays the plugins that are available as a plain-text table.
   *
   * @param request
   * @param response
   * @return
   */
  private static Object pluginsText(Request request, Response response) {

    response.type("text/plain");

    return pluginRegistry.toRegistryState();
  }

  /**
   * Displays the plugins that are available as an HTML table. The page also refreshes periodically
   *
   * @param request
   * @param response
   * @return
   */
  private static Object pluginsHtml(Request request, Response response) {

    List<Webpage> webpages = pluginRegistry.getAll(Webpage.class);

    StringBuffer htmlBuffer = new StringBuffer();
    String html =
        "<html>"
            // Refresh periodically
            + "<script>setTimeout(\"location.reload(true);\",1000);</script>"
            + "<table style=\"width:30%\">\n"
            + "  <tr>\n"
            + "    <th>Plugin Interface</th>\n"
            + "    <th>Plugin</th>\n"
            + "  </tr>";

    htmlBuffer.append(html);

    webpages.forEach(webpage -> {

      htmlBuffer.append(
          "  <tr>\n"
              + "    <td>Webpage</td>\n"
              + "    <td>" + webpage.getPluginName() + "</td>\n"
              + "  </tr>");
    });

    htmlBuffer.append("</table></html>");

    response.type("text/html");

    return htmlBuffer.toString();
  }

  private static String renderPage(String content) {
    // This method doesnt do much now that it no longer adds the table of plugins (which is now
    // displayed via /plugins and /plugins/text.
    //
    // Maybe its not needed or maybe there is some common functionallity that it can take over.
    return content;
  }

  /**
   * Extracts the directory monitor path from command-line args, or returns the default path.
   *
   * @param args Command-line arguments.
   * @return Directory monitor path.
   */
  private static String validatePath(String[] args) throws IOException {
    if (args.length <= 0) {
      Path rootDir = Paths.get("/tmp/webpages-at-runtime-demo/");
      if (!Files.exists(rootDir)) {
        Files.createDirectory(rootDir);
      }
      return rootDir.toString();
    } else if (args.length > 1) {
      throw new IllegalArgumentException(String.format(
          "Command-line contains too many arguments (expected 1, received %d", args.length));
    }

    // Check that the path exists.
    if (!Files.exists(Paths.get(args[0]))) {
      throw new IllegalArgumentException(String.format(
          "Directory monitor path specified does not exist %s.", args[0]));
    }

    return args[0];
  }
}
