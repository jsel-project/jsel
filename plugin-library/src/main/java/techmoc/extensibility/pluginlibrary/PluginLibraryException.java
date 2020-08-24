package techmoc.extensibility.pluginlibrary;


/**
 * Thrown to indicate a general exception within the {@link PluginRegistry}.
 */
public class PluginLibraryException extends RuntimeException {

  /**
   * Instantiates a new {@link PluginLibraryException}.
   *
   * @param errorMessage {@link String} error message.
   */
  public PluginLibraryException(String errorMessage) {
    super(errorMessage);
  }

  /**
   * Instantiates a new {@link PluginLibraryException}.
   *
   * @param errorMessage {@link String} error message.
   * @param cause {@link Throwable} that caused this exception.
   */
  public PluginLibraryException(String errorMessage, Throwable cause) {
    super(errorMessage, cause);
  }
}
