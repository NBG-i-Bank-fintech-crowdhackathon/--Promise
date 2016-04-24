package com.crowdhackathon.antigravity.fintech;

import android.util.Log;

import org.spongycastle.util.io.pem.PemObject;
import org.spongycastle.util.io.pem.PemWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.RSAKeyGenParameterSpec;
import com.crowdhackathon.antigravity.fintech.Preferences;


public class CryptData {

    public static KeyPair keypair;
    public static String publicKey;
    private static final int KEY_SIZE = 512;

    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    public static KeyPair generate() {
        try {
            SecureRandom random = new SecureRandom();
            RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(KEY_SIZE, RSAKeyGenParameterSpec.F4);
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "SC");
            generator.initialize(spec, random);
            keypair = generator.generateKeyPair();
            publicKey = keypair.getPublic().getEncoded().toString();
            writePublicKeyToPreferences(keypair);
            return keypair;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void writePublicKeyToPreferences(KeyPair key) {
        StringWriter publicStringWriter = new StringWriter();
        try {
            PemWriter pemWriter = new PemWriter(publicStringWriter);
            pemWriter.writeObject(new PemObject("PUBLIC KEY", key.getPublic().getEncoded()));
            pemWriter.flush();
            pemWriter.close();
            Preferences.putString(Preferences.RSA_PUBLIC_KEY, publicStringWriter.toString());
        } catch (IOException e) {
            Log.e("RSA", e.getMessage());
            e.printStackTrace();
        }
    }

}
