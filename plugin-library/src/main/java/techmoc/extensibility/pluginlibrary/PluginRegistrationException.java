package techmoc.extensibility.pluginlibrary;


/**
 * Thrown to indicate that the registration of the specified {@link Pluggable} in the {@link
 * PluginRegistry} has failed.
 */
public class PluginRegistrationException extends RuntimeException {


  // Format string used to construct the exception message
  private static final String EXCEPTION_MSG_FORMAT_STRING = "Failed to register %s class %s";


  /**
   * Instantiates a new {@link PluginRegistrationException}.
   *
   * @param className {@link String} name of the {@link Pluggable} {@link Class} that could not be
   * registered.
   */
  public PluginRegistrationException(String className) {

    super(
        String.format(
            PluginRegistrationException.EXCEPTION_MSG_FORMAT_STRING,
            Pluggable.class.getSimpleName(),
            className
        )
    );
  }


  /**
   * Instantiates a new {@link PluginRegistrationException}.
   *
   * @param className {@link String} name of the {@link Pluggable} {@link Class} that could not be
   * registered.
   * @param cause {@link Throwable} that caused this {@link PluginRegistrationException} to be
   * instantiated.
   */
  public PluginRegistrationException(String className, Throwable cause) {

    super(
        String.format(
            PluginRegistrationException.EXCEPTION_MSG_FORMAT_STRING,
            Pluggable.class.getSimpleName(),
            className
        ),
        cause
    );
  }
}
