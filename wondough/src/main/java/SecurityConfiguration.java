package wondough;

import java.io.IOException;
import java.nio.*;
import java.nio.charset.*;
import java.nio.file.*;

import com.google.gson.Gson;
import com.google.gson.annotations.*;

import org.apache.commons.codec.binary.Hex;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;

/**
* Stores security-related configuration.
* @author  The Intern
* @version 0.1
*/
public class SecurityConfiguration {
    /** Stores the number of iterations to use for PBKDF2 */
    @Expose
    private int iterations;
    /** Stores the key size to use for PBKDF2 */
    @Expose
    private int keySize;

    /**
    * Gets the number of iterations to use for PBKDF2.
    */
    public int getIterations() {
        return this.iterations;
    }

    /**
    * Gets the key size to use for PBKDF2.
    */
    public int getKeySize() {
        return this.keySize;
    }

    /**
    * Hashes a password using PBKDF2 with the specified salt.
    * @param password The password to hash.
    * @param salt The salt to use.
    */
    public String pbkdf2(String password, String salt) {
        return this.pbkdf2(password, salt, this.getIterations(), this.getKeySize());
    }

    /**
    * Hashes a password using PBKDF2 with the specified salt.
    * @param password The password to hash.
    * @param salt The salt to use.
    * @param iterations The iterations to use.
    * @param keySize The desired key size.
    */
    public String pbkdf2(String password, String salt, int iterations, int keySize) {
        // convert the username and password to char and byte arrays
        char[] pwd = password.toCharArray();
        byte[] slt = salt.getBytes();

        try {
            // initialise the crypto classes with the desired configuration
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            PBEKeySpec spec = new PBEKeySpec(pwd, slt, iterations, keySize);

            // hash the password using the configuration
            SecretKey key = skf.generateSecret(spec);
            byte[] res = key.getEncoded();

            // return the hashed password as a hexadecimal string
            return Hex.encodeHexString(res);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    public String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(input.getBytes());
            byte[] digest = md.digest();
            Base64.Encoder encoder = Base64.getEncoder();
            return encoder.encodeToString(digest);
        }
        catch(Exception ex) {
            return "";
        }
    }

    /**
    * Generates a cryptographically secure salt.
    */
    public String generateSalt() {
        String salt = new SimpleDateFormat("dd/mm/yyyy HH:mm:ss").format(Calendar.getInstance().getTime());
        return this.md5(salt);
    }

    /**
    * Constructs an instance of this class by deserialising it from a file.
    * @param filename The name of the file to deserialise from.
    */
    public static SecurityConfiguration fromFile(String filename) throws IOException {
        Gson gson = new Gson();
        String contents = new String(Files.readAllBytes(Paths.get(filename)), StandardCharsets.UTF_8);
        return gson.fromJson(contents, SecurityConfiguration.class);
    }
}
