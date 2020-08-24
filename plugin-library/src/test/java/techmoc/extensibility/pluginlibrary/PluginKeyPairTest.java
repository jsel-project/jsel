package techmoc.extensibility.pluginlibrary;

import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class PluginKeyPairTest {


  @Test
  public void createsKeyByteArrays(){

    PluginKeyPair keyPair = new PluginKeyPair();

    assertNotEquals(0, keyPair.getPrivateKey().length());
    assertNotEquals(0, keyPair.getPublicKey().length());

  }

  @Test
  public void createsPublicKeyOnly() throws NoSuchAlgorithmException {

    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
    keyPairGenerator.initialize(2048);
    KeyPair keys = keyPairGenerator.generateKeyPair();

    PluginKeyPair keyPair = PluginKeyPair.fromPublicKey(keys.getPublic().getEncoded());

    assertEquals(0, keyPair.getPrivateKey().length());
    assertNotEquals(0, keyPair.getPublicKey().length());
  }

  @Test
  public void hexifyDoesNotEndWithColon(){

    byte[] data = new byte[]{0x11, 0x22, 0x33, 0x44, 0x55};
    String expected = "11:22:33:44:55";

    String actual = PluginKeyPair.hexify(data);

    assertEquals(expected, actual);
  }

  @Test
  public void encodesPublicKeyBytes(){

      PluginKeyPair keyPair = new PluginKeyPair();

      assertNotNull(keyPair.getEncodedPublicKey());
  }

  @Test
  public void encodesPrivateKeyBytes(){

    PluginKeyPair keyPair = new PluginKeyPair();

    assertNotNull(keyPair.getEncodedPrivateKey());

  }

  @Test
  public void dehexifyArrayIsCorrect(){

    String data = "11:22:33:44:55";
    byte[] expected = new byte[]{0x11, 0x22, 0x33, 0x44, 0x55};

    byte[] actual = PluginKeyPair.dehexify(data);

    assertArrayEquals(expected, actual);
  }

  @Test
  public void dehexifyHandlesShortStrings(){
    String data = "11";
    byte[] expected = new byte[]{0x11};

    byte[] actual = PluginKeyPair.dehexify(data);

    assertArrayEquals(expected, actual);
  }

  @Test
  public void dehexifyThrowsExceptionOnInvalidInput(){

    assertThrows(IllegalArgumentException.class, () -> {
      String badData = "Hi! I'm an arbitrary string!";
      PluginKeyPair.dehexify(badData);
    });
  }

  @Test
  public void dehexifyExpectsTwoByteElements(){
    assertThrows(IllegalArgumentException.class, () -> {
      String badData = "1111111111:22:33";
      Arrays.toString(PluginKeyPair.dehexify(badData));
    });
  }


}