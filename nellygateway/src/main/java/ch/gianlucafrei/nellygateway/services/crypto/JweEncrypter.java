package ch.gianlucafrei.nellygateway.services.crypto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Base64;

public class JweEncrypter implements CookieEncryptor {

    private SecretKey secretKey;

    private JweEncrypter(byte[] keyBytes) {

        this.secretKey = new SecretKeySpec(keyBytes, 0, keyBytes.length, "AES");
    }

    public static JweEncrypter loadFromFileOrCreateAndStoreNewKey(String filename) throws IOException {

        if(filename == null)
            throw new IllegalArgumentException("Filename must not be null");

        File keyFile = new File(filename);
        byte[] keyBytes;

        if(keyFile.exists())
        {
            // Read key from file
            keyBytes = Files.toByteArray(keyFile);
        }
        else{
            // Create new secret key and store it in file

            KeyGenerator keyGen = null;
            try {
                keyGen = KeyGenerator.getInstance("AES");
                keyGen.init(128); // for example
                SecretKey secretKey = keyGen.generateKey();

                // Store key om file
                keyBytes = secretKey.getEncoded();
                Files.write(keyBytes, keyFile);

            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("Cloud not create AES key", e);
            }
        }

        return new JweEncrypter(keyBytes);
    }

    public static JweEncrypter loadFromEnvironmentVariable(String variableName) {

        String key = System.getenv(variableName);

        if(key == null)
            throw new IllegalStateException("NELLY-KEY is not defined");

        // decode the base64 encoded string
        byte[] decodedKey = Base64.getDecoder().decode(key);
        return new JweEncrypter(decodedKey);
    }

    private String generateKey() {

        try {
            // Generate symmetric 128 bit AES key
            SecretKey secretKey = KeyGenerator.getInstance("AES").generateKey();
            String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
            return encodedKey;

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("AES key coule not be generated", e);
        }
    }

    @Override
    public String encryptObject(Object payload) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String payloadString = objectMapper.writeValueAsString(payload);
            return encrypt(payloadString);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not encode Json", e);
        }
    }

    private String encrypt(String payload) {

        try {
            // Create JWT
            JWEHeader header = new JWEHeader(JWEAlgorithm.DIR, EncryptionMethod.A128GCM);
            Payload jwePayload = new Payload(payload);
            JWEObject jweObject = new JWEObject(header, jwePayload);
            jweObject.encrypt(new DirectEncrypter(secretKey));

            // Serialise to compact JOSE form
            String jweString = jweObject.serialize();
            return jweString;

        } catch (JOSEException e) {
            throw new RuntimeException("JWE could not be encrypted", e);
        }
    }

    @Override
    public <T> T decryptObject(String jwe, Class<T> clazz) {
        String payload = decrypt(jwe);


        try {
            ObjectMapper objectMapper = new ObjectMapper();
            T obj = objectMapper.readValue(payload, clazz);
            return obj;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JWE could not be deserialized", e);
        }
    }

    private String decrypt(String jwe) {
        try {
            // Parse into JWE object again...
            JWEObject jweObject = JWEObject.parse(jwe);

            // Decrypt
            jweObject.decrypt(new DirectDecrypter(secretKey));

            // Get the plain text
            Payload payload = jweObject.getPayload();
            return payload.toString();
        } catch (JOSEException | ParseException e) {
            throw new RuntimeException("JWE could not be decrypted", e);
        }
    }
}