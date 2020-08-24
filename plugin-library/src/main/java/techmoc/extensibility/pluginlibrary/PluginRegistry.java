package techmoc.extensibility.pluginlibrary;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.Thread.State;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.stream.Collectors;
import techmoc.extensibility.polymorphicmap.PolymorphicMap;


/**
 * Plugin Registry.
 */
public final class PluginRegistry {

  /**
   * Plugin Registry.
   */
  private final ConcurrentSkipListMap<Class<? extends Pluggable>, TreeSet<PluginEntry>>
      pluginRegistry = new ConcurrentSkipListMap<>(Comparator.comparing(Class::getSimpleName));

  /**
   * Directory Monitor service.
   */
  private DirectoryMonitoringThread directoryMonitor = new DirectoryMonitoringThread();

  /**
   * Directory monitoring thread.
   */
  private final class DirectoryMonitoringThread implements Runnable {

    // Watcher service.
    private WatchService watcherService = null;
    File targetDirectory = null;

    // Thread properties (set and reset by the start() method).
    private Thread thread;
    private AtomicBoolean shutdownFlag = new AtomicBoolean(false);
    private CountDownLatch threadIsInitialized = new CountDownLatch(1);
    private AtomicReference<String> lastErrorMessage = new AtomicReference<>(null);

    //---------- Constructor and Shutdown Handler ----------//

    public DirectoryMonitoringThread() {
      // Configure the thread.
      this.configureNewThread();

      // Register the shutdown handler.
      Runnable shutdownHook = () -> {
        if (this.isRunning()) {
          // Stop the thread.
          this.stop();
        }
      };
      Runtime.getRuntime().addShutdownHook(new Thread(shutdownHook));
    }

    private void configureNewThread() {
      thread = new Thread(this, DirectoryMonitoringThread.class.getSimpleName());
      thread.setUncaughtExceptionHandler(this::uncaughtExceptionHandler);
    }

    public final void start() {
      // Check if the thread is already running.
      if (this.isRunning()) {
        throw new IllegalThreadStateException("Thread is already running.");
      }

      // Reset the state of the MonitorThread.
      shutdownFlag.set(false);
      threadIsInitialized = new CountDownLatch(1);
      lastErrorMessage.set(null);

      // Reconfigure the thread (if it has already been started once before).
      if (!thread.getState().equals(State.NEW)) {
        configureNewThread();
      }

      // Run the onBeforeStart() actions.
      if (watcherService == null) {
        throw new IllegalThreadStateException("Thread not ready to be started.");
      }

      // Start the thread.
      thread.start();
    }

    @Override
    public final void run() {
      try {
        // Indicate that the thread is initialized.
        this.threadIsInitialized.countDown();

        while (!shutdownFlag.get()) {
          try {
            // Wait for key to be signaled.
            WatchKey key = watcherService.take();

            for (WatchEvent<?> event : key.pollEvents()) {
              // This key is registered only for ENTRY_CREATE events, but an OVERFLOW event can occur
              // regardless if events are lost or discarded.
              if (event.kind() == OVERFLOW) {
                continue;
              }

              // The filename is the context of the event.
              @SuppressWarnings("unchecked")
              File eventContext = ((WatchEvent<Path>) event).context().toFile();
              File newFile = new File(
                  targetDirectory.getAbsolutePath() + "/" + eventContext.toString());

              // Check the file type.
              if (newFile.isDirectory()) {
                // Scan the newly added package directory.
                for (Class<? extends Pluggable> registeredPluginInterface
                    : pluginRegistry.keySet()) {
                  scanPackageDirectory(newFile.getAbsolutePath(), registeredPluginInterface);
                }
              } else if (newFile.getName().endsWith(".jar")) {
                // Scan the newly added JAR file.
                for (Class<? extends Pluggable> registeredPluginInterface : pluginRegistry
                    .keySet()) {
                  try {
                    scanJar(newFile.getAbsolutePath(), registeredPluginInterface);
                  } catch (IOException e) {
                    throw new IllegalStateException(String.format(
                        "FATAL ERROR: IOException exception thrown by registered Plugin Interface [%s]: %s",
                        registeredPluginInterface.getCanonicalName(),
                        e.getMessage()));
                  }
                }
              } else {
                debug(String.format("Ignoring new file detected by the DirectoryMonitorThread, " +
                        "which is neither a package directory nor a JAR file [%s].",
                    newFile.getPath()));
              }
            }

            // Reset the key -- this step is critical if you want to receive further watch events.
            // If the key is no longer valid, the directory is inaccessible so exit the loop.
            if (!key.reset()) {
              break;
            }
          } catch (InterruptedException e) {
            // Do nothing.
          }
        }

      } catch (Exception e) {
        this.uncaughtExceptionHandler(thread, e);
      }
    }

    public final void stop() {
      // Interrupt the thread, if configured to do so.
      this.interruptRunningThread();

      // Only allow the onStop() method to be called once, after a thread has been started.
      if (!this.isRunning() || shutdownFlag.get()) {
        // Ignore this call, since the stop() method has already been run once.
        return;
      }

      // Set the shutdownFlag flag.
      shutdownFlag.set(true);

      // Reset the watch service and target directory.
      if (watcherService != null) {
        try {
          watcherService.close();
        } catch (Exception e) {
          // Do nothing.
        } finally {
          watcherService = null;
        }
      }
    }

    protected final void interruptRunningThread() {
      if (this.isRunning()) {
        // Cause all blocking operations to throw an InterruptedException.
        thread.interrupt();
      }
    }

    public final boolean isRunning() {
      return thread.isAlive();
    }

    public final boolean hasErrorMessage() {
      return (lastErrorMessage.get() != null);
    }

    public final String getErrorMessage() {
      return lastErrorMessage.get();
    }

    protected final void setErrorMessage(String errorMessage) {
      lastErrorMessage.set(errorMessage);
    }

    private void uncaughtExceptionHandler(Thread thread, Throwable throwable) {
      // Set the last error message.
      String errMsg = String.format(
          "Uncaught Exception Thrown by thread %s: %s",
          thread.getName(), throwable.getMessage());
      if (this.hasErrorMessage()) {
        errMsg = String.format(
            "%s\n----------\n%s", errMsg, this.getErrorMessage());
      }
      this.setErrorMessage(errMsg);
    }

    public final void waitUntilThreadInitializes() {
      if (this.isRunning() && threadIsInitialized.getCount() > 0) {
        try {
          threadIsInitialized.await();
        } catch (InterruptedException e) {
          // Break out of the blocking call.
        }
      }
    }

    public final void waitUntilThreadStops() {
      if (this.isRunning()) {
        try {
          thread.join();
        } catch (InterruptedException e) {
          // Do nothing.
        }
      }
    }

    void init(File targetDirectory) throws IOException {

      // Validate input.
      if (targetDirectory == null) {
        throw new IllegalArgumentException("Target directory is null.");
      } else if (!targetDirectory.exists()) {
        throw new IllegalArgumentException("Target directory does not exist.");
      } else if (!targetDirectory.isDirectory()) {
        throw new IllegalArgumentException("Target directory is not a directory (it's a file).");
      }

      // Create a new Watch Service.
      this.targetDirectory = targetDirectory;
      watcherService = FileSystems.getDefault().newWatchService();
      this.targetDirectory.toPath().register(watcherService, ENTRY_CREATE);
    }
  }

  //---------- Plugin Interface Registration Methods ----------//


  /**
   * Register one or more Plugin Interfaces (i.e. a Java interface that extends Pluggable).
   *
   * @param pluginInterfaces Array of Plugin Interface classes.
   */
  @SafeVarargs
  public final void registerPluginInterfaces(
      Class<? extends Pluggable>... pluginInterfaces) {
    registerPluginInterfaces(Set.of(pluginInterfaces));
  }

  /**
   * Register multiple Plugin Interfaces (i.e. a Java interface that extends Pluggable).
   *
   * @param pluginInterfaces Set of Plugin Interface classes.
   */
  public final void registerPluginInterfaces(
      Set<Class<? extends Pluggable>> pluginInterfaces) {
    for (Class<? extends Pluggable> pluginInterface : pluginInterfaces) {
      // Validate the Plugin Interface.
      validatePluginInterfaceArgument(pluginInterface);

      // Add the Plugin Interface to the Registry.
      pluginRegistry.putIfAbsent(pluginInterface, new TreeSet<>());
    }
  }

  /**
   * Removes one or more Plugin Interfaces from the Plugin Registry.
   *
   * @param registeredPluginInterfaces Array of registered Plugin Interface classes.
   */
  @SafeVarargs
  public final void unregisterPluginInterfaces(
      Class<? extends Pluggable>... registeredPluginInterfaces) {
    unregisterPluginInterfaces(Set.of(registeredPluginInterfaces));
  }

  /**
   * Removes multiple Plugin Interfaces from the Plugin Registry.
   *
   * @param registeredPluginInterfaces Set of registered Plugin Interface classes.
   */
  public final void unregisterPluginInterfaces(
      Set<Class<? extends Pluggable>> registeredPluginInterfaces) {

    // Validate the Plugin Interface.
    for (Class<? extends Pluggable> registeredPluginInterface : registeredPluginInterfaces) {
      validateRegisteredPluginInterfaceArgument(registeredPluginInterface);
    }

    // Remove the Plugin Interface from the Registry.
    for (Class<? extends Pluggable> pluginInterface : registeredPluginInterfaces) {
      pluginRegistry.remove(pluginInterface);
    }
  }

  /**
   * Returns true if the specified Plugin Interface is currently registered, otherwise false.
   *
   * @param pluginInterface Plugin Interface class.
   * @return True if Plugin Interface is registered, false otherwise.
   */
  public final boolean isRegisteredPluginInterface(
      Class<? extends Pluggable> pluginInterface) {

    // Validate input.
    validatePluginInterfaceArgument(pluginInterface);

    return pluginRegistry.containsKey(pluginInterface);
  }

  //---------- Plugin Interface Statistics Methods ----------//


  /**
   * Returns the set of registered Plugin Interfaces.
   *
   * @return Set of registered Plugin Interfaces.
   */
  public final List<Class<? extends Pluggable>> getRegisteredInterfaces() {
    return List.copyOf(pluginRegistry.navigableKeySet());
  }

  /**
   * Returns the number of Plugin Interfaces that are currently registered in the Plugin Registry.
   *
   * @return Total Plugin Interfaces registered.
   */
  public final int getRegisteredInterfaceCount() {
    return pluginRegistry.keySet().size();
  }

  /**
   * Delete all registered Plugin Interfaces and Plugins from the registry.
   */
  public final void clear() {
    pluginRegistry.clear();
  }

  //---------- Plugin Registration Methods ----------//


  /**
   * Registers a Plugin under all registered Plugin Interfaces that the Plugin implements.
   *
   * @param plugin Plugin class to be registered.
   */
  public final ScanResults registerPlugin(
      Class<? extends Pluggable> plugin) {
    // Check that Plugin Interfaces are currently registered in the Plugin Registry.
    validatePluginInterfacesAreRegistered();

    boolean pluginImplementsARegisteredInterface = false;
    List<ScanLog> scanLogs = new ArrayList<>();

    for (Class<? extends Pluggable> pluginInterface : pluginRegistry.keySet()) {

      // Validate the Plugin against the specified Plugin Interface.
      if (isValidPlugin(plugin, pluginInterface)) {
        scanLogs.add(registerPlugin(plugin, pluginInterface));
        pluginImplementsARegisteredInterface = true;
      }
    }

    if (!pluginImplementsARegisteredInterface) {
      throw new IllegalArgumentException(String.format(
          "Plugin class [%s] does not implement any registered Plugin Interface.",
          plugin.getCanonicalName()));
    }

    return new ScanResults(scanLogs);
  }

  /**
   * Registers a Plugin under the specified Plugin Interface.
   *
   * @param plugin Plugin class to be registered.
   * @param registeredPluginInterface Registered Plugin Interface to register the Plugin under.
   */
  public final ScanLog registerPlugin(
      Class<? extends Pluggable> plugin,
      Class<? extends Pluggable> registeredPluginInterface) {

    // Check that Plugin Interfaces are currently registered in the Plugin Registry.
    validatePluginInterfacesAreRegistered();

    // Validate the Plugin Interface.
    validateRegisteredPluginInterfaceArgument(registeredPluginInterface);

    // Validate the Plugin against the specified Plugin Interface.
    validatePluginArgument(plugin, registeredPluginInterface);

    ScanLog scanLog;

    // Retrieve the path to the class file (if possible).
    String pathToClassFile;
    try {
      pathToClassFile = plugin.getResource(plugin.getSimpleName() + ".class").getPath();
    } catch (Exception e) {
      pathToClassFile = "";
    }

    // Check if this class is already registered.
    if (pluginRegistry.get(registeredPluginInterface).stream()
        .anyMatch(x -> x.getPluginClass().getCanonicalName().equals(plugin.getCanonicalName()))) {

      // Create the scan log.
      scanLog = new ScanLog(
          false,
          true,
          "PLUGIN",
          pathToClassFile,
          plugin.getCanonicalName(),
          String.format("Plugin class [%s] is already registered to Plugin Interface [%s].",
              plugin.getCanonicalName(), registeredPluginInterface.getCanonicalName()));
    } else {

      // Create the PluginEntry.
      PluginEntry pluginEntry = new PluginEntry(plugin);

      // Check if the Plugin Name and Version is already registered.
      if (pluginRegistry.get(registeredPluginInterface).stream()
          .anyMatch(x ->
              x.getPluginName().equals(pluginEntry.getPluginName()) &&
                  x.getPluginVersion().equals(pluginEntry.getPluginVersion()))) {

        // Create the scan log.
        scanLog = new ScanLog(
            false,
            true,
            "PLUGIN",
            pathToClassFile,
            plugin.getCanonicalName(),
            String.format("Plugin [%s (%s)] is already registered to Plugin Interface [%s].",
                pluginEntry.getPluginName(),
                pluginEntry.getPluginVersion().toString(),
                registeredPluginInterface.getCanonicalName()));
      } else {
        // Add the Plugin to the Registry.
        pluginRegistry.get(registeredPluginInterface).add(pluginEntry);

        // Create the scan log.
        scanLog = new ScanLog(
            true,
            true,
            "PLUGIN",
            pathToClassFile,
            plugin.getCanonicalName(),
            "");
      }
    }

    return scanLog;
  }

  /**
   * Unregister the specified Plugin from all registered Plugin Interfaces.
   *
   * @param plugin Plugin class.
   */
  public final void unregisterPlugin(
      Class<? extends Pluggable> plugin) {

    // Check that Plugin Interfaces are currently registered in the Plugin Registry.
    validatePluginInterfacesAreRegistered();

    boolean pluginImplementsARegisteredInterface = false;

    for (Class<? extends Pluggable> registeredPluginInterface : pluginRegistry.keySet()) {
      for (PluginEntry pluginEntry : pluginRegistry.get(registeredPluginInterface)) {
        if (pluginEntry.getPluginClass().equals(plugin)) {
          unregisterPlugin(pluginEntry.getPluginClass(), registeredPluginInterface);
          pluginImplementsARegisteredInterface = true;
        }
      }
    }

    if (!pluginImplementsARegisteredInterface) {
      throw new IllegalArgumentException(String.format(
          "Plugin class [%s] was not found under any registered Plugin Interface.",
          plugin.getCanonicalName()));
    }
  }

