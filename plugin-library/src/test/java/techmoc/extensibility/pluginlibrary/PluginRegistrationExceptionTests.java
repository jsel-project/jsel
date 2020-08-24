package techmoc.extensibility.pluginlibrary;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


class PluginRegistrationExceptionTests {


  @Test
  void testConstructorWithPluginClassName() {

    final String notFoundPluggableClassName = "notFoundPluggableClassName";

    PluginRegistrationException pluginRegistrationException = new PluginRegistrationException(
        notFoundPluggableClassName);

    Assertions.assertEquals(
        String.format(
            "Failed to register %s class %s",
            Pluggable.class.getSimpleName(),
            notFoundPluggableClassName
        ),
        pluginRegistrationException.getMessage()
    );
  }


  @Test
  void testConstructorWithPluginClassNameAndCause() {

    final String notFoundPluggableClassName = "notFoundPluggableClassName";

    Throwable mockThrowable = Mockito.mock(Throwable.class);

    PluginRegistrationException pluginRegistrationException = new PluginRegistrationException(
        notFoundPluggableClassName, mockThrowable);

    Assertions.assertAll(
        () -> Assertions.assertEquals(
            String.format(
                "Failed to register %s class %s",
                Pluggable.class.getSimpleName(),
                notFoundPluggableClassName
            ),
            pluginRegistrationException.getMessage()
        ),
        () -> Assertions.assertEquals(mockThrowable, pluginRegistrationException.getCause())
    );
  }
}
