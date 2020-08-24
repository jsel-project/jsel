package techmoc.extensibility.pluginlibrary;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;

import static org.junit.jupiter.api.Assertions.*;

class CryptographyManagerTest {

  @TempDir Path tempDir;
  CryptographyManager sender;
  CryptographyManager receiver;

  @BeforeEach
  void setUp() throws IOException {
    sender = new CryptographyManager();
    receiver = new CryptographyManager();
    Files.createDirectory(tempDir.resolve("test/"));
  }

  // TODO: Remove this @Disabled annotation (used for Windows).
  @Disabled
  @Test
  public void packageZipAndUnzip() throws IOException, URISyntaxException {

    ClassLoader classLoader = getClass().getClassLoader();
    Path originalJar = Paths.get(classLoader.getResource("test-plugins.jar").toURI());

    sender.createPackage(originalJar.toString(), receiver.getPublicKey());
    String pluginFile = Paths.get(originalJar.getParent().toString(), "test-plugins.plugins").toString();
    String packageFolder = receiver.extractPackage(pluginFile, receiver.getPrivateKey());
    Path unzippedJar = Paths.get(packageFolder);

    /* Assert */
    byte[] originalFile = Files.readAllBytes(originalJar);
    byte[] unzippedFile = Files.readAllBytes(unzippedJar);

    assertNotEquals(originalFile.length, 0);
    assertNotEquals(unzippedFile.length, 0);
    assertArrayEquals(originalFile, unzippedFile);
  }

}