  /**
   * Unregister the specified Plugin from the specified Plugin Interface.
   *
   * @param plugin Plugin class.
   * @param registeredPluginInterface Registered Plugin Interface class.
   */
  public final void unregisterPlugin(
      Class<? extends Pluggable> plugin,
      Class<? extends Pluggable> registeredPluginInterface) {

    // Check that Plugin Interfaces are currently registered in the Plugin Registry.
    validatePluginInterfacesAreRegistered();

    // Validate the Plugin Interface.
    validateRegisteredPluginInterfaceArgument(registeredPluginInterface);

    // Validate the Plugin.
    validatePluginArgument(plugin, registeredPluginInterface);

    // Retrieve the Plugins registered under the specified Plugin Interface.
    Set<PluginEntry> registeredPlugins = pluginRegistry.get(registeredPluginInterface);

    // Identify the Plugin targeted for removal.
    Optional<PluginEntry> targetPluginOptional = registeredPlugins.stream()
        .filter(x -> x.getPluginClass().equals(plugin))
        .findFirst();

    // Check that the plugin exists in the set.
    if (targetPluginOptional.isEmpty()) {
      throw new IllegalArgumentException(String.format(
          "Plugin class [%s] is not registered under the [%s] Plugin Interface.",
          plugin.getCanonicalName(), registeredPluginInterface.getSimpleName()));
    }

    // Remove the Plugin from the plugin registry.
    registeredPlugins.remove(targetPluginOptional.get());
  }

  /**
   * Unregister all Plugins with the specified name, from all registered Plugin Interfaces.
   *
   * @param pluginName Plugin name.
   */
  public final void unregisterPlugin(
      String pluginName) {

    // Check that Plugin Interfaces are currently registered in the Plugin Registry.
    validatePluginInterfacesAreRegistered();

    boolean pluginImplementsARegisteredInterface = false;

    for (Class<? extends Pluggable> registeredPluginInterface : pluginRegistry.keySet()) {
      for (PluginEntry pluginEntry : pluginRegistry.get(registeredPluginInterface)) {
        if (pluginEntry.getPluginName().equals(pluginName)) {
          unregisterPlugin(pluginEntry.getPluginClass(), registeredPluginInterface);
          pluginImplementsARegisteredInterface = true;
        }
      }
    }

    if (!pluginImplementsARegisteredInterface) {
      throw new IllegalArgumentException(String.format(
          "Plugin [%s] does not implement any registered Plugin Interface.",
          pluginName));
    }
  }

  /**
   * Unregister all Plugins with the specified name, from under the specified Plugin Interface.
   *
   * @param pluginName Name of the plugin.
   * @param registeredPluginInterface Registered Plugin Interface class.
   */
  public final void unregisterPlugin(
      String pluginName,
      Class<? extends Pluggable> registeredPluginInterface) {

    // Check that Plugin Interfaces are currently registered in the Plugin Registry.
    validatePluginInterfacesAreRegistered();

    // Validate input.
    validatePluginNameArgument(pluginName);
    validateRegisteredPluginInterfaceArgument(registeredPluginInterface);

    boolean pluginImplementsARegisteredInterface = false;

    for (PluginEntry pluginEntry : pluginRegistry.get(registeredPluginInterface)) {
      if (pluginEntry.getPluginName().equals(pluginName)) {
        unregisterPlugin(pluginEntry.getPluginClass(), registeredPluginInterface);
        pluginImplementsARegisteredInterface = true;
      }
    }

    if (!pluginImplementsARegisteredInterface) {
      throw new IllegalArgumentException(String.format(
          "Plugin [%s] was not registered under the [%s] Plugin Interface.",
          pluginName, registeredPluginInterface.getSimpleName()));
    }
  }

  /**
   * Unregister all Plugins with the specified name and version number, from under all registered
   * Plugin Interfaces.
   *
   * @param pluginName Plugin name.
   * @param majorVersion Major version number.
   * @param minorVersion Minor version number.
   * @param buildNumber Build number.
   */
  public final void unregisterPlugin(
      String pluginName,
      int majorVersion, int minorVersion, int buildNumber) {

    // Check that Plugin Interfaces are currently registered in the Plugin Registry.
    validatePluginInterfacesAreRegistered();

    // Validate input, and create the PluginVersion object.
    validatePluginNameArgument(pluginName);
    PluginVersion pluginVersion = new PluginVersion(majorVersion, minorVersion, buildNumber);

    boolean pluginImplementsARegisteredInterface = false;

    for (Class<? extends Pluggable> registeredPluginInterface : pluginRegistry.keySet()) {
      for (PluginEntry pluginEntry : pluginRegistry.get(registeredPluginInterface)) {
        if (pluginEntry.getPluginName().equals(pluginName) &&
            pluginEntry.getPluginVersion().equals(pluginVersion)) {
          unregisterPlugin(pluginEntry.getPluginClass(), registeredPluginInterface);
          pluginImplementsARegisteredInterface = true;
        }
      }
    }

    if (!pluginImplementsARegisteredInterface) {
      throw new IllegalArgumentException(String.format(
          "Plugin [%s] does not implement any registered Plugin Interface.",
          pluginName));
    }
  }

  /**
   * Unregister the Plugin with the specified name and version number, from under the specified
   * Plugin Interface.
   *
   * @param pluginName Name of the plugin.
   * @param majorVersion Major version number.
   * @param minorVersion Minor version number.
   * @param buildNumber Build number.
   * @param registeredPluginInterface Registered Plugin Interface class.
   */
  public final void unregisterPlugin(
      String pluginName,
      int majorVersion, int minorVersion, int buildNumber,
      Class<? extends Pluggable> registeredPluginInterface) {

    // Check that Plugin Interfaces are currently registered in the Plugin Registry.
    validatePluginInterfacesAreRegistered();

    // Validate input, and create the PluginVersion object.
    validatePluginNameArgument(pluginName);
    validateRegisteredPluginInterfaceArgument(registeredPluginInterface);
    PluginVersion pluginVersion = new PluginVersion(majorVersion, minorVersion, buildNumber);

    boolean pluginImplementsARegisteredInterface = false;

    for (PluginEntry pluginEntry : pluginRegistry.get(registeredPluginInterface)) {
      if (pluginEntry.getPluginName().equals(pluginName) &&
          pluginEntry.getPluginVersion().equals(pluginVersion)) {
        unregisterPlugin(pluginEntry.getPluginClass(), registeredPluginInterface);
        pluginImplementsARegisteredInterface = true;
      }
    }

    if (!pluginImplementsARegisteredInterface) {
      throw new IllegalArgumentException(String.format(
          "Plugin [%s] was not registered under the [%s] Plugin Interface.",
          pluginName, registeredPluginInterface.getSimpleName()));
    }
  }

