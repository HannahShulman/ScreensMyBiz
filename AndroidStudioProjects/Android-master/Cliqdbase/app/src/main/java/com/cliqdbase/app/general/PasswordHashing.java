package com.cliqdbase.app.general;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Yuval on 06/03/2015.
 *
 * @author Yuval Siev
 */
public class PasswordHashing {

    /**
     * Hashes a given password using SHA-512 encryption
     * @param password    The given password to be encrypted.
     * @return The encryption of the given password. The encryption length is 128 characters.
     */
    public static String hashPass(String password) {
        String hash = "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] digestPass = md.digest(password.getBytes());                     // Hashing the password

            StringBuilder hexPass = new StringBuilder();
            for (byte b : digestPass)
                hexPass.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));      // Appending the hex-string of the hashed bytes from the password
                                                /* Integerizing the byte    */       /* Hex */

            hash = hexPass.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hash;
    }
}
