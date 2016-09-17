package com.crowdhackathon.antigravity.fintech;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.spongycastle.util.io.pem.PemObject;
import org.spongycastle.util.io.pem.PemWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.HashMap;
import java.util.Map;

public class Network {


    private static String masterIp = "http://10.0.2.2:8011";
    private static String temp_msg;
    private static String temp_signature;
    private static Map temp_map;

    public static String scannedCode;

    static RequestQueue MyRequestQueue;

    public static boolean uploadTransaction(String data, Class callerClass) {
        KeyPair key = CryptData.keypair;
        PrivateKey privateKey = key.getPrivate();
        PublicKey publicKey = key.getPublic();
        byte[] bytes = {0x3F};//data.getBytes();
        System.out.println(Preferences.getString(Preferences.RSA_PUBLIC_KEY));
        MyRequestQueue = Volley.newRequestQueue(MainActivity.myContext);

        try {
            byte[] answer = sign(bytes, privateKey);
            temp_msg = new String(bytes);
            temp_signature = new String(answer);

            Log.w("", publicKey.getFormat());
            temp_map = new HashMap<String, String>();
            temp_map.put("publicKey", Preferences.getString(Preferences.RSA_PUBLIC_KEY));
            temp_map.put("transactionMessage", data);


            StringWriter publicStringWriter = new StringWriter();
            try {
                PemWriter pemWriter = new PemWriter(publicStringWriter);
                pemWriter.writeObject(new PemObject("Sign", answer));
                pemWriter.flush();
                pemWriter.close();
                Preferences.putString("Trans_X", publicStringWriter.toString());
            } catch (IOException e) {
                Log.e("RSA", e.getMessage());
                e.printStackTrace();
            }
            System.out.println(Preferences.getString("Trans_X"));
            temp_map.put("signature", Preferences.getString("Trans_X"));

            String url = masterIp + "/newTransaction";

            StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.w("", "xx" + response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.w("", "x3333x" + error.getMessage());
                }
            }) {
                protected Map<String, String> getParams() {

                    return temp_map;
                }
            };
            MyRequestQueue.add(MyStringRequest);

        } catch (Exception e) {

        }
        return true;
    }



    private static byte[] sign(byte[] bytes, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA", "SC");
        signature.initSign(privateKey);
        signature.update(bytes);
        //testNetwork();
        return signature.sign();
    }

}
