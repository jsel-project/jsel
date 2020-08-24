package techmoc.extensibility.pluginlibrary;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static java.nio.file.StandardOpenOption.CREATE_NEW;

public class CryptographyManager {

  private final String algorithm = "AES";
  private final String keyWrapAlgorithm = "RSA";
  private final int bufferSize = 1024;
  /* Extensions for various files created or read */
  private final String packageExtension = ".plugins";
  private final String jarExtension = ".jar";
  private final String rsaKeyFileName = "rsa.pub";
  private final String sessionKeyFileName = "session.key";
  private final String cipherTextExtension = ".enc";
  private final String signatureFileExtension = ".sig";
  private Path clearTextFile;
  private Path cipherTextFile;
  private PluginKeyPair pluginKeyPair;
  /* Session key used for AES encryption */
  private SecretKey sessionKey;
  private Cipher cipher;
  private Path cipherKeyFile;

  /**
   * Creates a <code>CryptographyManager</code> and automatically sets its key pair.
   */
  public CryptographyManager() {
    this(new PluginKeyPair());
  }

  public CryptographyManager(PluginKeyPair keyPair) {
    Objects.requireNonNull(keyPair);

    final String transform = String.format("%s/CBC/PKCS5Padding", algorithm);
    try {
      cipher = Cipher.getInstance(transform);
    } catch (NoSuchPaddingException | NoSuchAlgorithmException ignored) {
      /* This case should not be possible unless the project is using a non-standard JVM */
      throw new RuntimeException(String.format("Could not find provider for %s encryption.", transform));
    }
    pluginKeyPair = keyPair;
  }

  /**
   * Ensures unzip will not write outside of intended directory (Zip Slip vulnerability).
   * Ref: https://www.baeldung.com/java-compress-and-uncompress
   *
   * @param destinationDir
   * @param zipEntry
   * @return
   * @throws IOException
   */
  private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
    File destFile = new File(destinationDir, zipEntry.getName());
    String destDirPath = destinationDir.getCanonicalPath();
    String destFilePath = destFile.getCanonicalPath();

    if (!destFilePath.startsWith(destDirPath + File.separator)) {
      throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
    }

