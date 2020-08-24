package techmoc.extensibility.pluginlibrary;

import java.util.Objects;

public final class PluginVersion implements Comparable<PluginVersion> {

  public static final PluginVersion DEFAULT = new PluginVersion(0, 0, 0);

  private final int majorVersion;
  private final int minorVersion;
  private final int buildNumber;
  private final String revisionTag;

  public PluginVersion() {
    this(0, 0, 0, null);
  }

  public PluginVersion(int majorVersion) {
    this(majorVersion, 0, 0, null);
  }

  public PluginVersion(int majorVersion, int minorVersion) {
    this(majorVersion, minorVersion, 0, null);
  }

  public PluginVersion(int majorVersion, int minorVersion, String revisionTag) {
    this(majorVersion, minorVersion, 0, revisionTag);
  }

  public PluginVersion(int majorVersion, int minorVersion, int buildNumber) {
    this(majorVersion, minorVersion, buildNumber, null);
  }

  public PluginVersion(
      int majorVersion, int minorVersion, int buildNumber, String revisionTag) {

    // Validate inputs.
    Objects.requireNonNull(majorVersion, "Major Version cannot be null.");
    Objects.requireNonNull(minorVersion, "Minor Version cannot be null.");
    Objects.requireNonNull(buildNumber, "Build Number cannot be null.");
    if (majorVersion < 0) {
      throw new IllegalArgumentException("Major Version cannot be a negative number.");
    }
    if (minorVersion < 0) {
      throw new IllegalArgumentException("Minor Version cannot be a negative number.");
    }
    if (buildNumber < 0) {
      throw new IllegalArgumentException("Build Number cannot be a negative number.");
    }

    this.majorVersion = majorVersion;
    this.minorVersion = minorVersion;
    this.buildNumber = buildNumber;
    this.revisionTag = revisionTag;
  }

  public final int getMajorVersion() {
    return majorVersion;
  }

  public final int getMinorVersion() {
    return minorVersion;
  }

  public final int getBuildNumber() {
    return buildNumber;
  }

  public final String getRevisionTag() {
    return revisionTag;
  }

  /**
   * Returns a string representation of the version number (without the revision tag).
   *
   * @return Version number.
   */
  public final String toVersionNumber() {
    return majorVersion + "." + minorVersion + "." + buildNumber;
  }

  /**
   * Returns the
   */
  @Override
  public final String toString() {
    return (revisionTag == null) ? toVersionNumber() : toVersionNumber() + "-" + revisionTag;
  }

  @Override
  public final int compareTo(PluginVersion that) {
    // Compare the Major Version numbers.
    if (this.majorVersion < that.majorVersion) {
      return -1;
    } else if (this.majorVersion > that.majorVersion) {
      return 1;
    }

    // Compare the Minor Version numbers, if they are not null.
    if (this.minorVersion < that.minorVersion) {
      return -1;
    } else if (this.minorVersion > that.minorVersion) {
      return 1;
    }

    // Compare the Build Numbers, if they are not null.
    if (this.buildNumber < that.buildNumber) {
      return -1;
    } else if (this.buildNumber > that.buildNumber) {
      return 1;
    }

    return 0;
  }

  @Override
  public final boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PluginVersion that = (PluginVersion) o;
    return majorVersion == that.majorVersion &&
        minorVersion == that.minorVersion &&
        buildNumber == that.buildNumber;
  }

  @Override
  public final int hashCode() {
    return Objects.hash(majorVersion, minorVersion, buildNumber);
  }
}
