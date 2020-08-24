package techmoc.extensibility.pluginlibrary;

public class ScanLog {

  final private boolean registeredSuccessfully;
  final private boolean isFile;
  final private String type;
  final private String path;
  final private String fullyQualifiedName;
  final private String reason;

  public ScanLog(
      boolean registeredSuccessfully, boolean isFile,
      String type, String path, String fullyQualifiedName, String reason) {
    this.registeredSuccessfully = registeredSuccessfully;
    this.isFile = isFile;
    this.type = type;
    this.path = path;
    this.fullyQualifiedName = fullyQualifiedName;
    this.reason = reason;
  }

  public boolean registeredSuccessfully() {
    return registeredSuccessfully;
  }

  public boolean isFile() {
    return isFile;
  }

  public String getType() {
    return type;
  }

  public String getPath() {
    return path;
  }

  public String getFullyQualifiedName() {
    return fullyQualifiedName;
  }

  public String getReason() {
    return reason;
  }

  @Override
  public String toString() {
    return "ScanLog{" +
        "registeredSuccessfully=" + registeredSuccessfully +
        ", isFile=" + isFile +
        ", type='" + type + '\'' +
        ", path='" + path + '\'' +
        ", fullyQualifiedName='" + fullyQualifiedName + '\'' +
        ", reason='" + reason + '\'' +
        '}';
  }
}
