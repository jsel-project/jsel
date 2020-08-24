package techmoc.extensibility.pluginlibrary;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;

import static java.nio.file.StandardOpenOption.CREATE_NEW;

public class SignatureManager {

  private PluginKeyPair pluginKeyPair;

  public SignatureManager(){
    pluginKeyPair = new PluginKeyPair();
  }

  public SignatureManager(PluginKeyPair keyPair){
    this.pluginKeyPair = keyPair;
  }

  public PluginKeyPair getPluginKeyPair(){
    return pluginKeyPair;
  }

  /**
   * Signs the file specified by the given <code>Path</code> using this <code>PluginKeyPair</code>'s <code>PrivateKey</code>
   * and signature algorithm. The file's signature will be written to a file adjacent to the input file with a <code>.sig</code> extension.
   * @param file Full path to the file to be signed.
   */
  public void sign(Path file) {

    PrivateKey privateKey = pluginKeyPair.getEncodedPrivateKey();

    try {

      /* Read all of the data */
      byte[] data = Files.readAllBytes(file);

      Signature signer = Signature.getInstance(pluginKeyPair.getAlgorithm());
      signer.initSign(privateKey);
      signer.update(data);

      /* Sign the data */
      byte[] messageSignature = signer.sign();

      Path signatureFile = Paths.get(file.toString() + ".sig");
      Files.deleteIfExists(signatureFile);
      Files.write(signatureFile, messageSignature, CREATE_NEW);
    } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | IOException e) {
      /* TODO: Handle existence cases for input file */
      e.printStackTrace();
    }

  }

  /**
   * Verifies that the <code>messageFile</code> has not been modified by checking its hash against the
   * <code>signatureFile</code>. Verification is based on this <code>PluginKeyPair</code>'s public key and
   * signature algorithm.
   * @param messageFile Full path to a file that is to be verified
   * @param signatureFile Full path to a file that contains the signature information for the given <code>messageFile</code>
   * @return <code>True</code> if the expected signature contained in <code>signatureFile</code> matches the actual signature of the <code>messageFile</code>. <Code>False</Code> otherwise.
   */
  public boolean verify(Path messageFile, Path signatureFile) {

    PublicKey publicKey = pluginKeyPair.getEncodedPublicKey();

    byte[] messageText;
    byte[] signature;
    try {
      messageText = Files.readAllBytes(messageFile);
      signature = Files.readAllBytes(signatureFile);
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }

    Signature verifier;
    try {
      verifier = Signature.getInstance(pluginKeyPair.getAlgorithm());
      verifier.initVerify(publicKey);
      verifier.update(messageText);
      boolean verified = verifier.verify(signature);
      if(verified){
        return true;
      }else {
        throw new PluginRegistrationException(messageFile.toString());
      }
    } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
      e.printStackTrace();
      return false;
    }

  }

}