    return destFile;
  }

  /**
   * @return this instance's public key as a colon-delimited, hex-encoded string
   */
  public String getPublicKey() {
    return pluginKeyPair.getPublicKey();
  }

  /**
   * @return this instance's private key as a colon-delimited, hex-encoded string
   */
  public String getPrivateKey() {
    return pluginKeyPair.getPrivateKey();
  }

  /**
   * Creates a plugin package ("*.plugin" file) from the provided <code>jarFile</code> using the provided <code>publicKey</code>.
   * The output file will be written to the current working directory.
   *
   * @param jarFile   path to the jar file to encrypt
   * @param publicKey public key to be used for encryption
   */
  public void createPackage(String jarFile, String publicKey) throws IOException {
    createPackage(jarFile, publicKey, Paths.get(jarFile).getParent().toString());
  }

  /**
   * Creates a plugin package ("*.plugin" file) from the provided <code>pathToJar</code> using the provided <code>publicKey</code>.
   * The output file will be written to <code>outputDirectory</code>.
   *
   * @param pathToJar       path to the jar file to encrypt
   * @param publicKey       public key to be used for encryption
   * @param outputDirectory location to store the output file
   */
  public void createPackage(String pathToJar, String publicKey, String outputDirectory) throws IOException {

    /* Pull the file name off of the path. The file name is used to determine the package name. */
    String jarName = Paths.get(pathToJar).getFileName().toString();
    String packageName = Paths.get(outputDirectory, jarName.replace(jarExtension, packageExtension)).toString();

    /* Create a SigMan and sign the Jar. This signature must be done before this instance's pluginKeyPair is updated for encryption */
    SignatureManager signatureManager = new SignatureManager(pluginKeyPair);
    signatureManager.sign(Paths.get(pathToJar));

    /* Save the RSA public key that was used */
    Path publicKeyFile = Paths.get(outputDirectory, rsaKeyFileName);
    Files.deleteIfExists(publicKeyFile);
    Files.write(publicKeyFile, pluginKeyPair.getEncodedPublicKey().getEncoded(), CREATE_NEW);

    /* Generate a session key to encrypt all files */
    cipherKeyFile = Paths.get(outputDirectory, sessionKeyFileName);

    String sigCipher = signatureFileExtension + cipherTextExtension;
    String encryptedJarFilePath = pathToJar + cipherTextExtension;
    String encryptedSignatureCipherPath = pathToJar + sigCipher;

    /* Compress the input file and the signature file together */
    List<String> files = Arrays.asList(
        encryptedJarFilePath, // Encrypted JAR.
        encryptedSignatureCipherPath, // Encrypted signature cipher.
        cipherKeyFile.toString(), // Cipher key.
        publicKeyFile.toString()); // Public key file.

    FileOutputStream outputStream = new FileOutputStream(packageName);
    ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);

    /* Update this instance's key pair for cryptography */
    pluginKeyPair = PluginKeyPair.fromPublicKey(PluginKeyPair.dehexify(publicKey));
    /* Encrypt the jar file and signature file */
    try {
      encryptFile(pathToJar);
      encryptFile(pathToJar + signatureFileExtension);
    } catch (IllegalBlockSizeException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException e) {
      throw new PluginRegistrationException(jarName);
    }

    /* Zip the files */
    for (String fileName : files) {
      File file = new File(fileName);
      FileInputStream inputStream = new FileInputStream(file);
      ZipEntry entry = new ZipEntry(file.getName());
      zipOutputStream.putNextEntry(entry);

      byte[] bytes = new byte[bufferSize];
      int length;
      while ((length = inputStream.read(bytes)) >= 0) {
        zipOutputStream.write(bytes, 0, length);
      }
      inputStream.close();
    }
    zipOutputStream.close();
    outputStream.close();

    /* Cleanup */
    destroySessionKey();
    Files.deleteIfExists(Paths.get(pathToJar + signatureFileExtension));
    Files.deleteIfExists(Paths.get(rsaKeyFileName));
    Files.deleteIfExists(Paths.get(cipherKeyFile.toString()));
    Files.deleteIfExists(Paths.get(encryptedJarFilePath));
    Files.deleteIfExists(Paths.get(encryptedSignatureCipherPath));
    Files.deleteIfExists(publicKeyFile);
  }

  /**
   * Helper function that will extract the plugin into a temporary directory.
   *
   * @param pluginFile path to the jar file to decrypt
   * @param privateKey private key to be used for decryption
   * @return string containing the path at which the plugin was extracted
   * @throws IOException on any file access error
   */
  private String doExtraction(String pluginFile, String privateKey) throws IOException {
    /* The session key must be in the archive */
    final String tempDirPrefix = "plugin-registry-";
    Path outputPath = Files.createTempDirectory(tempDirPrefix);
    Path pluginBase = Paths.get(pluginFile).getFileName();
    /* Drop the package extension to determine what the jar was called originally */
    String baseName = pluginBase.toString().replace(packageExtension, "");
    String jarName = pluginBase.toString().replace(packageExtension, jarExtension);
    cipherKeyFile = Paths.get(outputPath.toString(), sessionKeyFileName);

    unzipPackage(pluginFile, outputPath.toString());
    String publicKey = readPublicKey(outputPath.toString());

    try {
      /* Decrypt the Jar */
      decryptFile(baseName + jarExtension, outputPath);
      /* Decrypt the signature file */
      decryptFile(baseName + jarExtension + signatureFileExtension, outputPath);
    } catch (NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException e) {
      throw new PluginRegistrationException(baseName, e);
    }
    /* Verify the Jar */
    pluginKeyPair = PluginKeyPair.fromKeys(PluginKeyPair.dehexify(privateKey), PluginKeyPair.dehexify(publicKey));
    SignatureManager signatureManager = new SignatureManager(pluginKeyPair);
    signatureManager.verify(Paths.get(outputPath.toString(), baseName + jarExtension), Paths.get(outputPath.toString(), baseName + jarExtension + signatureFileExtension));
    return Paths.get(outputPath.toString()).toString();
  }


  /**
   * Extracts a plugin package ("*.plugin" file) from the provided <code>pluginFile</code> using the provided <code>privateKey</code>.
   * The plugin package is expected to be in the current working directory.
   *
   * @param pluginFile path to the jar file to decrypt
   * @param privateKey private key to be used for decryption
   */
  public String extractPackage(String pluginFile, String privateKey) throws IOException {
    Path pluginBase = Paths.get(pluginFile).getFileName();
    String jarName = pluginBase.toString().replace(packageExtension, jarExtension);
    String outputPath = doExtraction(pluginFile, privateKey);
    return Paths.get(outputPath, jarName).toString();
  }

  /**
   * Extracts a plugin package ("*.plugin" file) from the provided <code>pluginFile</code> using the provided <code>privateKey</code>.
   * The plugin package is expected to be in the current working directory. Throws a <code>PluginRegistrationException</code> if
   * the plugin's (public) signature key is not found in the whitelist.
   *
   * @param pluginFile          path to the jar file to decrypt
   * @param privateKey          private key to be used for decryption
   * @param publicSignatureKeys whitelist of valid public signature keys
   */
  public String extractPackage(String pluginFile, String privateKey, List<String> publicSignatureKeys) throws IOException {
    /* The session key must be in the archive */
    Path pluginBase = Paths.get(pluginFile).getFileName();
    String jarName = pluginBase.toString().replace(packageExtension, jarExtension);
    String baseName = pluginBase.toString().replace(packageExtension, "");
    String outputPath = doExtraction(pluginFile, privateKey);

    String publicKey = readPublicKey(outputPath);
    if (!publicSignatureKeys.contains(publicKey)) {
      throw new PluginRegistrationException(baseName);
    }
    return Paths.get(outputPath, jarName).toString();
  }

  /**
   * Sets the path to the clear-text file that this instance will operate on. If <code>encrypt</code> is invoked, the clear-text file will be encrypted and output as the cipher-text file. If <code>decrypt</code> is invoked, the cipher-text will be decrypted and output as the clear-text file.
   *
   * @param path path to the file to be used the clear-text
   */
  private void setClearTextFile(String path) {
    clearTextFile = Paths.get(path);
  }

  /**
   * Sets the path to the cipher-text file that this instance will operate on. If <code>encrypt</code> is invoked, the clear-text file will be encrypted and output as the cipher-text file. If <code>decrypt</code> is invoked, the cipher-text will be decrypted and output as the clear-text file.
   *
   * @param path path to the file to be used the cipher-text
   */
  private void setCipherTextFile(String path) {
    cipherTextFile = Paths.get(path);
  }

  /**
   * Creates the session key used for the AES encryption. The output is an RSA-encrypted key file.
   *
   * @throws IllegalBlockSizeException
   * @throws NoSuchPaddingException
   * @throws NoSuchAlgorithmException
   * @throws InvalidKeyException
   * @throws IOException
   */
  private void generateSessionKey() throws IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IOException {

    /* Generate the session key */
    SecureRandom secureRandom = new SecureRandom();
    byte[] sessionKeyBytes = new byte[16];
    secureRandom.nextBytes(sessionKeyBytes);
    sessionKey = new SecretKeySpec(sessionKeyBytes, algorithm);
    writeSessionKey();
  }

  /**
   * Transforms the clear-text file into cipher-text using AES encryption. All files processed in a single batch are part of the same encryption "session" and share a "session key" that is also produced by this method. The session key must be protected from disclosure since it can decrypt any file that this method operates on. As such, the key file produced is encrypted using RSA and this instance's <code>PluginKeyPair</code>.
   *
   * @throws InvalidKeyException       thrown if session key is invalid
   * @throws IOException               thrown if either the session key or the cipher text file could not be written
   * @throws IllegalBlockSizeException thrown if no algorithm provider can provide a 2048-bit RSA <code>Cipher</code>. It should never be thrown.
   * @throws NoSuchAlgorithmException  thrown if no algorithm provider can provide an AES <code>Cipher</code>. It should never be thrown.
   * @throws NoSuchPaddingException    thrown if no algorithm provider can provide an AES <code>Cipher</code> that uses PKCS5 padding. It should never be thrown.
   */
  private void encrypt() throws InvalidKeyException, IOException, IllegalBlockSizeException, NoSuchAlgorithmException, NoSuchPaddingException {

    if (sessionKey == null) {
      generateSessionKey();
    }

    cipher.init(Cipher.ENCRYPT_MODE, sessionKey);
    byte[] initVector = cipher.getIV();

    /* Write the IV and cipher blocks */
    try (
        InputStream fileIn = Files.newInputStream(clearTextFile);
        OutputStream fileOut = Files.newOutputStream(cipherTextFile);
        CipherOutputStream cipherOut = new CipherOutputStream(fileOut, cipher)
    ) {
      fileOut.write(initVector);
      final byte[] bytes = new byte[bufferSize];
      for (int length = fileIn.read(bytes); length != -1; length = fileIn.read(bytes)) {
        cipherOut.write(bytes, 0, length);
      }
    }
  }

  /**
   * Decrypts the provided cipher text file and writes it into the provided clear text file. The instance must have
   * a valid key file, private key and cipher text file. The private key is used to unwrap the AES session key from the
   * key file.
   *
   * @throws InvalidKeyException                thrown if session key is invalid
   * @throws IOException                        thrown if either the session key or the cipher text file could not be read
   * @throws InvalidAlgorithmParameterException thrown if the initialization vector in the cipher text is invalid
   * @throws NoSuchAlgorithmException           thrown if no algorithm provider can provide an AES <code>Cipher</code>. It should never be thrown.
   * @throws NoSuchPaddingException             thrown if no algorithm provider can provide an AES <code>Cipher</code> that uses PKCS5 padding. It should never be thrown.
   */
  private void decrypt() throws
      InvalidKeyException, IOException, InvalidAlgorithmParameterException,
      NoSuchAlgorithmException, NoSuchPaddingException {
    readSessionKey();
    try (FileInputStream fileIn = new FileInputStream(String.valueOf(cipherTextFile))) {
      byte[] initVector = new byte[16];
      fileIn.read(initVector);
      cipher.init(Cipher.DECRYPT_MODE, sessionKey, new IvParameterSpec(initVector));
      try (
          CipherInputStream decryptedStream = new CipherInputStream(fileIn, cipher);
          OutputStream decryptedClearText = Files.newOutputStream(clearTextFile)
      ) {

        final byte[] bytes = new byte[bufferSize];
        for (int length = decryptedStream.read(bytes); length != -1; length = decryptedStream.read(bytes)) {
          decryptedClearText.write(bytes, 0, length);
        }
      }
    }
  }

  /**
   * Writes the AES session key to a key file as a "wrapped" (RSA-encrypted) key.
   *
   * @throws IOException               thrown if the key file cannot be written
   * @throws InvalidKeyException       thrown if the private key is invalid or it results in an invalid <code>SecretKey</code>
   * @throws NoSuchAlgorithmException  thrown if no algorithm provider can provide an RSA <code>Cipher</code></Code>. It should never be thrown.
   * @throws IllegalBlockSizeException thrown if no algorithm provider can provide a 2048-bit RSA <code>Cipher</code>. It should never be thrown.
   * @throws NoSuchPaddingException    thrown if no algorithm provider can provide an RSA <code>Cipher</code> that uses no padding. It should never be thrown.
   */
  private void writeSessionKey() throws IOException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException {

    /* Prepare an RSA cipher to wrap the AES session key before writing to the file */
    Cipher wrapper = Cipher.getInstance(keyWrapAlgorithm);
    wrapper.init(Cipher.WRAP_MODE, pluginKeyPair.getEncodedPublicKey());

    byte[] wrappedKey = wrapper.wrap(sessionKey);
    try (DataOutputStream dataOut = new DataOutputStream(new FileOutputStream(String.valueOf(cipherKeyFile)))) {
      dataOut.writeInt(wrappedKey.length);
      dataOut.write(wrappedKey);
    }
  }

  /**
   * Reads the wrapped session key from the key file and unwraps it into an AES session key.
   *
   * @throws IOException              thrown if the key file cannot be read
   * @throws InvalidKeyException      thrown if the private key is invalid or it results in an invalid <code>SecretKey</code>
   * @throws NoSuchAlgorithmException thrown if no algorithm provider can provide an RSA <code>Cipher</code></Code>. It should never be thrown.
   * @throws NoSuchPaddingException   thrown if no algorithm provider can provide an RSA <code>Cipher</code> that uses no padding. It should never be thrown.
   */
  private void readSessionKey() throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {

    /* Prepare an RSA cipher to unwrap the AES session key from the file */
    Cipher unwrapper = Cipher.getInstance(keyWrapAlgorithm);
    unwrapper.init(Cipher.UNWRAP_MODE, pluginKeyPair.getEncodedPrivateKey());

    try (DataInputStream dataIn = new DataInputStream(new FileInputStream(String.valueOf(cipherKeyFile)))) {
      int length = dataIn.readInt();
      byte[] wrappedKey = new byte[length];
      dataIn.read(wrappedKey, 0, length);

      sessionKey = (SecretKey) unwrapper.unwrap(wrappedKey, algorithm, Cipher.SECRET_KEY);
    }
  }

  /**
   * Clears the session key used for the AES encryption. This should be called as soon as possible
   * after the session key is no longer needed.
   */
  private void destroySessionKey() {
    if (sessionKey != null) {
      SecureRandom secureRandom = new SecureRandom();
      byte[] sessionKeyBytes = new byte[16];
      secureRandom.nextBytes(sessionKeyBytes);
      sessionKey = new SecretKeySpec(sessionKeyBytes, algorithm);
      sessionKey = null;
      Arrays.fill(sessionKeyBytes, (byte) 0);
    }
  }

  /**
   * Helper function to perform the <code>encrypt</code> operation on a specified input file and produce consistently named cipher-text file.
   *
   * @param file
   * @throws IllegalBlockSizeException
   * @throws NoSuchPaddingException
   * @throws NoSuchAlgorithmException
   * @throws InvalidKeyException
   * @throws IOException
   */
  private void encryptFile(String file) throws IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IOException {
    setClearTextFile(file);
    setCipherTextFile(file + cipherTextExtension);
    encrypt();
  }

  /**
   * Helper function to perform the <code>decrypt</code> operation on a specified input file and produce consistently named clear-text file.
   *
   * @param file
   * @param outputDirectory
   * @throws NoSuchAlgorithmException
   * @throws NoSuchPaddingException
   * @throws InvalidAlgorithmParameterException
   * @throws InvalidKeyException
   * @throws IOException
   */
  private void decryptFile(String file, Path outputDirectory) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, IOException {
    setClearTextFile(Paths.get(outputDirectory.toString(), file).toString());
    setCipherTextFile(Paths.get(outputDirectory.toString(), file + cipherTextExtension).toString());
    decrypt();
  }

  /**
   * Helper function to read an RSA public key from a file and convert it to a "hexified" string.
   *
   * @param outputPath
   * @return
   * @throws IOException
   */
  private String readPublicKey(String outputPath) throws IOException {
    Path keyfile = Paths.get(outputPath, rsaKeyFileName);
    return PluginKeyPair.hexify(Files.readAllBytes(keyfile));
  }

  /**
   * Helper function to decompress a given Zip file within the specified output directory.
   *
   * @param zipFile
   * @param outputPath
   * @throws IOException
   */
  private void unzipPackage(String zipFile, String outputPath) throws IOException {

    File destDir = new File(outputPath);
    byte[] buffer = new byte[bufferSize];
    ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
    ZipEntry zipEntry = zis.getNextEntry();

    while (zipEntry != null) {

      File newFile = newFile(destDir, zipEntry);
      FileOutputStream fos = new FileOutputStream(newFile);
      int len;
      while ((len = zis.read(buffer)) > 0) {
        fos.write(buffer, 0, len);
      }
      fos.close();
      zipEntry = zis.getNextEntry();
    }
    zis.closeEntry();
    zis.close();
  }
}