  /**
   * Returns true if a Plugin with the specified name has been registered under any registered
   * Plugin Interface.
   *
   * @param pluginName Name of the target Plugin.
   */
  public final boolean isRegisteredPlugin(
      String pluginName) {

    // Loop through each registered Plugin Interface.
    for (Class<? extends Pluggable> registeredPluginInterface : pluginRegistry.keySet()) {

      // Loop through each Plugin registered under the specified Plugin Interface.
      for (PluginEntry pluginEntry : pluginRegistry.get(registeredPluginInterface)) {
        // Check if this plugin matches the specified name.
        if (pluginEntry.getPluginName().equals(pluginName)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Returns true if a Plugin with the specified name has been registered under the specified Plugin
   * Interface.
   *
   * @param pluginName Name of the target Plugin.
   * @param registeredPluginInterface Registered Plugin Interface class.
   */
  public final boolean isRegisteredPlugin(
      String pluginName,
      Class<? extends Pluggable> registeredPluginInterface) {

    // Validate input.
    validatePluginNameArgument(pluginName);
    validateRegisteredPluginInterfaceArgument(registeredPluginInterface);

    // Loop through each Plugin registered under the specified Plugin Interface.
    for (PluginEntry pluginEntry : pluginRegistry.get(registeredPluginInterface)) {
      // Check if this plugin matches the specified name.
      if (pluginEntry.getPluginName().equals(pluginName)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns true if a Plugin with the specified name and version number has been registered under
   * the specified Plugin Interface.
   *
   * @param pluginName Name of the target Plugin.
   */
  public final boolean isRegisteredPlugin(
      String pluginName,
      int majorVersion, int minorVersion, int buildNumber) {

    // Validate input, and create the PluginVersion object.
    validatePluginNameArgument(pluginName);
    PluginVersion pluginVersion = new PluginVersion(majorVersion, minorVersion, buildNumber);

    // Loop through each registered Plugin Interface.
    for (Class<? extends Pluggable> registeredPluginInterface : pluginRegistry.keySet()) {
      if (isRegisteredPlugin(pluginName, pluginVersion, registeredPluginInterface)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Returns true if a Plugin with the specified name and version number has been registered under
   * the specified Plugin Interface.
   *
   * @param pluginName Name of the target Plugin.
   * @param registeredPluginInterface Registered Plugin Interface class.
   */
  public final boolean isRegisteredPlugin(
      String pluginName,
      int majorVersion, int minorVersion, int buildNumber,
      Class<? extends Pluggable> registeredPluginInterface) {

    // Validate input, and create the PluginVersion object.
    validatePluginNameArgument(pluginName);
    validateRegisteredPluginInterfaceArgument(registeredPluginInterface);
    PluginVersion pluginVersion = new PluginVersion(majorVersion, minorVersion, buildNumber);

    return isRegisteredPlugin(pluginName, pluginVersion, registeredPluginInterface);
  }

  /**
   * Delete all registered Plugins from under the specified Plugin Interfaces. (Does not remove the
   * Plugin Interface itself.)
   *
   * @param registeredPluginInterfaces Array of registered Plugin Interface classes.
   */
  @SafeVarargs
  public final void clear(Class<? extends Pluggable>... registeredPluginInterfaces) {
    clear(Set.of(registeredPluginInterfaces));
  }

  /**
   * Delete all registered Plugins from under the specified Plugin Interfaces. (Does not remove the
   * Plugin Interface itself.)
   *
   * @param registeredPluginInterfaces Set of registered Plugin Interface classes.
   */
  public final void clear(Set<Class<? extends Pluggable>> registeredPluginInterfaces) {

    // Validate input.
    for (Class<? extends Pluggable> registeredPluginInterface : registeredPluginInterfaces) {
      validateRegisteredPluginInterfaceArgument(registeredPluginInterface);
    }

    // Remove the Plugin records.
    for (Class<? extends Pluggable> registeredPluginInterface : registeredPluginInterfaces) {
      pluginRegistry.get(registeredPluginInterface).clear();
    }
  }

  //---------- Plugin Scanning Methods ----------//


  /**
   * Scan the current JAR file (or Package Directory) for Plugins that implement any registered
   * Plugin Interface.
   *
   * @return Scan results.
   */
  public final ScanResults scan() throws IOException {
    Set<Class<? extends Pluggable>> registeredPluginInterfaces = pluginRegistry.keySet();

    if (registeredPluginInterfaces.size() <= 0) {
      throw new IllegalStateException("No interfaces have been registered to the plugin registry.");
    }

    return scan(registeredPluginInterfaces, null, true);
  }

  /**
   * Scan the current JAR file (or Package Directory) for Plugins that implement any registered
   * Plugin Interface.
   *
   * @param targetPackages Set of packages to scan for plugins (or null, to scan all packages).
   * @param scanSubpackages Recursively scan subpackages of target packages.
   * @return Scan results.
   */
  public final ScanResults scan(Set<String> targetPackages, boolean scanSubpackages)
      throws IOException {
    Set<Class<? extends Pluggable>> registeredPluginInterfaces = pluginRegistry.keySet();

    if (registeredPluginInterfaces.size() <= 0) {
      throw new IllegalStateException("No interfaces have been registered to the plugin registry.");
    }

    return scan(registeredPluginInterfaces, targetPackages, scanSubpackages);
  }

  /**
   * Scan the current JAR file (or Package Directory) for Plugins that implement the specified
   * Plugin Interface.
   *
   * @param registeredPluginInterface Registered Plugin Interface.
   * @return Scan results.
   */
  public final ScanResults scan(Class<? extends Pluggable> registeredPluginInterface)
      throws IOException {
    return scan(Set.of(registeredPluginInterface), null, true);
  }

  /**
   * Scan the current JAR file (or Package Directory) for Plugins that implement the specified
   * Plugin Interface, within the specified target packages.
   *
   * @param registeredPluginInterface Registered Plugin Interface.
   * @param targetPackages Set of packages to scan for plugins (or null, to scan all packages).
   * @param scanSubpackages Recursively scan subpackages of target packages.
   * @return Scan results.
   */
  public final ScanResults scan(
      Class<? extends Pluggable> registeredPluginInterface,
      Set<String> targetPackages,
      boolean scanSubpackages)
      throws IOException {
    Objects.requireNonNull(registeredPluginInterface);
    return scan(Set.of(registeredPluginInterface), targetPackages, scanSubpackages);
  }

  /**
   * Scan the current JAR file (or Package Directory) for Plugins that implement the specified
   * Plugin Interfaces.
   *
   * @param registeredPluginInterfaces Set of registered Plugin Interfaces.
   * @return Scan results.
   */
  public final ScanResults scan(Set<Class<? extends Pluggable>> registeredPluginInterfaces)
      throws IOException {
    return scan(registeredPluginInterfaces, null, true);
  }

  /**
   * Scan the current JAR file (or Package Directory) for Plugins that implement the specified
   * Plugin Interfaces, within the specified target packages.
   *
   * @param registeredPluginInterfaces Set of registered Plugin Interfaces.
   * @param targetPackages Set of packages to scan for plugins (or null, to scan all packages).
   * @param scanSubpackages Recursively scan subpackages of target packages.
   * @return Scan results.
   */
  public final ScanResults scan(
      Set<Class<? extends Pluggable>> registeredPluginInterfaces,
      Set<String> targetPackages,
      boolean scanSubpackages)
      throws IOException {

    // Determine the path to the calling class's root (either a JAR or Package Directory).
    File callingClassRootPath = getCallingClassRootPath();

    // Determine if the root is a JAR or Package Directory.
    if (callingClassRootPath.isDirectory()) {

      // Scan the package directory.
      return scanPackageDirectory(
          callingClassRootPath.getParentFile().getPath(),
          registeredPluginInterfaces,
          targetPackages,
          scanSubpackages);
    } else {

      // Scan the JAR file.
      return scanJar(
          callingClassRootPath.getPath(),
          registeredPluginInterfaces,
          targetPackages,
          scanSubpackages);
    }
  }

// TODO: FUTURE - Scanning the classpath is difficult; ran out of time for now...
//  /**
//   * Scans the classpath for Plugins that implement any registered Plugin Interface.
//   */
//  public final void scanClasspath() throws IOException {
//    Set<Class<? extends Pluggable>> registeredPluginInterfaces = pluginRegistry.keySet();
//
//    if (registeredPluginInterfaces.size() <= 0) {
//      throw new IllegalStateException("No interfaces have been registered to the plugin registry.");
//    }
//
//    scanClasspath(registeredPluginInterfaces);
//  }
//
//  /**
//   * Scans the classpath for Plugins that implement the specified Plugin Interface.
//   *
//   * @param registeredPluginInterface Registered Plugin Interface.
//   */
//  public final void scanClasspath(Class<? extends Pluggable> registeredPluginInterface)
//      throws IOException {
//    scanClasspath(Set.of(registeredPluginInterface));
//  }
//
//  /**
//   * Scans the classpath for Plugins that implement the specified Plugin Interfaces.
//   *
//   * @param registeredPluginInterfaces Set of registered Plugin Interfaces.
//   */
//  public final void scanClasspath(Set<Class<? extends Pluggable>> registeredPluginInterfaces)
//      throws IOException {
//
//    // Get the class loader.
//    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
//
//    // Find package roots.
//    Enumeration<URL> roots = classLoader.getResources("");
//    while (roots.hasMoreElements()) {
//      File root = new File(roots.nextElement().getPath());
//      File[] rootFiles = root.listFiles();
//      if (rootFiles != null) {
//        for (File resourceFile : rootFiles) {
//          if (resourceFile.isDirectory()) {
//            // Loop through its listFiles() recursively.
//            debug(String.format("DIRECTORY: %s", resourceFile.getPath()));
//
//            // DEBUG.
//            debug("- Package Root: " + resourceFile.getName());
//
//            Set<File> classFiles = retrieveClassFilesInPackage(resourceFile.getName());
//
//            // Loop through each class file.
//            for (File file : classFiles) {
//
//              // Generate the fully qualified class name.
//              String fullClassName =
//                  resourceFile.getName() + "." + file.getName().replace(".class", "");
//
//              // Load the class definition into memory.
//              Class<?> clazz;
//              try {
//                clazz = Class.forName(fullClassName);
//              } catch (ClassNotFoundException e) {
//                throw new PluginLibraryException(
//                    String.format("Class could not be loaded [%s].", fullClassName), e);
//              }
//
//              // Register the plugin.
//              for (Class<? extends Pluggable> registeredPluginInterface : registeredPluginInterfaces) {
//                if (isValidPlugin(clazz, registeredPluginInterface)) {
//
//                  @SuppressWarnings("unchecked")
//                  Class<? extends Pluggable> pluggableClazz = (Class<? extends Pluggable>) clazz;
//
//                  registerPlugin(pluggableClazz, registeredPluginInterface);
//                }
//              }
//            }
//          } else {
//            // Ignore resource files.
//            debug(String.format("RESOURCE FILE: %s", resourceFile.getPath()));
//          }
//        }
//      }
//    }
//
////    // Loop through each package found in the current JAR.
////    for (Package pkg : classLoader.getDefinedPackages()) {
////
////      // DEBUG.
////      debug("- Package: " + pkg.getName());
////
////      Set<File> classFiles = retrieveClassFilesInPackage(pkg.getName());
////
////      // Loop through each class file.
////      for (File file : classFiles) {
////
////        // Generate the fully qualified class name.
////        String fullClassName = pkg.getName() + "." + file.getName().replace(".class", "");
////
////        // Load the class definition into memory.
////        Class<?> clazz;
////        try {
////          clazz = Class.forName(fullClassName);
////        } catch (ClassNotFoundException e) {
////          throw new PluginLibraryException("Class could not be loaded.", e);
////        }
////
////        // Register the plugin.
////        for (Class<? extends Pluggable> registeredPluginInterface : registeredPluginInterfaces) {
////          if (isValidPlugin(clazz, registeredPluginInterface)) {
////
////            @SuppressWarnings("unchecked")
////            Class<? extends Pluggable> pluggableClazz = (Class<? extends Pluggable>) clazz;
////
////            registerPlugin(pluggableClazz, registeredPluginInterface);
////          }
////        }
////      }
////    }
//  }

  /**
   * Scan the specified JAR file for Plugins that implement any registered Plugin Interface.
   *
   * @param jarFilePath Path to JAR file to be scanned.
   * @return Scan results.
   */
  public final ScanResults scanJar(String jarFilePath) throws IOException {
    Set<Class<? extends Pluggable>> registeredPluginInterfaces = pluginRegistry.keySet();

    if (registeredPluginInterfaces.size() <= 0) {
      throw new IllegalStateException("No interfaces have been registered to the plugin registry.");
    }

    return scanJar(jarFilePath, registeredPluginInterfaces, null, true);
  }

  /**
   * Scan the specified JAR file for Plugins that implement any registered Plugin Interface.
   *
   * @param jarFilePath Path to JAR file to be scanned.
   * @param targetPackages Set of packages to scan for plugins (or null, to scan all packages).
   * @param scanSubpackages Recursively scan subpackages of target packages.
   * @return Scan results.
   */
  public final ScanResults scanJar(
      String jarFilePath,
      Set<String> targetPackages,
      boolean scanSubpackages) throws IOException {
    Set<Class<? extends Pluggable>> registeredPluginInterfaces = pluginRegistry.keySet();

    if (registeredPluginInterfaces.size() <= 0) {
      throw new IllegalStateException("No interfaces have been registered to the plugin registry.");
    }

    return scanJar(jarFilePath, registeredPluginInterfaces, targetPackages, scanSubpackages);
  }

  /**
   * Scan the specified JAR file for Plugins that implement the specified Plugin Interface.
   *
   * @param jarFilePath Path to JAR file to be scanned.
   * @param registeredPluginInterface Registered Plugin Interfaces.
   * @return Scan results.
   */
  public final ScanResults scanJar(
      String jarFilePath,
      Class<? extends Pluggable> registeredPluginInterface) throws IOException {
    return scanJar(jarFilePath, Set.of(registeredPluginInterface), null, true);
  }

  /**
   * Scan the specified JAR file for Plugins that implement the specified Plugin Interface.
   *
   * @param jarFilePath Path to JAR file to be scanned.
   * @param registeredPluginInterface Registered Plugin Interfaces.
   * @param targetPackages Set of packages to scan for plugins (or null, to scan all packages).
   * @param scanSubpackages Recursively scan subpackages of target packages.
   * @return Scan results.
   */
  public final ScanResults scanJar(
      String jarFilePath,
      Class<? extends Pluggable> registeredPluginInterface,
      Set<String> targetPackages,
      boolean scanSubpackages) throws IOException {
    Objects.requireNonNull(registeredPluginInterface);
    return scanJar(jarFilePath, Set.of(registeredPluginInterface), targetPackages, scanSubpackages);
  }

  /**
   * Scan the specified JAR file for Plugins that implement the specified Plugin Interfaces.
   *
   * @param jarFilePath Path to JAR file to be scanned.
   * @param registeredPluginInterfaces Set of registered Plugin Interfaces.
   * @return Scan results.
   */
  public final ScanResults scanJar(
      String jarFilePath,
      Set<Class<? extends Pluggable>> registeredPluginInterfaces)
      throws IOException {
    return scanJar(jarFilePath, registeredPluginInterfaces, null, true);
  }

  /**
   * Scan the specified JAR file for Plugins that implement the specified Plugin Interfaces.
   *
   * @param jarFilePath Path to JAR file to be scanned.
   * @param registeredPluginInterfaces Set of registered Plugin Interfaces.
   * @param targetPackages Set of packages to scan for plugins (or null, to scan all packages).
   * @param scanSubpackages Recursively scan subpackages of target packages.
   * @return Scan results.
   */
  public final ScanResults scanJar(
      String jarFilePath,
      Set<Class<? extends Pluggable>> registeredPluginInterfaces,
      Set<String> targetPackages,
      boolean scanSubpackages) throws IOException {

    // Validate the Plugin Interface.
    validateRegisteredPluginInterfaceArguments(registeredPluginInterfaces);

    // Validate the package names.
    validateJavaPackageNames(targetPackages);

    // Validate the specified JAR file path.
    File jarFile = new File(jarFilePath);
    if (!jarFile.exists()) {
      throw new IllegalArgumentException(
          String.format("Path does not exist [%s].", jarFilePath));
    } else if (jarFile.isDirectory()) {
      throw new IllegalArgumentException(
          String.format("Path is not a file [%s].", jarFilePath));
    } else if (!jarFile.getName().endsWith(".jar")) {
      throw new IllegalArgumentException(
          String.format("Path does not specify a JAR file [%s].", jarFilePath));
    }

    // Track the scan.
    List<ScanLog> scanLogs = new ArrayList<>();

    // Open the specified JAR file.
    FileInputStream fis = new FileInputStream(jarFile);
    JarInputStream jis = new JarInputStream(fis);

    // Loop through each package found in the current JAR.
    try {
      JarEntry jarEntry = jis.getNextJarEntry();
      Map<String, URL> urlsFound = new HashMap<>();

      // Gather the URL and Full Class Name of all class definitions found in the JAR file.
      while (jarEntry != null) {
        if (jarEntry.isDirectory()) {

          // Add to scan logs.
          scanLogs.add(new ScanLog(
              false, false, "DIRECTORY", jarEntry.getName(), "", ""));
        } else if (jarEntry.getName().endsWith(".class")) {

          // Generate the fully qualified class name.
          String fullClassName = jarEntry.getName()
              .replace("/", ".")
              .replace(".class", "");

          // TODO: Validate that this fullClassName is valid (valid package name, class name, etc).

          // Check whether this class is contained within a targeted package.
          String pkgName = fullClassName.substring(0, fullClassName.lastIndexOf('.'));
          if (targetPackages == null ||
              (!scanSubpackages && targetPackages.contains(pkgName)) ||
              (scanSubpackages && targetPackages.stream().anyMatch(x -> pkgName.startsWith(x)))) {

            // Capture the URL to the class.
            URL classPackageRootUrl = new URL("jar:" + jarFile.toURI().toURL().toString() + "!/");

            // Collect all of the class names and URLs.
            urlsFound.put(fullClassName, classPackageRootUrl);
          } else {

            // Add to scan logs.
            scanLogs.add(new ScanLog(
                false, true, "CLASS", jarEntry.getName(), fullClassName,
                "Not located in a targeted package."));
          }
        } else {

          // Add to scan logs.
          scanLogs.add(new ScanLog(
              false, true, "RESOURCE", jarEntry.getName(), "",
              "Not a class file."));
        }

        jarEntry = jis.getNextJarEntry();
      }

      // Load all gathered class definitions into memory.
      URLClassLoader cl = new URLClassLoader(urlsFound.values().toArray(new URL[]{}));

      // Loop through each class, and check for Plugins.
      for (String fullClassName : urlsFound.keySet()) {

        // Load the class definition into memory.
        Class<?> clazz;
        try {
          clazz = cl.loadClass(fullClassName);
        } catch (ClassNotFoundException e) {
          throw new PluginRegistrationException(fullClassName, e);
        }

        // Perform basic plugin validation.
        if (isValidPlugin(clazz)) {

          // Register the plugin.
          boolean pluginRegistered = false;
          for (Class<? extends Pluggable> registeredPluginInterface : registeredPluginInterfaces) {
            if (isValidPlugin(clazz, registeredPluginInterface)) {

              @SuppressWarnings("unchecked")
              Class<? extends Pluggable> pluggableClazz = (Class<? extends Pluggable>) clazz;

              scanLogs.add(registerPlugin(pluggableClazz, registeredPluginInterface));

              pluginRegistered = true;
            }
          }

          // Check if this Plugin failed to match any of the registered Plugin Interfaces.
          if (!pluginRegistered) {
            // Add to scan logs.
            scanLogs.add(new ScanLog(
                false, true, "PLUGIN", urlsFound.get(fullClassName).getPath(), fullClassName,
                "Does not implement any of the targeted Plugin Interfaces."));
          }
        } else {
          // Add to scan logs.
          scanLogs.add(new ScanLog(
              false, true, "CLASS", urlsFound.get(fullClassName).getPath(), fullClassName,
              "Not a valid Plugin."));
        }
      }
    } finally {
      jis.close();
    }

    return new ScanResults(scanLogs);
  }

  /**
   * Decrypts and verifies the specified signed and encrypted package, and scans its JAR file.
   *
   * @param pathToEncryptedPackage Path to the encrypted package.
   * @param privateEncryptionKey Private key used to decrypt the package.
   * @return Scan results.
   * @throws IOException Thrown on IO exception.
   */
  public final ScanResults scanSignedAndEncryptedPackage(
      String pathToEncryptedPackage,
      String privateEncryptionKey,
      List<String> publicSignatureKeys) throws IOException {

    // Retrieve all registered plugin interfaces.
    Set<Class<? extends Pluggable>> registeredPluginInterfaces = pluginRegistry.keySet();
    if (registeredPluginInterfaces.size() <= 0) {
      throw new IllegalStateException("No interfaces have been registered to the plugin registry.");
    }

    return scanSignedAndEncryptedPackage(
        pathToEncryptedPackage,
        privateEncryptionKey,
        publicSignatureKeys,
        registeredPluginInterfaces,
        null,
        true);
  }

  /**
   * Decrypts and verifies the specified signed and encrypted package, and scans its JAR file.
   *
   * @param pathToEncryptedPackage Path to the encrypted package.
   * @param privateEncryptionKey Private key used to decrypt the package.
   * @param targetPackages Set of packages to scan for plugins (or null, to scan all packages).
   * @param scanSubpackages Recursively scan subpackages of target packages.
   * @return Scan results.
   * @throws IOException Thrown on IO exception.
   */
  public final ScanResults scanSignedAndEncryptedPackage(
      String pathToEncryptedPackage,
      String privateEncryptionKey,
      List<String> publicSignatureKeys,
      Set<String> targetPackages,
      boolean scanSubpackages) throws IOException {

    // Retrieve all registered plugin interfaces.
    Set<Class<? extends Pluggable>> registeredPluginInterfaces = pluginRegistry.keySet();
    if (registeredPluginInterfaces.size() <= 0) {
      throw new IllegalStateException("No interfaces have been registered to the plugin registry.");
    }

    return scanSignedAndEncryptedPackage(
        pathToEncryptedPackage,
        privateEncryptionKey,
        publicSignatureKeys,
        registeredPluginInterfaces,
        targetPackages,
        scanSubpackages);
  }

  /**
   * Decrypts and verifies the specified signed and encrypted package, and scans its JAR file.
   *
   * @param pathToEncryptedPackage Path to the encrypted package.
   * @param privateEncryptionKey Private key used to decrypt the package.
   * @param registeredPluginInterface Registered Plugin Interface.
   * @return Scan results.
   * @throws IOException Thrown on IO exception.
   */
  public final ScanResults scanSignedAndEncryptedPackage(
      String pathToEncryptedPackage,
      String privateEncryptionKey,
      List<String> publicSignatureKeys,
      Class<? extends Pluggable> registeredPluginInterface) throws IOException {

    return scanSignedAndEncryptedPackage(
        pathToEncryptedPackage,
        privateEncryptionKey,
        publicSignatureKeys,
        Set.of(registeredPluginInterface),
        null,
        true);
  }

  /**
   * Decrypts and verifies the specified signed and encrypted package, and scans its JAR file.
   *
   * @param pathToEncryptedPackage Path to the encrypted package.
   * @param privateEncryptionKey Private key used to decrypt the package.
   * @param registeredPluginInterface Registered Plugin Interface.
   * @param targetPackages Set of packages to scan for plugins (or null, to scan all packages).
   * @param scanSubpackages Recursively scan subpackages of target packages.
   * @return Scan results.
   * @throws IOException Thrown on IO exception.
   */
  public final ScanResults scanSignedAndEncryptedPackage(
      String pathToEncryptedPackage,
      String privateEncryptionKey,
      List<String> publicSignatureKeys,
      Class<? extends Pluggable> registeredPluginInterface,
      Set<String> targetPackages,
      boolean scanSubpackages) throws IOException {

    return scanSignedAndEncryptedPackage(
        pathToEncryptedPackage,
        privateEncryptionKey,
        publicSignatureKeys,
        Set.of(registeredPluginInterface),
        targetPackages,
        scanSubpackages);
  }

  /**
   * Decrypts and verifies the specified signed and encrypted package, and scans its JAR file.
   *
   * @param pathToEncryptedPackage Path to the encrypted package.
   * @param privateEncryptionKey Private key used to decrypt the package.
   * @param registeredPluginInterfaces Set of registered Plugin Interfaces.
   * @return Scan results.
   */
  public final ScanResults scanSignedAndEncryptedPackage(
      String pathToEncryptedPackage,
      String privateEncryptionKey,
      List<String> publicSignatureKeys,
      Set<Class<? extends Pluggable>> registeredPluginInterfaces) throws IOException {

    return scanSignedAndEncryptedPackage(
        pathToEncryptedPackage,
        privateEncryptionKey,
        publicSignatureKeys,
        registeredPluginInterfaces,
        null,
        true);
  }

  /**
   * Decrypts and verifies the specified signed and encrypted package, and scans its JAR file.
   *
   * @param pathToEncryptedPackage Path to the encrypted package.
   * @param privateEncryptionKey Private key used to decrypt the package.
   * @param publicSignatureKeys Public keys used to verify the package's digital signature.
   * @param registeredPluginInterfaces Set of registered Plugin Interfaces.
   * @param targetPackages Set of packages to scan for plugins (or null, to scan all packages).
   * @param scanSubpackages Recursively scan subpackages of target packages.
   * @return Scan results.
   */
  public final ScanResults scanSignedAndEncryptedPackage(
      String pathToEncryptedPackage,
      String privateEncryptionKey,
      List<String> publicSignatureKeys,
      Set<Class<? extends Pluggable>> registeredPluginInterfaces,
      Set<String> targetPackages,
      boolean scanSubpackages) throws IOException {

    // Validate input.
    Objects.requireNonNull(pathToEncryptedPackage);
    Objects.requireNonNull(privateEncryptionKey);

    // Decrypt the package.
    CryptographyManager cryptographyManager = new CryptographyManager(
        PluginKeyPair.fromPrivateKey(PluginKeyPair.dehexify(privateEncryptionKey)));

    String pathToJar = cryptographyManager.extractPackage(pathToEncryptedPackage, privateEncryptionKey, publicSignatureKeys);

    // Scan the decrypted JAR file.
    return scanJar(pathToJar, registeredPluginInterfaces, targetPackages, scanSubpackages);
  }

  /**
   * Decrypts the specified encrypted package, and scans its JAR file.
   *
   * @param pathToEncryptedPackage Path to the encrypted package.
   * @param privateEncryptionKey Private key used to decrypt the package.
   * @return Scan results.
   * @throws IOException Thrown on IO exception.
   */
  public final ScanResults scanEncryptedPackage(
      String pathToEncryptedPackage,
      String privateEncryptionKey) throws IOException {

    // Retrieve all registered plugin interfaces.
    Set<Class<? extends Pluggable>> registeredPluginInterfaces = pluginRegistry.keySet();
    if (registeredPluginInterfaces.size() <= 0) {
      throw new IllegalStateException("No interfaces have been registered to the plugin registry.");
    }

    return scanEncryptedPackage(
        pathToEncryptedPackage,
        privateEncryptionKey,
        registeredPluginInterfaces,
        null,
        true);
  }

  /**
   * Decrypts the specified encrypted package, and scans its JAR file.
   *
   * @param pathToEncryptedPackage Path to the encrypted package.
   * @param privateEncryptionKey Private key used to decrypt the package.
   * @param targetPackages Set of packages to scan for plugins (or null, to scan all packages).
   * @param scanSubpackages Recursively scan subpackages of target packages.
   * @return Scan results.
   * @throws IOException Thrown on IO exception.
   */
  public final ScanResults scanEncryptedPackage(
      String pathToEncryptedPackage,
      String privateEncryptionKey,
      Set<String> targetPackages,
      boolean scanSubpackages) throws IOException {

    // Retrieve all registered plugin interfaces.
    Set<Class<? extends Pluggable>> registeredPluginInterfaces = pluginRegistry.keySet();
    if (registeredPluginInterfaces.size() <= 0) {
      throw new IllegalStateException("No interfaces have been registered to the plugin registry.");
    }

    return scanEncryptedPackage(
        pathToEncryptedPackage,
        privateEncryptionKey,
        registeredPluginInterfaces,
        targetPackages,
        scanSubpackages);
  }

  /**
   * Decrypts the specified encrypted package, and scans its JAR file.
   *
   * @param pathToEncryptedPackage Path to the encrypted package.
   * @param privateEncryptionKey Private key used to decrypt the package.
   * @param registeredPluginInterface Registered Plugin Interface.
   * @return Scan results.
   * @throws IOException Thrown on IO exception.
   */
  public final ScanResults scanEncryptedPackage(
      String pathToEncryptedPackage,
      String privateEncryptionKey,
      Class<? extends Pluggable> registeredPluginInterface) throws IOException {

    return scanEncryptedPackage(
        pathToEncryptedPackage,
        privateEncryptionKey,
        Set.of(registeredPluginInterface),
        null,
        true);
  }

  /**
   * Decrypts the specified encrypted package, and scans its JAR file.
   *
   * @param pathToEncryptedPackage Path to the encrypted package.
   * @param privateEncryptionKey Private key used to decrypt the package.
   * @param registeredPluginInterface Registered Plugin Interface.
   * @param targetPackages Set of packages to scan for plugins (or null, to scan all packages).
   * @param scanSubpackages Recursively scan subpackages of target packages.
   * @return Scan results.
   * @throws IOException Thrown on IO exception.
   */
  public final ScanResults scanEncryptedPackage(
      String pathToEncryptedPackage,
      String privateEncryptionKey,
      Class<? extends Pluggable> registeredPluginInterface,
      Set<String> targetPackages,
      boolean scanSubpackages) throws IOException {

    return scanEncryptedPackage(
        pathToEncryptedPackage,
        privateEncryptionKey,
        Set.of(registeredPluginInterface),
        targetPackages,
        scanSubpackages);
  }

  /**
   * Decrypts the specified encrypted package, and scans its JAR file.
   *
   * @param pathToEncryptedPackage Path to the encrypted package.
   * @param privateEncryptionKey Private key used to decrypt the package.
   * @param registeredPluginInterfaces Set of registered Plugin Interfaces.
   * @return Scan results.
   */
  public final ScanResults scanEncryptedPackage(
      String pathToEncryptedPackage,
      String privateEncryptionKey,
      Set<Class<? extends Pluggable>> registeredPluginInterfaces) throws IOException {

    return scanEncryptedPackage(
        pathToEncryptedPackage,
        privateEncryptionKey,
        registeredPluginInterfaces,
        null,
        true);
  }

  /**
   * Decrypts the specified encrypted package, and scans its JAR file.
   *
   * @param pathToEncryptedPackage Path to the encrypted package.
   * @param privateEncryptionKey Private key used to decrypt the package.
   * @param registeredPluginInterfaces Set of registered Plugin Interfaces.
   * @param targetPackages Set of packages to scan for plugins (or null, to scan all packages).
   * @param scanSubpackages Recursively scan subpackages of target packages.
   * @return Scan results.
   */
  public final ScanResults scanEncryptedPackage(
      String pathToEncryptedPackage,
      String privateEncryptionKey,
      Set<Class<? extends Pluggable>> registeredPluginInterfaces,
      Set<String> targetPackages,
      boolean scanSubpackages) throws IOException {

    // Validate input.
    Objects.requireNonNull(pathToEncryptedPackage);
    Objects.requireNonNull(privateEncryptionKey);

    // Decrypt the package.
    CryptographyManager cryptographyManager = new CryptographyManager(
        PluginKeyPair.fromPrivateKey(PluginKeyPair.dehexify(privateEncryptionKey)));
    String pathToJar = cryptographyManager.extractPackage(pathToEncryptedPackage, privateEncryptionKey);

    // Scan the decrypted JAR file.
    return scanJar(pathToJar, registeredPluginInterfaces, targetPackages, scanSubpackages);
  }

  /**
   * Scan the specified package directory for Plugins that implement any registered Plugin
   * Interface.
   *
   * @param packageDirectoryPath Path to a package root directory.
   * @return Scan results.
   */
  public final ScanResults scanPackageDirectory(String packageDirectoryPath) {
    Set<Class<? extends Pluggable>> registeredPluginInterfaces = pluginRegistry.keySet();

    if (registeredPluginInterfaces.size() <= 0) {
      throw new IllegalStateException("No interfaces have been registered to the plugin registry.");
    }

    return scanPackageDirectory(packageDirectoryPath, registeredPluginInterfaces, null, true);
  }

  /**
   * Scan the specified package directory for Plugins that implement any registered Plugin
   * Interface.
   *
   * @param packageDirectoryPath Path to a package root directory.
   * @param targetPackages Set of packages to scan for plugins (or null, to scan all packages).
   * @param scanSubpackages Recursively scan subpackages of target packages.
   * @return Scan results.
   */
  public final ScanResults scanPackageDirectory(
      String packageDirectoryPath,
      Set<String> targetPackages,
      boolean scanSubpackages) {
    Set<Class<? extends Pluggable>> registeredPluginInterfaces = pluginRegistry.keySet();

    if (registeredPluginInterfaces.size() <= 0) {
      throw new IllegalStateException("No interfaces have been registered to the plugin registry.");
    }

    return scanPackageDirectory(
        packageDirectoryPath,
        registeredPluginInterfaces,
        targetPackages,
        scanSubpackages);
  }

  /**
   * Scan the specified package directory for Plugins that implement the specified Plugin
   * Interface.
   *
   * @param packageDirectoryPath Path to a package root directory.
   * @param registeredPluginInterface Registered Plugin Interface class.
   * @return Scan results.
   */
  public final ScanResults scanPackageDirectory(
      String packageDirectoryPath,
      Class<? extends Pluggable> registeredPluginInterface) {

    return scanPackageDirectory(
        packageDirectoryPath,
        Set.of(registeredPluginInterface),
        null,
        true);
  }

  /**
   * Scan the specified package directory for Plugins that implement the specified Plugin
   * Interface.
   *
   * @param packageDirectoryPath Path to a package root directory.
   * @param registeredPluginInterface Registered Plugin Interface class.
   * @param targetPackages Set of packages to scan for plugins (or null, to scan all packages).
   * @param scanSubpackages Recursively scan subpackages of target packages.
   * @return Scan results.
   */
  public final ScanResults scanPackageDirectory(
      String packageDirectoryPath,
      Class<? extends Pluggable> registeredPluginInterface,
      Set<String> targetPackages,
      boolean scanSubpackages) {

    return scanPackageDirectory(
        packageDirectoryPath,
        Set.of(registeredPluginInterface),
        targetPackages,
        scanSubpackages);
  }

  /**
   * Scan the specified package directory for Plugins that implement the specified Plugin
   * Interfaces.
   *
   * @param packageDirectoryPath Path to a package root directory.
   * @param registeredPluginInterfaces Set of registered Plugin Interface.
   * @return Scan results.
   */
  final ScanResults scanPackageDirectory(
      String packageDirectoryPath,
      Set<Class<? extends Pluggable>> registeredPluginInterfaces) {

    return scanPackageDirectory(
        packageDirectoryPath,
        registeredPluginInterfaces,

        null,
        true);
  }

  /**
   * Scan the specified package directory for Plugins that implement the specified Plugin
   * Interfaces.
   *
   * @param packageDirectoryPath Path to a package root directory.
   * @param registeredPluginInterfaces Set of registered Plugin Interface.
   * @param targetPackages Set of packages to scan for plugins (or null, to scan all packages).
   * @param scanSubpackages Recursively scan subpackages of target packages.
   * @return Scan results.
   */
  public final ScanResults scanPackageDirectory(
      String packageDirectoryPath,
      Set<Class<? extends Pluggable>> registeredPluginInterfaces,
      Set<String> targetPackages,
      boolean scanSubpackages) {

    // Validate the Plugin Interface.
    validateRegisteredPluginInterfaceArguments(registeredPluginInterfaces);

    // Validate the specified file package directory.
    File pkgDir = new File(packageDirectoryPath);
    if (!pkgDir.exists()) {
      throw new IllegalArgumentException(
          String.format("Path does not exist [%s].", packageDirectoryPath));
    } else if (!pkgDir.isDirectory()) {
      throw new IllegalArgumentException(
          String.format("Path is not a directory [%s].", packageDirectoryPath));
    }

    // Validate the package names.
    validateJavaPackageNames(targetPackages);

    // Track the scan.
    List<ScanLog> scanLogs = new ArrayList<>();

    // Retrieve the package root.
    String packageRoot = pkgDir.getAbsolutePath();

    // Add to scan logs.
    scanLogs.add(new ScanLog(false, false, "DIRECTORY", packageRoot, "", ""));

    // Loop through each package found in the specified package directory.
    Set<File> classFiles = retrieveClassFilesFromDirectoryTree(pkgDir, scanLogs);

    // Load all gathered class definitions into memory.
    URLClassLoader cl;
    try {
      cl = new URLClassLoader(new URL[]{pkgDir.toURI().toURL()});
    } catch (MalformedURLException e) {
      throw new PluginLibraryException("Package directory is invalid.", e);
    }

    // Loop through each class file.
    for (File classFile : classFiles) {

      // Generate the fully qualified class name.
      String fullClassName = classFile.getAbsolutePath()
          .substring(packageRoot.length() + 1)
          .replace("/", ".")   // Replace unix slashes with dots.
          .replace("\\", "."); // Replace Windows backslashes with dots.

      // Remove the .class extension.
      fullClassName = fullClassName.substring(0, fullClassName.lastIndexOf('.'));

      // Check whether this class is contained within a targeted package.
      String pkgName = fullClassName.substring(0, fullClassName.lastIndexOf('.'));
      if (targetPackages == null ||
          (!scanSubpackages && targetPackages.contains(pkgName)) ||
          (scanSubpackages && targetPackages.stream().anyMatch(pkgName::startsWith))) {

        // Load the class definition into memory.
        Class<?> clazz;
        try {
          clazz = cl.loadClass(fullClassName);
        } catch (ClassNotFoundException e) {
          throw new PluginRegistrationException(fullClassName, e);
        }

        // Perform basic plugin validation.
        if (isValidPlugin(clazz)) {

          // Register the plugin.
          boolean pluginRegistered = false;
          for (Class<? extends Pluggable> registeredPluginInterface : registeredPluginInterfaces) {
            if (isValidPlugin(clazz, registeredPluginInterface)) {

              @SuppressWarnings("unchecked")
              Class<? extends Pluggable> pluggableClazz = (Class<? extends Pluggable>) clazz;

              scanLogs.add(registerPlugin(pluggableClazz, registeredPluginInterface));

              pluginRegistered = true;
            }
          }

          // Check if this Plugin failed to match any of the registered Plugin Interfaces.
          if (!pluginRegistered) {
            // Add to scan logs.
            scanLogs.add(new ScanLog(
                false, true, "PLUGIN",
                classFile.getAbsolutePath(),
                fullClassName,
                "Does not implement any of the targeted Plugin Interfaces."));
          }
        } else {
          // Add to scan logs.
          scanLogs.add(new ScanLog(
              false, true, "CLASS",
              classFile.getAbsolutePath(),
              fullClassName,
              "Not a valid Plugin."));
        }
      } else {

        // Add to scan logs.
        scanLogs.add(new ScanLog(
            false, true, "CLASS", classFile.getName(), fullClassName,
            "Not located in a targeted package."));
      }
    }

    return new ScanResults(scanLogs);
  }

  //---------- Plugin Retrieval Methods ----------//


  /**
   * Returns all Plugins registered under the specified Plugin Interface.
   *
   * @param registeredPluginInterface Registered Plugin Interface class.
   * @param <T> Registered Plugin Interface type.
   * @return A set of instantiated Plugins.
   */
  public final <T extends Pluggable> List<T> getAll(
      Class<T> registeredPluginInterface) {

    validateRegisteredPluginInterfaceArgument(registeredPluginInterface);

    List<T> plugins = new ArrayList<>();

    // Loop through all Plugins registered under the specified Plugin Interface.
    for (PluginEntry pluginEntry : pluginRegistry.get(registeredPluginInterface)) {
      plugins.add(pluginEntry.getPluginAsInterface(registeredPluginInterface));
    }
    return plugins;
  }

  /**
   * Returns all versions of the specified Plugin (i.e. all Plugins sharing the specified name),
   * that are registered under the specified Plugin Interface.
   *
   * @param <T> Registered Plugin Interface type.
   * @param pluginName Name of the Plugin.
   * @param registeredPluginInterface Registered Plugin Interface class.
   */
  public final <T extends Pluggable> List<T> getAll(
      String pluginName,
      Class<T> registeredPluginInterface) {

    // Validate input.
    validatePluginNameArgument(pluginName);
    validateRegisteredPluginInterfaceArgument(registeredPluginInterface);

    List<T> plugins = new ArrayList<>();

    // Loop through all Plugins registered under the specified Plugin Interface.
    for (PluginEntry pluginEntry : pluginRegistry.get(registeredPluginInterface)) {
      if (pluginEntry.getPluginName().equals(pluginName)) {
        plugins.add(pluginEntry.getPluginAsInterface(registeredPluginInterface));
      }
    }
    return plugins;
  }

  /**
   * Returns a single Plugin with the specified name and version number, that is registered under
   * the specified Plugin Interface (if one exists). Otherwise, returns null.
   *
   * @param <T> Registered Plugin Interface type.
   * @param pluginName Plugin name.
   * @param registeredPluginInterface Registered Plugin Interface class.
   * @return An instantiated Plugin, or NULL if none was found.
   */
  public final <T extends Pluggable> T get(
      String pluginName,
      int majorVersion, int minorVersion, int buildNumber,
      Class<T> registeredPluginInterface) {

    // Validate input, and create PluginVersion object.
    validatePluginNameArgument(pluginName);
    validateRegisteredPluginInterfaceArgument(registeredPluginInterface);
    PluginVersion pluginVersion = new PluginVersion(majorVersion, minorVersion, buildNumber);

    // Loop through all Plugins registered under the specified Plugin Interface.
    for (PluginEntry pluginEntry : pluginRegistry.get(registeredPluginInterface)) {
      if (pluginEntry.getPluginName().equals(pluginName) &&
          pluginEntry.getPluginVersion().equals(pluginVersion)) {

        return pluginEntry.getPluginAsInterface(registeredPluginInterface);
      }
    }

    return null;
  }

  /**
   * Returns the latest version of a Plugin with the given name, that is registered under the
   * specified Plugin Interface (if one exists). Otherwise, returns null.
   *
   * @param pluginName Plugin name.
   * @param registeredPluginInterface Registered Plugin Interface class.
   * @param <T> Registered Plugin Interface type.
   * @return An instantiated plugin, or NULL if none was found.
   */
  public final <T extends Pluggable> T getLatestVersion(
      String pluginName,
      Class<T> registeredPluginInterface) {

    // Validate input.
    validatePluginNameArgument(pluginName);
    validateRegisteredPluginInterfaceArgument(registeredPluginInterface);

    PluginVersion latestVersion = null;
    PluginEntry targetPluginEntry = null;

    // Loop through all Plugins registered under the specified Plugin Interface.
    for (PluginEntry pluginEntry : pluginRegistry.get(registeredPluginInterface)) {

      // Check the plugin name.
      if (pluginEntry.getPluginName().equals(pluginName)) {

        // Filter for the latest version.
        if (latestVersion == null ||
            latestVersion.compareTo(pluginEntry.getPluginVersion()) < 0) {
          latestVersion = pluginEntry.getPluginVersion();
          targetPluginEntry = pluginEntry;
        }
      }
    }

    return (targetPluginEntry == null) ?
        null :
        targetPluginEntry.getPluginAsInterface(registeredPluginInterface);
  }

  /**
   * Returns the latest version of Plugins registered under the specified Plugin Interface, that
   * contain attributes that satisfy the specified "attributes test".
   *
   * @param pluginName Plugin name.
   * @param attributesTest Predicate that determines whether a Plugin's attributes (provided as a
   * PolymorphicMap) satisfy user-specified conditions.
   * @param registeredPluginInterface Registered Plugin Interface class.
   * @param <T> Registered Plugin Interface type.
   * @return A set of instantiated Plugins.
   */
  public final <T extends Pluggable> T getLatestVersionByAttribute(
      Predicate<PolymorphicMap> attributesTest,
      String pluginName,
      Class<T> registeredPluginInterface) {

    // Validate input.
    Objects.requireNonNull(attributesTest);
    validatePluginNameArgument(pluginName);
    validateRegisteredPluginInterfaceArgument(registeredPluginInterface);

    PluginVersion latestVersion = null;
    PluginEntry targetPluginEntry = null;

    // Loop through all Plugins registered under the specified Plugin Interface.
    for (PluginEntry pluginEntry : pluginRegistry.get(registeredPluginInterface)) {

      // Check the plugin name.
      if (pluginEntry.getPluginName().equals(pluginName)) {

        // Check if this Plugin's attributes pass the "Attributes Test" provided by the user.
        if (attributesTest.test(pluginEntry.getPluginAttributes())) {

          // Filter for the latest version.
          if (latestVersion == null ||
              latestVersion.compareTo(pluginEntry.getPluginVersion()) < 0) {
            latestVersion = pluginEntry.getPluginVersion();
            targetPluginEntry = pluginEntry;
          }
        }
      }
    }

    return (targetPluginEntry == null) ?
        null :
        targetPluginEntry.getPluginAsInterface(registeredPluginInterface);
  }

  /**
   * Returns the latest version of Plugins registered under the specified Plugin Interface, that
   * contain attributes that satisfy the specified "attributes biPredicate".
   *
   * @param pluginName Plugin name.
   * @param biPredicate Predicate that determines whether a Plugin's attributes (provided as a
   * PolymorphicMap) and its Plugin Version satisfy user-specified conditions.
   * @param registeredPluginInterface Registered Plugin Interface class.
   * @param <T> Registered Plugin Interface type.
   * @return A set of instantiated Plugins.
   */
  public final <T extends Pluggable> T getLatestVersionByAttribute(
      BiPredicate<PolymorphicMap, PluginVersion> biPredicate,
      String pluginName,
      Class<T> registeredPluginInterface) {

    // Validate input.
    Objects.requireNonNull(biPredicate);
    validatePluginNameArgument(pluginName);
    validateRegisteredPluginInterfaceArgument(registeredPluginInterface);

    PluginVersion latestVersion = null;
    PluginEntry targetPluginEntry = null;

    // Loop through all Plugins registered under the specified Plugin Interface.
    for (PluginEntry pluginEntry : pluginRegistry.get(registeredPluginInterface)) {

      // Check the plugin name.
      if (pluginEntry.getPluginName().equals(pluginName)) {

        // Check if this Plugin's attributes pass the "Attributes & Plugin Version Test" provided by the user.
        if (biPredicate.test(pluginEntry.getPluginAttributes(), pluginEntry.getPluginVersion())) {

          // Filter for the latest version.
          if (latestVersion == null ||
              latestVersion.compareTo(pluginEntry.getPluginVersion()) < 0) {
            latestVersion = pluginEntry.getPluginVersion();
            targetPluginEntry = pluginEntry;
          }
        }
      }
    }

    return (targetPluginEntry == null) ?
        null :
        targetPluginEntry.getPluginAsInterface(registeredPluginInterface);
  }

  /**
   * Returns the latest version of Plugins registered under the specified Plugin Interface.
   *
   * @param registeredPluginInterface Registered Plugin Interface class.
   * @param <T> Registered Plugin Interface type.
   * @return Set of instantiated plugins.
   */
  public final <T extends Pluggable> List<T> getLatestVersions(
      Class<T> registeredPluginInterface) {

    // Validate input.
    validateRegisteredPluginInterfaceArgument(registeredPluginInterface);

    Map<String, PluginEntry> latestPluginMap = new HashMap<>();

    // Loop through all Plugins registered under the specified Plugin Interface.
    for (PluginEntry pluginEntry : pluginRegistry.get(registeredPluginInterface)) {

      PluginEntry currentLatest = latestPluginMap.get(pluginEntry.getPluginName());

      // Filter for the latest version.
      if (currentLatest == null ||
          currentLatest.getPluginVersion().compareTo(pluginEntry.getPluginVersion()) < 0) {
        latestPluginMap.put(pluginEntry.getPluginName(), pluginEntry);
      }
    }

    return latestPluginMap.values().stream()
        .map(x -> x.getPluginAsInterface(registeredPluginInterface))
        .collect(Collectors.toList());
  }

  /**
   * Returns the latest version of Plugins registered under the specified Plugin Interface, that
   * contain attributes that satisfy the specified "attributes test".
   *
   * @param attributesTest Predicate that determines whether a Plugin's attributes (provided as a
   * PolymorphicMap) satisfy user-specified conditions.
   * @param registeredPluginInterface Registered Plugin Interface class.
   * @param <T> Registered Plugin Interface type.
   * @return A set of instantiated Plugins.
   */
  public final <T extends Pluggable> List<T> getLatestVersionsByAttribute(
      Predicate<PolymorphicMap> attributesTest,
      Class<T> registeredPluginInterface) {

    // Validate input.
    Objects.requireNonNull(attributesTest);
    validateRegisteredPluginInterfaceArgument(registeredPluginInterface);

    Map<String, PluginEntry> latestPluginMap = new HashMap<>();

    // Loop through all Plugins registered under the specified Plugin Interface.
    for (PluginEntry pluginEntry : pluginRegistry.get(registeredPluginInterface)) {

      // Check if this Plugin's attributes pass the "Attributes Test" provided by the user.
      if (attributesTest.test(pluginEntry.getPluginAttributes())) {

        PluginEntry currentLatest = latestPluginMap.get(pluginEntry.getPluginName());

        // Filter for the latest version.
        if (currentLatest == null ||
            currentLatest.getPluginVersion().compareTo(pluginEntry.getPluginVersion()) < 0) {
          latestPluginMap.put(pluginEntry.getPluginName(), pluginEntry);
        }
      }
    }

    return latestPluginMap.values().stream()
        .map(x -> x.getPluginAsInterface(registeredPluginInterface))
        .collect(Collectors.toList());
  }

  /**
   * Returns the latest version of Plugins registered under the specified Plugin Interface, that
   * contain attributes that satisfy the specified "attributes biPredicate".
   *
   * @param biPredicate Predicate that determines whether a Plugin's attributes (provided as a
   * PolymorphicMap) and its Plugin Version satisfy user-specified conditions.
   * @param registeredPluginInterface Registered Plugin Interface class.
   * @param <T> Registered Plugin Interface type.
   * @return A set of instantiated Plugins.
   */
  public final <T extends Pluggable> List<T> getLatestVersionsByAttribute(
      BiPredicate<PolymorphicMap, PluginVersion> biPredicate,
      Class<T> registeredPluginInterface) {

    // Validate input.
    Objects.requireNonNull(biPredicate);
    validateRegisteredPluginInterfaceArgument(registeredPluginInterface);

    Map<String, PluginEntry> latestPluginMap = new HashMap<>();

    // Loop through all Plugins registered under the specified Plugin Interface.
    for (PluginEntry pluginEntry : pluginRegistry.get(registeredPluginInterface)) {

      // Check if this Plugin's attributes pass the "Attributes & Plugin Version Test" provided by the user.
      if (biPredicate.test(pluginEntry.getPluginAttributes(), pluginEntry.getPluginVersion())) {

        PluginEntry currentLatest = latestPluginMap.get(pluginEntry.getPluginName());

        // Filter for the latest version.
        if (currentLatest == null ||
            currentLatest.getPluginVersion().compareTo(pluginEntry.getPluginVersion()) < 0) {
          latestPluginMap.put(pluginEntry.getPluginName(), pluginEntry);
        }
      }
    }

    return latestPluginMap.values().stream()
        .map(x -> x.getPluginAsInterface(registeredPluginInterface))
        .collect(Collectors.toList());
  }

  /**
   * Returns all Plugins registered under the specified Plugin Interface, that contain attributes
   * that satisfy the specified "attributes test".
   *
   * @param attributesTest Predicate that determines whether a Plugin's attributes (provided as a
   * PolymorphicMap) satisfy user-specified conditions.
   * @param registeredPluginInterface Registered Plugin Interface class.
   * @param <T> Registered Plugin Interface type.
   * @return A set of instantiated Plugins.
   */
  public final <T extends Pluggable> List<T> getByAttribute(
      Predicate<PolymorphicMap> attributesTest,
      Class<T> registeredPluginInterface) {

    // Validate input.
    Objects.requireNonNull(attributesTest);
    validateRegisteredPluginInterfaceArgument(registeredPluginInterface);

    List<T> results = new ArrayList<>();

    // Loop through all Plugins registered under the specified Plugin Interface.
    for (PluginEntry pluginEntry : pluginRegistry.get(registeredPluginInterface)) {

      // Check if this Plugin's attributes pass the "Attributes Test" provided by the user.
      if (attributesTest.test(pluginEntry.getPluginAttributes())) {
        results.add(pluginEntry.getPluginAsInterface(registeredPluginInterface));
      }
    }

    return results;
  }

  /**
   * Returns all Plugins registered under the specified Plugin Interface, that contain attributes
   * that satisfy the specified "attributes and plugin version test".
   *
   * @param biPredicate Predicate that determines whether a Plugin's attributes (provided as a
   * PolymorphicMap) and its Plugin Version satisfy user-specified conditions.
   * @param registeredPluginInterface Registered Plugin Interface class.
   * @param <T> Registered Plugin Interface type.
   * @return A set of instantiated Plugins.
   */
  public final <T extends Pluggable> List<T> getByAttribute(
      BiPredicate<PolymorphicMap, PluginVersion> biPredicate,
      Class<T> registeredPluginInterface) {

    // Validate input.
    Objects.requireNonNull(biPredicate);
    validateRegisteredPluginInterfaceArgument(registeredPluginInterface);

    List<T> results = new ArrayList<>();

    // Loop through all Plugins registered under the specified Plugin Interface.
    for (PluginEntry pluginEntry : pluginRegistry.get(registeredPluginInterface)) {

      // Check if this Plugin's attributes pass the "Attributes Test" provided by the user.
      if (biPredicate.test(pluginEntry.getPluginAttributes(), pluginEntry.getPluginVersion())) {
        results.add(pluginEntry.getPluginAsInterface(registeredPluginInterface));
      }
    }

    return results;
  }

  /**
   * Returns all Plugins registered under the specified Plugin Interface, with the specified name,
   * that contain attributes that satisfy the specified "attributes test".
   *
   * @param attributesTest Predicate that determines whether a Plugin's attributes (provided as a
   * PolymorphicMap) satisfy user-specified conditions.
   * @param pluginName Plugin name.
   * @param registeredPluginInterface Registered Plugin Interface class.
   * @param <T> Registered Plugin Interface type.
   * @return A set of instantiated Plugins.
   */
  public final <T extends Pluggable> List<T> getByAttribute(
      Predicate<PolymorphicMap> attributesTest,
      String pluginName,
      Class<T> registeredPluginInterface) {

    // Validate input.
    Objects.requireNonNull(attributesTest);
    validatePluginNameArgument(pluginName);
    validateRegisteredPluginInterfaceArgument(registeredPluginInterface);

    List<T> results = new ArrayList<>();

    // Loop through all Plugins registered under the specified Plugin Interface.
    for (PluginEntry pluginEntry : pluginRegistry.get(registeredPluginInterface)) {

      // Check the plugin name.
      if (pluginEntry.getPluginName().equals(pluginName)) {

        // Check if this Plugin's attributes pass the "Attributes Test" provided by the user.
        if (attributesTest.test(pluginEntry.getPluginAttributes())) {
          results.add(pluginEntry.getPluginAsInterface(registeredPluginInterface));
        }
      }
    }

    return results;
  }

  /**
   * Returns all Plugins registered under the specified Plugin Interface, with the specified name,
   * that contain attributes that satisfy the specified "attributes and plugin version test".
   *
   * @param biPredicate Predicate that determines whether a Plugin's attributes (provided as a
   * PolymorphicMap) and its Plugin Version satisfy user-specified conditions.
   * @param pluginName Plugin name.
   * @param registeredPluginInterface Registered Plugin Interface class.
   * @param <T> Registered Plugin Interface type.
   * @return A set of instantiated Plugins.
   */
  public final <T extends Pluggable> List<T> getByAttribute(
      BiPredicate<PolymorphicMap, PluginVersion> biPredicate,
      String pluginName,
      Class<T> registeredPluginInterface) {

    // Validate input.
    Objects.requireNonNull(biPredicate);
    validatePluginNameArgument(pluginName);
    validateRegisteredPluginInterfaceArgument(registeredPluginInterface);

    List<T> results = new ArrayList<>();

    // Loop through all Plugins registered under the specified Plugin Interface.
    for (PluginEntry pluginEntry : pluginRegistry.get(registeredPluginInterface)) {

      // Check the plugin name.
      if (pluginEntry.getPluginName().equals(pluginName)) {

        // Check if this Plugin's attributes pass the "Attributes Test" provided by the user.
        if (biPredicate.test(pluginEntry.getPluginAttributes(), pluginEntry.getPluginVersion())) {
          results.add(pluginEntry.getPluginAsInterface(registeredPluginInterface));
        }
      }
    }

    return results;
  }

  //---------- Plugin Attribute Methods ----------//


  /**
   * Returns the list of attribute keys associated with the specified Plugin (version number is
   * assumed to be 0.0.0).
   *
   * @param pluginName Plugin name.
   * @param registeredPluginInterface Registered Plugin Interface class.
   * @param <T> Registered Plugin Interface type.
   * @return List of attribute keys.
   */
  public final <T extends Pluggable> int getPluginAttributeCount(
      String pluginName,
      Class<T> registeredPluginInterface) {

    return getPluginAttributeCount(
        pluginName,
        new PluginVersion(0, 0, 0),
        registeredPluginInterface);
  }

  /**
   * Returns the list of attribute keys associated with the specified Plugin.
   *
   * @param pluginName Plugin name.
   * @param majorVersion Major version.
   * @param minorVersion Minor version.
   * @param buildNumber Build number.
   * @param registeredPluginInterface Registered Plugin Interface class.
   * @param <T> Registered Plugin Interface type.
   * @return List of attribute keys.
   */
  public final <T extends Pluggable> int getPluginAttributeCount(
      String pluginName,
      int majorVersion, int minorVersion, int buildNumber,
      Class<T> registeredPluginInterface) {

    return getPluginAttributeCount(
        pluginName,
        new PluginVersion(majorVersion, minorVersion, buildNumber),
        registeredPluginInterface);
  }

  /**
   * Returns the list of attribute keys associated with the specified Plugin.
   *
   * @param pluginName Plugin name.
   * @param pluginVersion Plugin version.
   * @param registeredPluginInterface Registered Plugin Interface class.
   * @param <T> Registered Plugin Interface type.
   * @return List of attribute keys.
   */
  public final <T extends Pluggable> int getPluginAttributeCount(
      String pluginName,
      PluginVersion pluginVersion,
      Class<T> registeredPluginInterface) {

    // Validate input, and create the PluginVersion object.
    validatePluginNameArgument(pluginName);
    Objects.requireNonNull(pluginVersion);
    validateRegisteredPluginInterfaceArgument(registeredPluginInterface);

    // Retrieve the specified Plugin Entry.
    PluginEntry pluginEntry = getPluginEntry(pluginName, pluginVersion, registeredPluginInterface);

    // Return the set of attribute keys.
    return pluginEntry.getPluginAttributes().size();
  }

  /**
   * Returns the list of attribute keys associated with the specified Plugin (version number is
   * assumed to be 0.0.0).
   *
   * @param pluginName Plugin name.
   * @param registeredPluginInterface Registered Plugin Interface class.
   * @param <T> Registered Plugin Interface type.
   * @return List of attribute keys.
   */
  public final <T extends Pluggable> Set<String> getPluginAttributeNames(
      String pluginName,
      Class<T> registeredPluginInterface) {

    return getPluginAttributeNames(
        pluginName,
        new PluginVersion(0, 0, 0),
        registeredPluginInterface);
  }

  /**
   * Returns the list of attribute keys associated with the specified Plugin.
   *
   * @param pluginName Plugin name.
   * @param majorVersion Major version.
   * @param minorVersion Minor version.
   * @param buildNumber Build number.
   * @param registeredPluginInterface Registered Plugin Interface class.
   * @param <T> Registered Plugin Interface type.
   * @return List of attribute keys.
   */
  public final <T extends Pluggable> Set<String> getPluginAttributeNames(
      String pluginName,
      int majorVersion, int minorVersion, int buildNumber,
      Class<T> registeredPluginInterface) {

    return getPluginAttributeNames(
        pluginName,
        new PluginVersion(majorVersion, minorVersion, buildNumber),
        registeredPluginInterface);
  }

  /**
   * Returns the list of attribute keys associated with the specified Plugin.
   *
   * @param pluginName Plugin name.
   * @param pluginVersion Plugin version.
   * @param registeredPluginInterface Registered Plugin Interface class.
   * @param <T> Registered Plugin Interface type.
   * @return List of attribute keys.
   */
  public final <T extends Pluggable> Set<String> getPluginAttributeNames(
      String pluginName,
      PluginVersion pluginVersion,
      Class<T> registeredPluginInterface) {

    // Validate input, and create the PluginVersion object.
    validatePluginNameArgument(pluginName);
    Objects.requireNonNull(pluginVersion);
    validateRegisteredPluginInterfaceArgument(registeredPluginInterface);

    // Retrieve the specified Plugin Entry.
    PluginEntry pluginEntry = getPluginEntry(pluginName, pluginVersion, registeredPluginInterface);

    // Return the set of attribute keys.
    return pluginEntry.getPluginAttributes().keySet();
  }

  /**
   * Returns true if the specified attribute exists on the specified Plugin (version number is
   * assumed to be 0.0.0).
   *
   * @param pluginName Plugin name.
   * @param registeredPluginInterface Registered Plugin Interface class.
   * @param attributeName Attribute name.
   * @param <T> Registered Plugin Interface type.
   * @return True if the attribute exists on the specified plugin, false otherwise.
   */
  public final <T extends Pluggable> boolean pluginAttributeExists(
      String pluginName,
      Class<T> registeredPluginInterface,
      String attributeName) {

    return pluginAttributeExists(
        pluginName,
        new PluginVersion(0, 0, 0),
        registeredPluginInterface,
        attributeName);
  }

  /**
   * Returns true if the specified attribute exists on the specified Plugin.
   *
   * @param pluginName Plugin name.
   * @param majorVersion Major version.
   * @param minorVersion Minor version.
   * @param buildNumber Build number.
   * @param registeredPluginInterface Registered Plugin Interface class.
   * @param attributeName Attribute name.
   * @param <T> Registered Plugin Interface type.
   * @return True if the attribute exists on the specified plugin, false otherwise.
   */
  public final <T extends Pluggable> boolean pluginAttributeExists(
      String pluginName,
      int majorVersion, int minorVersion, int buildNumber,
      Class<T> registeredPluginInterface,
      String attributeName) {

    return pluginAttributeExists(
        pluginName,
        new PluginVersion(majorVersion, minorVersion, buildNumber),
        registeredPluginInterface,
        attributeName);
  }

  /**
   * Returns true if the specified attribute exists on the specified Plugin.
   *
   * @param pluginName Plugin name.
   * @param pluginVersion Plugin version.
   * @param registeredPluginInterface Registered Plugin Interface class.
   * @param attributeName Attribute name.
   * @param <T> Registered Plugin Interface type.
   * @return True if the attribute exists on the specified plugin, false otherwise.
   */
  public final <T extends Pluggable> boolean pluginAttributeExists(
      String pluginName,
      PluginVersion pluginVersion,
      Class<T> registeredPluginInterface,
      String attributeName) {

    // Validate input, and create the PluginVersion object.
    validatePluginNameArgument(pluginName);
    Objects.requireNonNull(pluginVersion);
    validateRegisteredPluginInterfaceArgument(registeredPluginInterface);
    Objects.requireNonNull(attributeName);

    // Retrieve the specified Plugin Entry.
    PluginEntry pluginEntry = getPluginEntry(pluginName, pluginVersion, registeredPluginInterface);

    // Return true if the key exists, false otherwise.
    return pluginEntry.getPluginAttributes().containsKey(attributeName);
  }

  /**
   * Returns the type of the value associated with the specified plugin attribute key (version
   * number is assumed to be 0.0.0).
   *
   * @param pluginName Plugin name.
   * @param registeredPluginInterface Registered Plugin Interface class.
   * @return Type of the value associated with the given attribute key.
   */
  public final <T extends Pluggable> Class<?> getPluginAttributeType(
      String pluginName,
      Class<T> registeredPluginInterface,
      String attributeName) {

    return getPluginAttributeType(
        pluginName,
        new PluginVersion(0, 0, 0),
        registeredPluginInterface,
        attributeName);
  }

  /**
   * Returns the type of the value associated with the specified plugin attribute key.
   *
   * @param pluginName Plugin name.
   * @param majorVersion Major version.
   * @param minorVersion Minor version.
   * @param buildNumber Build number.
   * @param registeredPluginInterface Registered Plugin Interface class.
   * @return Type of the value associated with the given attribute key.
   */
  public final <T extends Pluggable> Class<?> getPluginAttributeType(
      String pluginName,
      int majorVersion, int minorVersion, int buildNumber,
      Class<T> registeredPluginInterface,
      String attributeName) {

    return getPluginAttributeType(
        pluginName,
        new PluginVersion(majorVersion, minorVersion, buildNumber),
        registeredPluginInterface,
        attributeName);
  }

  /**
   * Returns the type of the value associated with the specified plugin attribute key.
   *
   * @param pluginName Plugin name.
   * @param pluginVersion Plugin version.
   * @param registeredPluginInterface Registered Plugin Interface class.
   * @return Type of the value associated with the given attribute key.
   */
  public final <T extends Pluggable> Class<?> getPluginAttributeType(
      String pluginName,
      PluginVersion pluginVersion,
      Class<T> registeredPluginInterface,
      String attributeName) {

    // Validate input, and create the PluginVersion object.
    validatePluginNameArgument(pluginName);
    Objects.requireNonNull(pluginVersion);
    validateRegisteredPluginInterfaceArgument(registeredPluginInterface);
    Objects.requireNonNull(attributeName);

    // Retrieve the specified Plugin Entry.
    PluginEntry pluginEntry = getPluginEntry(pluginName, pluginVersion, registeredPluginInterface);

    // Return the type of the specified attribute.
    return pluginEntry.getPluginAttributes().getType(attributeName);
  }

  /**
   * Returns the specified attribute associated with the specified Plugin, cast to the given type
   * (version number is assumed to be 0.0.0).
   *
   * @param pluginName Plugin name.
   * @param registeredPluginInterface Registered Plugin Interface class.
   * @param attributeName Attribute name.
   * @param attributeValueType Class that the attribute value is to be casted to.
   * @param <T> Registered Plugin Interface type.
   * @param <V> Type that the attribute value is to be casted to.
   * @return Plugin attribute value.
   */
  public final <T extends Pluggable, V> V getPluginAttribute(
      String pluginName,
      Class<T> registeredPluginInterface,
      String attributeName,
      Class<V> attributeValueType) {

    return getPluginAttribute(
        pluginName,
        new PluginVersion(0, 0, 0),
        registeredPluginInterface,
        attributeName,
        attributeValueType);
  }

  /**
   * Returns the specified attribute associated with the specified Plugin, cast to the given type.
   *
   * @param pluginName Plugin name.
   * @param majorVersion Major version.
   * @param minorVersion Minor version.
   * @param buildNumber Build number.
   * @param registeredPluginInterface Registered Plugin Interface class.
   * @param attributeName Attribute name.
   * @param attributeValueType Class that the attribute value is to be casted to.
   * @param <T> Registered Plugin Interface type.
   * @param <V> Type that the attribute value is to be casted to.
   * @return Plugin attribute value.
   */
  public final <T extends Pluggable, V> V getPluginAttribute(
      String pluginName,
      int majorVersion, int minorVersion, int buildNumber,
      Class<T> registeredPluginInterface,
      String attributeName,
      Class<V> attributeValueType) {

    return getPluginAttribute(
        pluginName,
        new PluginVersion(majorVersion, minorVersion, buildNumber),
        registeredPluginInterface,
        attributeName,
        attributeValueType);
  }

  /**
   * Returns the specified attribute associated with the specified Plugin, cast to the given type.
   *
   * @param pluginName Plugin name.
   * @param pluginVersion Plugin version.
   * @param registeredPluginInterface Registered Plugin Interface class.
   * @param attributeName Attribute name.
   * @param attributeValueType Class that the attribute value is to be casted to.
   * @param <T> Registered Plugin Interface type.
   * @param <V> Type that the attribute value is to be casted to.
   * @return Plugin attribute value.
   */
  public final <T extends Pluggable, V> V getPluginAttribute(
      String pluginName,
      PluginVersion pluginVersion,
      Class<T> registeredPluginInterface,
      String attributeName,
      Class<V> attributeValueType) {

    // Validate input, and create the PluginVersion object.
    validatePluginNameArgument(pluginName);
    Objects.requireNonNull(pluginVersion);
    validateRegisteredPluginInterfaceArgument(registeredPluginInterface);
    Objects.requireNonNull(attributeName);

    // Retrieve the specified Plugin Entry.
    PluginEntry pluginEntry = getPluginEntry(pluginName, pluginVersion, registeredPluginInterface);

    // Return the value of the specified attribute.
    return pluginEntry.getPluginAttributes().get(attributeName, attributeValueType);
  }

  /**
   * Returns the specified attribute associated with the specified Plugin, cast to the given type;
   * if-and-only-if the value is of exactly the same type (version number is assumed to be 0.0.0).
   *
   * @param pluginName Plugin name.
   * @param registeredPluginInterface Registered Plugin Interface class.
   * @param attributeName Attribute name.
   * @param attributeValueType Exact type that the attribute value is expected to be.
   * @param <T> Registered Plugin Interface type.
   * @param <V> Exact type that the attribute value is expected to be.
   * @return Plugin attribute value.
   */
  public final <T extends Pluggable, V> V getExactPluginAttribute(
      String pluginName,
      Class<T> registeredPluginInterface,
      String attributeName,
      Class<V> attributeValueType) {

    return getExactPluginAttribute(
        pluginName,
        new PluginVersion(0, 0, 0),
        registeredPluginInterface,
        attributeName,
        attributeValueType);
  }

  /**
   * Returns the specified attribute associated with the specified Plugin, cast to the given type;
   * if-and-only-if the value is of exactly the same type.
   *
   * @param pluginName Plugin name.
   * @param majorVersion Major version.
   * @param minorVersion Minor version.
   * @param buildNumber Build number.
   * @param registeredPluginInterface Registered Plugin Interface class.
   * @param attributeName Attribute name.
   * @param attributeValueType Exact type that the attribute value is expected to be.
   * @param <T> Registered Plugin Interface type.
   * @param <V> Exact type that the attribute value is expected to be.
   * @return Plugin attribute value.
   */
  public final <T extends Pluggable, V> V getExactPluginAttribute(
      String pluginName,
      int majorVersion, int minorVersion, int buildNumber,
      Class<T> registeredPluginInterface,
      String attributeName,
      Class<V> attributeValueType) {

    return getExactPluginAttribute(
        pluginName,
        new PluginVersion(majorVersion, minorVersion, buildNumber),
        registeredPluginInterface,
        attributeName,
        attributeValueType);
  }

  /**
   * Returns the specified attribute associated with the specified Plugin, cast to the given type;
   * if-and-only-if the value is of exactly the same type.
   *
   * @param pluginName Plugin name.
   * @param pluginVersion Plugin version.
   * @param registeredPluginInterface Registered Plugin Interface class.
   * @param attributeName Attribute name.
   * @param attributeValueType Exact type that the attribute value is expected to be.
   * @param <T> Registered Plugin Interface type.
   * @param <V> Exact type that the attribute value is expected to be.
   * @return Plugin attribute value.
   */
  public final <T extends Pluggable, V> V getExactPluginAttribute(
      String pluginName,
      PluginVersion pluginVersion,
      Class<T> registeredPluginInterface,
      String attributeName,
      Class<V> attributeValueType) {

    // Validate input, and create the PluginVersion object.
    validatePluginNameArgument(pluginName);
    Objects.requireNonNull(pluginVersion);
    validateRegisteredPluginInterfaceArgument(registeredPluginInterface);
    Objects.requireNonNull(attributeName);

    // Retrieve the specified Plugin Entry.
    PluginEntry pluginEntry = getPluginEntry(pluginName, pluginVersion, registeredPluginInterface);

    // Return the value of the specified attribute.
    return pluginEntry.getPluginAttributes().getExact(attributeName, attributeValueType);
  }

  /**
   * Attaches the specified attribute value to the specified Plugin (replacing an existing value).
   * (Version number is assumed to be 0.0.0)
   *
   * @param pluginName Plugin name.
   * @param registeredPluginInterface Registered Plugin Interface class.
   * @param attributeName Attribute name.
   * @param attributeValue Attribute value.
   * @param <T> Registered Plugin Interface type.
   * @param <V> Type of the attribute value.
   */
  public final <T extends Pluggable, V> V putPluginAttribute(
      String pluginName,
      Class<T> registeredPluginInterface,
      String attributeName,
      V attributeValue) {

    return putPluginAttribute(
        pluginName,
        new PluginVersion(0, 0, 0),
        registeredPluginInterface,
        attributeName,
        attributeValue);
  }

  /**
   * Attaches the specified attribute value to the specified Plugin (replacing an existing value).
   *
   * @param pluginName Plugin name.
   * @param majorVersion Major version.
   * @param minorVersion Minor version.
   * @param buildNumber Build number.
   * @param registeredPluginInterface Registered Plugin Interface class.
   * @param attributeName Attribute name.
   * @param attributeValue Attribute value.
   * @param <T> Registered Plugin Interface type.
   * @param <V> Type of the attribute value.
   */
  public final <T extends Pluggable, V> V putPluginAttribute(
      String pluginName,
      int majorVersion, int minorVersion, int buildNumber,
      Class<T> registeredPluginInterface,
      String attributeName,
      V attributeValue) {

    return putPluginAttribute(
        pluginName,
        new PluginVersion(majorVersion, minorVersion, buildNumber),
        registeredPluginInterface,
        attributeName,
        attributeValue);
  }

  /**
   * Attaches the specified attribute value to the specified Plugin (replacing an existing value).
   *
   * @param pluginName Plugin name.
   * @param pluginVersion Plugin version.
   * @param registeredPluginInterface Registered Plugin Interface class.
   * @param attributeName Attribute name.
   * @param attributeValue Attribute value.
   * @param <T> Registered Plugin Interface type.
   * @param <V> Type of the attribute value.
   */
  public final <T extends Pluggable, V> V putPluginAttribute(
      String pluginName,
      PluginVersion pluginVersion,
      Class<T> registeredPluginInterface,
      String attributeName,
      V attributeValue) {

    // Validate input, and create the PluginVersion object.
    validatePluginNameArgument(pluginName);
    Objects.requireNonNull(pluginVersion);
    validateRegisteredPluginInterfaceArgument(registeredPluginInterface);
    Objects.requireNonNull(attributeName);
    Objects.requireNonNull(attributeValue);

    // Retrieve the specified Plugin Entry.
    PluginEntry pluginEntry = getPluginEntry(pluginName, pluginVersion, registeredPluginInterface);

    // Return the value of the specified attribute.
    return pluginEntry.getPluginAttributes().put(attributeName, attributeValue);
  }

  /**
   * Attaches the specified attribute value to the specified Plugin (replacing an existing value).
   * (Version number is assumed to be 0.0.0)
   *
   * @param pluginName Plugin name.
   * @param registeredPluginInterface Registered Plugin Interface class.
   * @param attributeName Attribute name.
   * @param attributeValue Attribute value.
   * @param previousAttributeValueType Type of the attribute value being replaced.
   * @param <T> Registered Plugin Interface type.
   * @param <V> Type of the attribute value.
   * @param <R> Type of the previous attribute value, or null if none existed.
   */
  public final <T extends Pluggable, V, R> R putPluginAttribute(
      String pluginName,
      Class<T> registeredPluginInterface,
      String attributeName,
      V attributeValue,
      Class<R> previousAttributeValueType) {

    return putPluginAttribute(
        pluginName,
        new PluginVersion(0, 0, 0),
        registeredPluginInterface,
        attributeName,
        attributeValue,
        previousAttributeValueType);
  }

  /**
   * Attaches the specified attribute value to the specified Plugin (replacing an existing value).
   *
   * @param pluginName Plugin name.
   * @param majorVersion Major version.
   * @param minorVersion Minor version.
   * @param buildNumber Build number.
   * @param registeredPluginInterface Registered Plugin Interface class.
   * @param attributeName Attribute name.
   * @param attributeValue Attribute value.
   * @param previousAttributeValueType Type of the attribute value being replaced.
   * @param <T> Registered Plugin Interface type.
   * @param <V> Type of the attribute value.
   * @param <R> Type of the previous attribute value, or null if none existed.
   */
  public final <T extends Pluggable, V, R> R putPluginAttribute(
      String pluginName,
      int majorVersion, int minorVersion, int buildNumber,
      Class<T> registeredPluginInterface,
      String attributeName,
      V attributeValue,
      Class<R> previousAttributeValueType) {

    return putPluginAttribute(
        pluginName,
        new PluginVersion(majorVersion, minorVersion, buildNumber),
        registeredPluginInterface,
        attributeName,
        attributeValue,
        previousAttributeValueType);
  }

  /**
   * Attaches the specified attribute value to the specified Plugin (replacing an existing value).
   *
   * @param pluginName Plugin name.
   * @param pluginVersion Plugin version.
   * @param registeredPluginInterface Registered Plugin Interface class.
   * @param attributeName Attribute name.
   * @param attributeValue Attribute value.
   * @param previousAttributeValueType Type of the attribute value being replaced.
   * @param <T> Registered Plugin Interface type.
   * @param <V> Type of the attribute value.
   * @param <R> Type of the previous attribute value, or null if none existed.
   */
  public final <T extends Pluggable, V, R> R putPluginAttribute(
      String pluginName,
      PluginVersion pluginVersion,
      Class<T> registeredPluginInterface,
      String attributeName,
      V attributeValue,
      Class<R> previousAttributeValueType) {

    // Validate input, and create the PluginVersion object.
    validatePluginNameArgument(pluginName);
    Objects.requireNonNull(pluginVersion);
    validateRegisteredPluginInterfaceArgument(registeredPluginInterface);
    Objects.requireNonNull(attributeName);
    Objects.requireNonNull(attributeValue);
    Objects.requireNonNull(previousAttributeValueType);

    // Retrieve the specified Plugin Entry.
    PluginEntry pluginEntry = getPluginEntry(pluginName, pluginVersion, registeredPluginInterface);

    // Return the value of the specified attribute.
    return pluginEntry.getPluginAttributes()
        .put(attributeName, attributeValue, previousAttributeValueType);
  }

  //---------- Plugin Statistics Methods ----------//


  /**
   * Returns the number of Plugins registered, across all registered Plugin Interfaces.
   *
   * @return Total Plugins registered.
   */
  public final int count() {
    int total = 0;
    for (Class<? extends Pluggable> registeredPluginInterface : pluginRegistry.keySet()) {
      total += this.count(registeredPluginInterface);
    }
    return total;
  }

  /**
   * Returns the number of Plugins registered under the specified Plugin Interface.
   *
   * @return Total Plugins registered under the specified Plugin Interface.
   */
  public final int count(
      Class<? extends Pluggable> registeredPluginInterface) {

    // Validate the Plugin Interface.
    validateRegisteredPluginInterfaceArgument(registeredPluginInterface);

    return pluginRegistry.get(registeredPluginInterface).size();
  }

  /**
   * Returns the number of Plugins with the specified name, that are registered under the specified
   * Plugin Interface.
   *
   * @return Total Plugins with the specified name, registered under the specified Plugin Interface.
   */
  public final int count(
      String pluginName,
      Class<? extends Pluggable> registeredPluginInterface) {

    // Validate input.
    validatePluginNameArgument(pluginName);
    validateRegisteredPluginInterfaceArgument(registeredPluginInterface);

    // Loop through each Plugin registered under the specified Plugin Interface.
    int total = 0;
    for (PluginEntry pluginEntry : pluginRegistry.get(registeredPluginInterface)) {
      if (pluginEntry.getPluginName().equals(pluginName)) {
        total++;
      }
    }

    return total;
  }

  //---------- Registry State Methods ----------//


  /**
   * Returns a string representation of the state of the Plugin Registry.
   *
   * @return Representation of the state of the Plugin Registry.
   */
  public String toRegistryState() {
    return toRegistryState(false);
  }

  /**
   * Returns a string representation of the state of the Plugin Registry.
   *
   * @param useCanonicalNames When true, expresses the full class name of the Plugin Interface.
   * @return Representation of the state of the Plugin Registry.
   */
  public String toRegistryState(boolean useCanonicalNames) {
    Map<String, ArrayList<String>> contents = getSortedRegistryContents(useCanonicalNames);
    final String FIELD_HEADER_1 = "PLUGIN INTERFACE";
    final String FIELD_HEADER_2 = "PLUGINS";

    int firstFieldLength = contents.keySet().stream()
        .map(String::length)
        .max(Integer::compare)
        .orElse(0);
    if (firstFieldLength < FIELD_HEADER_1.length()) {
      firstFieldLength = FIELD_HEADER_1.length();
    }

    String firstFieldTemplate = "| %-" + firstFieldLength + "s | ";

    int secondFieldLength = contents.values().stream()
        .flatMap(Collection::stream)
        .map(String::length)
        .max(Integer::compare)
        .orElse(0);
    if (secondFieldLength < FIELD_HEADER_2.length()) {
      secondFieldLength = FIELD_HEADER_2.length();
    }

    String secondFieldTemplate = "%-" + secondFieldLength + "s |";

    int tableWidth = 7 + firstFieldLength + secondFieldLength;

    StringBuilder sb = new StringBuilder();
    sb.append("-".repeat(tableWidth)).append("\n");
    sb.append(String.format(firstFieldTemplate, FIELD_HEADER_1));
    sb.append(String.format(secondFieldTemplate, FIELD_HEADER_2)).append("\n");
    sb.append("-".repeat(tableWidth)).append("\n");

    if (contents.isEmpty()) {
      sb.append("|").append(" ".repeat(tableWidth - 2)).append("|\n");
    } else {
      for (String pluginInterface : contents.keySet()) {
        sb.append(String.format(firstFieldTemplate, pluginInterface));
        if (contents.get(pluginInterface).size() <= 0) {
          sb.append(String.format(secondFieldTemplate, "")).append("\n");
        } else {
          boolean firstIteration = true;
          for (String plugin : contents.get(pluginInterface)) {
            if (!firstIteration) {
              sb.append(String.format(firstFieldTemplate, ""));
            }
            sb.append(String.format(secondFieldTemplate, plugin)).append("\n");
            firstIteration = false;
          }
        }
      }
    }

    sb.append("-".repeat(tableWidth)).append("\n");

    return sb.toString();
  }

  /**
   * Print the state of the Plugin Registry to standard out.
   */
  public void printRegistryState() {
    this.printRegistryState(false);
  }

  /**
   * Print the state of the Plugin Registry to standard out.
   *
   * @param useCanonicalNames When true, prints the full class name of the Plugin Interface.
   */
  public void printRegistryState(boolean useCanonicalNames) {
    System.out.print(this.toRegistryState(useCanonicalNames));
  }

  //---------- Directory Monitor Methods ----------//


  /**
   * Monitors a directory for JAR and Package Directories, and scans them when they are detected.
   *
   * @param directoryPath Directory targeted for montoring.
   * @throws IOException Exception thrown when monitoring cannot be performed or fails.
   */
  public final void startDirectoryMonitor(String directoryPath) throws IOException {

    // Validate the input.
    if (directoryPath == null || directoryPath.isBlank()) {
      throw new IllegalArgumentException("Directory path is null or empty.");
    }
    Path dir = Paths.get(directoryPath);
    File file = dir.toFile();
    if (!file.exists()) {
      throw new IllegalArgumentException(String.format(
          "Directory does not exist [%s].", dir.getFileName()));
    } else if (!file.isDirectory()) {
      throw new IllegalArgumentException(String.format(
          "Path specified must be a directory (not a file) [%s].", dir.getFileName()));
    }

    // Check whether the Directory Monitor is already running.
    if (directoryMonitor.isRunning()) {
      throw new IllegalStateException("The Plugin Library Directory Monitor is already running.");
    }

    // Start the directory monitor.
    directoryMonitor.init(file);
    directoryMonitor.start();
    directoryMonitor.waitUntilThreadInitializes();
  }

  /**
   * Stops the directory monitor.
   */
  public final void stopDirectoryMonitor() {
    directoryMonitor.stop();
    directoryMonitor.waitUntilThreadStops();
  }

  /**
   * Returns true if the directory monitor is currently running, otherwise false.
   *
   * @return True if directory monitor is running, otherwise false.
   */
  public final boolean isDirectoryMonitorRunning() {
    return directoryMonitor.isRunning();
  }

  //---------- Equals / HashCode / ToString / ToJson Methods ----------//


  /**
   * Equals.
   *
   * @return Equals.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PluginRegistry that = (PluginRegistry) o;
    return pluginRegistry.equals(that.pluginRegistry);
  }

  /**
   * Hash code.
   *
   * @return Hash code.
   */
  @Override
  public int hashCode() {
    return Objects.hash(pluginRegistry);
  }

  /**
   * Returns a JSON representation of the state of the Plugin Registry.
   *
   * @return Representation of the state of the Plugin Registry.
   */
  @Override
  public String toString() {
    return this.toJson(true, false);
  }

  /**
   * Returns a JSON representation of the state of the Plugin Registry.
   *
   * @param useCanonicalNames Use canonical names to describe Plugin Interfaces.
   * @param prettyPrint Format for pretty printing.
   * @return Representation of the state of the Plugin Registry.
   */
  public String toJson(boolean useCanonicalNames, boolean prettyPrint) {
    if (pluginRegistry.isEmpty()) {
      return "[]";
    }

    StringBuilder sb = new StringBuilder();
    sb.append((prettyPrint) ? "[\n" : "[");

    Map<String, ArrayList<String>> contents = getSortedRegistryContents(useCanonicalNames);
    boolean isFirstPluginInterfaceIteration = true;
    for (String pluginInterface : contents.keySet()) {
      if (!isFirstPluginInterfaceIteration) {
        sb.append((prettyPrint) ? ",\n  " : ",");
      }

      if (isFirstPluginInterfaceIteration && prettyPrint) {
        sb.append("  ");
      }
      sb.append("{ \"");
      sb.append(pluginInterface);
      sb.append("\": [");

      boolean isFirstPluginIteration = true;
      for (String plugin : contents.get(pluginInterface)) {
        if (!isFirstPluginIteration) {
          sb.append(",");
        }
        sb.append(" \"").append(plugin).append("\"");
        isFirstPluginIteration = false;
      }
      sb.append((contents.get(pluginInterface).size() > 0) ? " ]}" : "]}");

      isFirstPluginInterfaceIteration = false;
    }

    sb.append((prettyPrint) ? "\n]" : "]");

    return sb.toString();
  }

  //---------- Create Encrypted Package ----------//


  /**
   * Encrypts the specified JAR file, and writes the encrypted package to the current directory.
   *
   * @param pathToJar Path to target JAR file to be encrypted.
   * @param publicEncryptionKey Public key provided by the receiver, for use in encrypting the JAR
   * file.
   */
  public void createEncryptedPackage(
      String pathToJar,
      String publicEncryptionKey) throws IOException {
    Objects.requireNonNull(pathToJar);
    Objects.requireNonNull(publicEncryptionKey);

    CryptographyManager cryptographyManager = new CryptographyManager();
    cryptographyManager.createPackage(pathToJar, publicEncryptionKey);
  }

  /**
   * Encrypts the specified JAR file, and writes the encrypted package to the specified output
   * directory.
   *
   * @param pathToJar Path to target JAR file to be encrypted.
   * @param publicEncryptionKey Public key provided by the receiver, for use in encrypting the JAR
   * file.
   * @param outputDirectory Path to the directory where output is to be written.
   */
  public void createEncryptedPackage(
      String pathToJar,
      String publicEncryptionKey,
      String outputDirectory) throws IOException {
    Objects.requireNonNull(pathToJar);
    Objects.requireNonNull(publicEncryptionKey);
    Objects.requireNonNull(outputDirectory);

    CryptographyManager cryptographyManager = new CryptographyManager();
    cryptographyManager.createPackage(pathToJar, publicEncryptionKey, outputDirectory);
  }

  //---------- Private Methods ----------//


  /**
   * Returns a sorted collection of the contents of the Plugin Registry (i.e. describes the current
   * state of the Plugin Registry).
   *
   * @param useCanonicalNames Use canonical names to describe Plugin Interfaces.
   * @return Sorted collection of the contents of the Plugin Registry.
   */
  private LinkedHashMap<String, ArrayList<String>> getSortedRegistryContents(
      boolean useCanonicalNames) {
    LinkedHashMap<String, ArrayList<String>> results = new LinkedHashMap<>();

    for (Class<? extends Pluggable> pluginInterface : pluginRegistry.navigableKeySet()) {
      ArrayList<String> pluginStrings = new ArrayList<>();
      for (PluginEntry pluginEntry : pluginRegistry.get(pluginInterface)) {
        pluginStrings.add(
            String.format("%1$s %2$s",
                pluginEntry.getPluginName(),
                (pluginEntry.getPluginVersion().toVersionNumber().equalsIgnoreCase("0.0.0")) ?
                    "" :
                    "(version " + pluginEntry.getPluginVersion().toString() + ")")
                .trim());
      }

      results.put(
          (useCanonicalNames) ?
              pluginInterface.getCanonicalName() :
              pluginInterface.getSimpleName(),
          pluginStrings);
    }

    return results;
  }

  /**
   * Returns true if a Plugin with the specified name and version number has been registered under
   * the specified Plugin Interface.
   *
   * @param pluginName Name of the target Plugin.
   * @param registeredPluginInterface Registered Plugin Interface class.
   */
  private boolean isRegisteredPlugin(
      String pluginName,
      PluginVersion pluginVersion,
      Class<? extends Pluggable> registeredPluginInterface) {

    // Validate input.
    validatePluginNameArgument(pluginName);
    Objects.requireNonNull(pluginVersion);
    validateRegisteredPluginInterfaceArgument(registeredPluginInterface);

    // Loop through each Plugin registered under the specified Plugin Interface.
    for (PluginEntry pluginEntry : pluginRegistry.get(registeredPluginInterface)) {
      // Check if this plugin matches the specified name and version.
      if (pluginEntry.getPluginName().equals(pluginName) &&
          pluginEntry.getPluginVersion().equals(pluginVersion)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns all .class files found in the specified package.
   *
   * @param packageName Target package.
   * @return Set of .class files.
   * @throws IOException Throws on I/O error reading the specified package.
   */
  private Set<File> retrieveClassFilesInPackage(String packageName) throws IOException {
    Set<File> classFiles = new HashSet<>();

    // Get the class loader.
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    // Extract the resources from this package.
    Enumeration<URL> resources = classLoader.getResources(packageName.replace('.', '/'));

    // Loop through each resource directory.
    Queue<File> nestedPackageQueue = new ArrayDeque<>();

    while (resources.hasMoreElements() || !nestedPackageQueue.isEmpty()) {
      File path = (!nestedPackageQueue.isEmpty()) ?
          nestedPackageQueue.remove() :
          new File(resources.nextElement().getFile());

      // Filter out non-existing and non-directory resources (if any exist, which seems unlikely).
      if (path.exists()) {
        if (path.isDirectory()) {
          // Extract all files in this directory.
          File[] dirFiles = path.listFiles();
          if (dirFiles != null) {
            for (File resourceFile : dirFiles) {
              // Filter for .class files and check that they are assignable to the target.
              if (resourceFile.isFile()) {
                if (resourceFile.getName().endsWith(".class")) {
                  // DEBUG.
                  debug("    - Class File: [" + resourceFile.getName() + "]");

                  // Add the class file.
                  classFiles.add(resourceFile);
                } else {
                  // DEBUG.
                  debug("    - Misc Resource File: [" + resourceFile.getName() + "]");
                }
              } else {
                // DEBUG.
                debug("    - Nested Package Directory: [" + resourceFile.getName() + "]");

                // Add new package directory to the nested package queue.
                nestedPackageQueue.add(resourceFile);
              }
            }
          }
        } else {
          // DEBUG.
          debug("  - ERROR: NOT A DIRECTORY: [" + path.getName() + "]");
        }
      } else {
        // DEBUG.
        debug("  - ERROR: DOES NOT EXIST: [" + path.getName() + "]");
      }
    }

    return classFiles;
  }

  /**
   * Returns all .class files found in the specified directory.
   *
   * @param path Directory path.
   * @return List of .class files found.
   */
  Set<File> retrieveClassFilesFromDirectoryTree(File path, List<ScanLog> scanLogs) {

    // Validate input.
    if (!path.exists()) {
      throw new IllegalArgumentException(
          String.format("Path does not exist [%s].", path.getPath()));
    } else if (!path.isDirectory()) {
      throw new IllegalArgumentException(
          String.format("Target is not a directory [%s]", path.getPath()));
    }

    File[] files;
    files = path.listFiles();
    if (files == null) {
      throw new IllegalArgumentException(String.format(
          "Files could not be read from the specified directory [%s].", path.getPath()));
    }

    Set<File> classFiles = new HashSet<>();

    // Loop through each file in the directory tree.
    for (File file : files) {
      if (file.exists()) {
        if (file.isDirectory()) {

          // Add to scan logs.
          scanLogs.add(new ScanLog(
              false, false, "DIRECTORY", file.getPath(), "", ""));

          // Recursively scan the directory for files.
          classFiles.addAll(retrieveClassFilesFromDirectoryTree(file, scanLogs)); // Recursion.
        } else if (file.isFile() && file.getName().endsWith(".class")) {

          // Add the class file.
          classFiles.add(file);
        } else {

          // Add to scan logs.
          scanLogs.add(new ScanLog(
              false, true, "RESOURCE", file.getPath(), "",
              "Not a class file."));
        }
      } else {
        // DEBUG.
        debug("  - ERROR: DOES NOT EXIST: [" + file.getName() + "]");
      }
    }

    return classFiles;
  }

  /**
   * Throws an exception if no Plugin Interfaces are registered.
   */
  private void validatePluginInterfacesAreRegistered() {
    if (pluginRegistry.size() <= 0) {
      throw new IllegalStateException(
          "No Plugin Interfaces are currently registered in the Plugin Registry.");
    }
  }

  /**
   * Throws an exception if the class provided is not a valid Plugin Interface.
   *
   * @param pluginInterface Registered Plugin Interface class.
   */
  private void validatePluginInterfaceArgument(Class<? extends Pluggable> pluginInterface) {

    // Plugin Interfaces must be declared as a Java interface.
    if (!pluginInterface.isInterface()) {
      throw new IllegalArgumentException(String.format(
          "Class [%s] is not a valid Plugin Interface, because it is not declared as a Java interface.",
          pluginInterface.getSimpleName()));
    }

    // Plugin Interfaces must extend the Pluggable interface.
    if (!Pluggable.class.isAssignableFrom(pluginInterface)) {
      throw new IllegalArgumentException(String.format(
          "Class [%s] is not a valid Plugin Interface, because it does not extend Pluggable.",
          pluginInterface.getSimpleName()));
    }
  }

  /**
   * Throws an exception if the class provided is not a registered Plugin Interface.
   *
   * @param registeredPluginInterfaces Set of Registered Plugin Interface classes.
   */
  private void validateRegisteredPluginInterfaceArguments(
      Set<Class<? extends Pluggable>> registeredPluginInterfaces) {
    Objects.requireNonNull(registeredPluginInterfaces);
    for (Class<? extends Pluggable> registeredPluginInterface : registeredPluginInterfaces) {
      validateRegisteredPluginInterfaceArgument(registeredPluginInterface);
    }
  }

  /**
   * Throws an exception if the class provided is not a registered Plugin Interface.
   *
   * @param registeredPluginInterface Registered Plugin Interface class.
   */
  private void validateRegisteredPluginInterfaceArgument(
      Class<? extends Pluggable> registeredPluginInterface) {

    validatePluginInterfaceArgument(registeredPluginInterface);

    if (!pluginRegistry.containsKey(registeredPluginInterface)) {
      throw new IllegalArgumentException(String.format(
          "Plugin Interface [%s] is not registered.", registeredPluginInterface.getSimpleName()));
    }
  }

  /**
   * Checks that the Plugin is:
   * <ul>
   * <li> 1) not a Java interface, and </li>
   * <li> 2) implements the Pluggable interface.</li>
   * </ul>
   *
   * @param clazz Plugin class.
   */
  private void validatePluginArgument(Class<?> clazz) {

    // Plugin class must be a Java 'class', not an interface.
    if (clazz.isInterface()) {
      throw new IllegalArgumentException(String.format(
          "Class [%s] is not a valid Plugin, because it is implemented as a Java interface.",
          clazz.getSimpleName()));
    }

    // Plugin must implement the Pluggable interface.
    if (!Pluggable.class.isAssignableFrom(clazz)) {
      throw new IllegalArgumentException(String.format(
          "Class [%s] is not a valid Plugin, because it does not implement the Pluggable interface.",
          clazz.getSimpleName()));
    }
  }

  /**
   * Checks that the Plugin is:
   * <ul>
   * <li> 1) not a Java interface, </li>
   * <li> 2) implements the Pluggable interface, and </li>
   * <li> 3) implements the specified Java Interface.</li>
   * </ul>
   *
   * @param clazz Plugin class.
   * @param registeredPluginInterface Registered Plugin Interface class.
   */
  private void validatePluginArgument(
      Class<?> clazz,
      Class<? extends Pluggable> registeredPluginInterface) {

    validatePluginArgument(clazz);

    // Plugin must implement the specified Plugin Interface.
    if (!registeredPluginInterface.isAssignableFrom(clazz)) {
      throw new IllegalArgumentException(String.format(
          "Plugin [%s] does not implement the specified Plugin Interface [%s].",
          clazz.getSimpleName(),
          registeredPluginInterface.getSimpleName()));
    }
  }

  /**
   * Validates that the string contains a valid plugin name.
   *
   * @param pluginName Plugin name.
   */
  private void validatePluginNameArgument(String pluginName) {
    if (pluginName == null || pluginName.isBlank()) {
      throw new IllegalArgumentException("Plugin names many not be null or blank.");
    }
  }

  /**
   * Validates that the package names are valid.
   *
   * @param targetPackages List of package names.
   */
  private void validateJavaPackageNames(Set<String> targetPackages) {
    if (targetPackages == null) {
      return;
    } else if (targetPackages.size() <= 0) {
      throw new IllegalArgumentException(
          "The targetPackages argument contains an empty set. Argument must either be NULL or contain one or more values). ");
    }

    for (String pkg : targetPackages) {
      Objects.requireNonNull(pkg, "Null package name detected.");
      if (!pkg.matches("^[a-z][a-z0-9_]*(\\.[a-z0-9_]+)+[0-9a-z_]$")) {
        throw new IllegalArgumentException(String.format("Invalid package name [%s].", pkg));
      }
    }
  }

  /**
   * Returns true if the class specified is a valid plugin.
   *
   * @param clazz Class being tested.
   * @return True if class is a valid plugin, false otherwise.
   */
  private boolean isValidPlugin(
      Class<?> clazz) {
    try {
      // Check that plugin:
      //   1) is not a Java interface, and
      //   2) implements the Pluggable interface.
      validatePluginArgument(clazz);

      return true;

    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Returns true if the class specified is a valid plugin, and implements the given registered
   * Plugin Interface.
   *
   * @param clazz Class being tested.
   * @return True if class is a valid plugin, false otherwise.
   */
  private boolean isValidPlugin(
      Class<?> clazz,
      Class<? extends Pluggable> registeredPluginInterface) {
    try {
      // Check that plugin:
      //   1) is not a Java interface,
      //   2) implements the Pluggable interface, and
      //   3) implements the specified Java interface.
      validatePluginArgument(clazz, registeredPluginInterface);

      return true;

    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Returns the Plugin Entry associated with the given criteria.
   *
   * @param pluginName Plugin name.
   * @param pluginVersion Plugin version.
   * @param registeredPluginInterface Registered Plugin Interface class.
   * @param <T> Registered Plugin Interface type.
   * @return Plugin Entry.
   */
  private <T extends Pluggable> PluginEntry getPluginEntry(
      String pluginName,
      PluginVersion pluginVersion,
      Class<T> registeredPluginInterface) {

    // Retrieve the specified Plugin Entry.
    List<PluginEntry> pluginEntries = pluginRegistry
        .get(registeredPluginInterface)
        .stream()
        .filter(x -> x.getPluginName().equals(pluginName) &&
            x.getPluginVersion().equals(pluginVersion))
        .collect(Collectors.toList());

    if (pluginEntries.size() <= 0) {
      throw new IllegalArgumentException(String.format(
          "Plugin does not exist in the Plugin Registry [%s, (version %s)].",
          pluginName,
          pluginVersion.toVersionNumber()));
    } else if (pluginEntries.size() > 1) {
      throw new IllegalStateException(String.format(
          "Multiple plugins of the given name and version found (this should never occur) [%s, (version %s)].",
          pluginName,
          pluginVersion.toVersionNumber()));
    }

    return pluginEntries.get(0);
  }

  /**
   * Returns the path to the JAR file containing the class that called this method.
   *
   * @return Path to JAR file.
   */
  private File getCallingClassRootPath() {
    try {
      // Get the caller class.
      for (StackTraceElement stElement : Thread.currentThread().getStackTrace()) {
        if (!stElement.getClassName().equals("java.lang.Thread") &&
            !stElement.getClassName().equals(PluginRegistry.class.getCanonicalName())) {

          // Extract the class name, and determine its root (either a JAR file, or a Package Root directory).
          Class<?> callerClass = Class.forName(stElement.getClassName());
          File rootPath = new File(
              callerClass.getProtectionDomain().getCodeSource().getLocation().toURI());

          if (rootPath.isDirectory()) {
            // Return the path to the Package Root directory.
            String rootPackage = callerClass.getPackageName();
            rootPackage = rootPackage.substring(0, rootPackage.indexOf('.'));
            return new File(rootPath.getPath() + "/" + rootPackage);
          } else {
            // Return the path to the JAR file.
            return rootPath;
          }
        }
      }
    } catch (ClassNotFoundException | URISyntaxException e) {
      throw new PluginLibraryException("Local JAR file path could not be determined.", e);
    }

    throw new PluginLibraryException(
        "Local JAR file path could not be determined (stack loop completed).");
  }

  /**
   * For debugging only.
   *
   * @param message Debug message.
   */
  private void debug(String message) {
    System.out.println(message);
  }
}
