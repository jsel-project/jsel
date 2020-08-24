package techmoc.extensibility.pluginlibrary;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public class ScanResults {

  private List<ScanLog> scanLogs;

  public ScanResults(List<ScanLog> scanLogs) {
    Objects.requireNonNull(scanLogs);
    this.scanLogs = scanLogs;
  }

  public List<ScanLog> getScanLogs() {
    return scanLogs;
  }

  /**
   * Total files scanned (excludes directories).
   *
   * @return Total directories scanned.
   */
  public long getTotalFilesScanned() {
    return scanLogs.stream().filter(ScanLog::isFile).count();
  }

  /**
   * Total directories scanned.
   *
   * @return Total directories scanned.
   */
  public long getTotalDirectoriesScanned() {
    return scanLogs.stream().filter(x -> !x.isFile()).count();
  }

  /**
   * Total successfully registered plugins.
   *
   * @return Total successfully registered plugins.
   */
  public long getTotalPluginsRegistered() {
    return scanLogs.stream().filter(ScanLog::registeredSuccessfully).count();
  }

  /**
   * Total ignored files.
   *
   * @return Total ignored files.
   */
  public long getTotalFilesIgnored() {
    return scanLogs.stream().filter(x -> !x.registeredSuccessfully() && x.isFile()).count();
  }

  /**
   * Returns the logs of scanned directories.
   *
   * @return Logs of scanned directories.
   */
  public List<ScanLog> getDirectoryLogs() {
    return scanLogs.stream().filter(x -> !x.isFile()).collect(Collectors.toList());
  }

  /**
   * Returns the logs of all scanned files.
   *
   * @return Logs of scanned files.
   */
  public List<ScanLog> getFileLogs() {
    return scanLogs.stream().filter(ScanLog::isFile).collect(Collectors.toList());
  }

  /**
   * Returns the logs of all scanned files marked as "RESOURCE" type.
   *
   * @return Logs of files marked as "RESOURCE" type.
   */
  public List<ScanLog> getResourceFileLogs() {
    return scanLogs.stream()
        .filter(x -> x.getType().equals("RESOURCE"))
        .collect(Collectors.toList());
  }

  /**
   * Returns the logs of all scanned files marked as "CLASS" type.
   *
   * @return Logs of files marked as "CLASS" type.
   */
  public List<ScanLog> getClassFileLogs() {
    return scanLogs.stream()
        .filter(x -> x.getType().equals("CLASS"))
        .collect(Collectors.toList());
  }

  /**
   * Returns the logs of all scanned files marked as "PLUGIN" type.
   *
   * @return Logs of files marked as "PLUGIN" type.
   */
  public List<ScanLog> getPluginFileLogs() {
    return scanLogs.stream()
        .filter(x -> x.getType().equals("PLUGIN"))
        .collect(Collectors.toList());
  }

  /**
   * Returns the logs of all scanned files marked as "PLUGIN" type, but that were not successfully
   * registered.
   *
   * @return Logs of plugin files that were not registered.
   */
  public List<ScanLog> getFailedPluginFileLogs() {
    return scanLogs.stream()
        .filter(x -> x.getType().equals("PLUGIN") && !x.registeredSuccessfully())
        .collect(Collectors.toList());
  }

  /**
   * Returns the logs of all scanned files marked as "PLUGIN" type, and were successfully
   * registered.
   *
   * @return Logs of plugin files that were successfully registered.
   */
  public List<ScanLog> getSuccessfulPluginFileLogs() {
    return scanLogs.stream()
        .filter(x -> x.getType().equals("PLUGIN") && x.registeredSuccessfully())
        .collect(Collectors.toList());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("[\n");
    for (int i = 0; i < scanLogs.size(); i++) {
      sb.append((i == 0) ? "  {" : ",\n  {")
          .append(" \"registeredSuccessfully\": ").append(scanLogs.get(i).registeredSuccessfully())
          .append(" \"type:\" ").append(scanLogs.get(i).getType())
          .append(" \"path:\" ").append(scanLogs.get(i).getPath())
          .append(" \"fullyQualifiedName:\" ").append(scanLogs.get(i).getFullyQualifiedName())
          .append(" \"reason:\" ").append(scanLogs.get(i).getReason())
          .append(" }");
    }
    sb.append("\n]");

    return sb.toString();
  }
}
