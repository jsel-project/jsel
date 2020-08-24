package techmoc.extensibility.pluginlibrary;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;

import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static org.junit.jupiter.api.Assertions.*;

public class SignatureManagerTests {

  @TempDir Path tempDir;
  byte[] messageText;
  SignatureManager manager;
  Path messageFile;

  @BeforeEach
  public void setUp() throws IOException{
    manager = new SignatureManager();
    Files.createDirectory(tempDir.resolve("test/"));
    messageFile = tempDir.resolve("Messages.txt");
    messageText = "Once upon a midnight dreary, while I pondered weak and weary over many a quaint and curious".getBytes();
    Files.write(messageFile, messageText, CREATE_NEW);
  }

  @Test
  public void symmetricKeyPairsGenerated() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

    PublicKey publicKey;
    PrivateKey privateKey;
    publicKey = manager.getPluginKeyPair().getEncodedPublicKey();
    privateKey = manager.getPluginKeyPair().getEncodedPrivateKey();

    /* Checking that the keys are symmetric, so do not use SM's sign/verify */
    Signature signer = Signature.getInstance(manager.getPluginKeyPair().getAlgorithm());
    signer.initSign(privateKey);
    byte[] signature = signer.sign();
    signer.update(messageText);

    signer.initVerify(publicKey);
    boolean matched = signer.verify(signature);

    assertNotNull(publicKey);
    assertNotNull(privateKey);
    assertTrue(matched);
  }

  @Test
  public void signatureIsSymmetric() {

    Path messageSignature = tempDir.resolve("Messages.txt.sig");

    manager.sign(messageFile);
    boolean verified = manager.verify(messageFile, messageSignature);

    assertTrue(verified);

  }


  @Test
  public void failedVerifyThrowsException() {
    /* Setup */
    assertThrows(PluginRegistrationException.class, () -> {
      /* Trigger Exception */
      byte[] tamperText = "And like a ghastly, rapid river through the pale door a hideous throng rush out forever".getBytes();
      Path messageSignature = tempDir.resolve("Messages.txt.sig");

      manager.sign(messageFile);
      Files.write(messageFile, tamperText);
      boolean verified = manager.verify(messageFile, messageSignature);

      assertFalse(verified);
    });
  }
}