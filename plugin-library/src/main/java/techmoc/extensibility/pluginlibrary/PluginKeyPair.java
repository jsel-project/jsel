package techmoc.extensibility.pluginlibrary;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

public class PluginKeyPair {

  /* Key Generation Parameters */
  private final String SHA256_RSA = "SHA256withRSA";
  private final String keyPairAlgorithm = "RSA";
  private final int keySize = 2048;

  private byte[] publicKey;
  private byte[] privateKey;
  public static final String PRIVATE_KEY = "Private";
  public static final String PUBLIC_KEY = "Public";
  public static final String KEY_PAIR = "Both";

  /**
   * Creates a <code>PluginKeyPair</code> and automatically generates the public and private keys. The keys are RSA-2048 <code>PublicKey</code> and <code>PrivateKey</code> types.
   */
  public PluginKeyPair() {

    KeyPairGenerator keyPairGenerator = null;
    try {
       keyPairGenerator = KeyPairGenerator.getInstance(keyPairAlgorithm);
    }catch (NoSuchAlgorithmException suppressed){
      /* RSA is a required algorithm per Java Crypto Arch */
    }

    keyPairGenerator.initialize(keySize);
    KeyPair keys = keyPairGenerator.generateKeyPair();

    publicKey = keys.getPublic().getEncoded();
    privateKey = keys.getPrivate().getEncoded();

  }

  public static PluginKeyPair fromPublicKey(byte[] hex){
    return new PluginKeyPair(hex, null, PUBLIC_KEY);
  }

  public static PluginKeyPair fromPrivateKey(byte[] hex){
    return new PluginKeyPair(hex, null, PRIVATE_KEY);
  }

  public static PluginKeyPair fromKeys(byte[] privateKey, byte[] publicKey) {
    return new PluginKeyPair(privateKey, publicKey, KEY_PAIR);
  }

  /**
   * Creates a <code>PluginKeyPair</code>. The input byte arrays must be correctly encoded for <code>PublicKey</code> (X509). The <code>PrivateKey</code> will be set to an empty array
   * and should not be accessed by user logic.
   * @param key1 X509-Encoded PublicKey byte array
   */
  public PluginKeyPair(byte[] key1, byte[] key2 , String kind){
    switch (kind) {
      case PUBLIC_KEY:
        this.publicKey = key1;
        this.privateKey = new byte[]{};
        break;
      case PRIVATE_KEY:
        this.publicKey = new byte[]{};
        this.privateKey = key1;
        break;
      case KEY_PAIR:
        this.privateKey = key1;
        this.publicKey = key2;
        break;
      default:
        throw new IllegalArgumentException(String.format("%s is an invalid key type.", kind));
    }

  }



  /**
   * Returns the hex string representing an X509-encoded RSA Public Key.
   * @return colon-delimited string representing the bytes of a X509-encoded RSA public key
   */
  public String getPublicKey(){
    return hexify(publicKey);
  }


  /**
   * Returns the hex string representing a PKCS8-encoded RSA Private Key.
   * @return colon-delimited string representing the bytes of a PKCS8-encoded RSA private key
   */
  public String getPrivateKey(){
    return hexify(privateKey);
  }

  /**
   * <i>Hexification</i> is the process of converting bytes of data into a colon-delimited string representing each 8-bit byte
   * as two hexadecimal values. <p>This process knows nothing of byte ordering (<i>endian-ness</i>), it will create the string so that
   * index 0 of the byte array is the first byte in the returned string.</p>
   * @param data byte array containing data to be <i>hexified</i>
   * @return colon-delimited string representing the bytes provided
   */
  public static String hexify(byte[] data){

    StringBuilder sb = new StringBuilder();
    for (byte b : data) {
      sb.append(String.format("%02X:", b));
    }

    /* Check for and strip a trailing colon */
    int lastColon = sb.lastIndexOf(":");
    if(lastColon == sb.toString().length()-1 && lastColon > -1) {
      sb.deleteCharAt(lastColon);
    }
    return sb.toString();
  }


  /**
   * <i>Dehexification</i> is the process of converting a colon-delimited string representing arbitrary data into
   * a <code>byte</code> array. Each string element must be two bytes wide. <p>This process knows nothing of byte ordering (<i>endian-ness</i>),
   * it will create the byte array so that index 0 contains the value represented by the first element in the string.</p>
   *
   * <p>Ref: https://www.baeldung.com/java-byte-arrays-hex-strings</p>
   *
   * @param string colon-delimited hexadecimal string to be <i>dehexified</i>
   * @return byte array containing the values represented by the string elements
   */
  public static byte[] dehexify(String string) throws IllegalArgumentException{

    String[] elements = string.split(":");
    byte[] hexData = new byte[elements.length];

    for (int idx = 0; idx < elements.length; idx++) {

      String element = elements[idx];
      if(element.length() != 2){
        throw new IllegalArgumentException("Elements must be 2 characters wide");
      }

      int sixteensDigit = toDigit(element.charAt(0));
      int onesDigit = toDigit(element.charAt(1));
      hexData[idx] = (byte) ((sixteensDigit << 4) + onesDigit);

    }

    return hexData;

  }

  private static int toDigit(char hexChar){
    int digit = Character.digit(hexChar, 16);
    if(digit == -1){
      throw new IllegalArgumentException("Could not decode character: " + hexChar);
    }
    return digit;
  }

  public PrivateKey getEncodedPrivateKey(){
    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKey);
    PrivateKey key = null;

    try{
      KeyFactory keyFactory = KeyFactory.getInstance(keyPairAlgorithm);
      key = keyFactory.generatePrivate(keySpec);
    } catch (NoSuchAlgorithmException | InvalidKeySpecException ignored){}

    /* TODO: Look at how to handle invalid keys (byte array does not decode) */

    return key;
  }

  public PublicKey getEncodedPublicKey(){
    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKey);
    PublicKey key = null;

    try {
      KeyFactory keyFactory = KeyFactory.getInstance(keyPairAlgorithm);
      key = keyFactory.generatePublic(keySpec);
    } catch (NoSuchAlgorithmException | InvalidKeySpecException ignored){}

    /* TODO: Look at how to handle invalid keys (byte array does not decode) */

    return key;
  }

  public String getAlgorithm(){
    return SHA256_RSA;
  }

  @Override
  public String toString() {
    return String.format("{ \"class\":\"PluginKeyPair\", \"SHA256_RSA\":\"%s\", \"keyPairAlgorithm\":\"%s\", \"keySize\":%d, \"publicKey\":%s, \"privateKey\":%s}", SHA256_RSA, keyPairAlgorithm, keySize, hexify(publicKey), hexify(privateKey));
  }
}
