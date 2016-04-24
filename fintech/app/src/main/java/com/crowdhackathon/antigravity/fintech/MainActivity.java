package com.crowdhackathon.antigravity.fintech;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;

import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.ViewFlipper;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.android.volley.*;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

public class MainActivity extends Activity {

    public static Context myContext;
    private static final int KEY_SIZE = 1024;
    private Map<String, String> TransactionData;
    RequestQueue MyRequestQueue;


    private static int mYear;
    private static int mMonth;
    private static int mDay;
    private static String ammount;

    private TextView mDateDisplay;
    private Button mPickDate;
    private Button pickRecep;
    static final int DATE_DIALOG_ID = 0;
    private Button send;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_page);
        myContext = getApplicationContext();

        Preferences.init(this);

        CryptData.generate();

        final EditText edittext = new EditText(myContext);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Enter Username");
        alert.setView(edittext);

        alert.setPositiveButton("Login", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //What ever you want to do with the value
                Editable YouEditTextValue = edittext.getText();

                String h = edittext.getText().toString();
                Preferences.USERNAME = h;
                System.out.println(h);
                ImageView imageView = (ImageView) findViewById(R.id.qrimage);
                try {
                    Bitmap bitmap = encodeAsBitmap(Preferences.USERNAME);
                    imageView.setImageBitmap(bitmap);
                } catch (WriterException e) {
                    e.printStackTrace();
                }
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                finish();
            }
        });

        alert.show();

        flipper = (ViewFlipper) findViewById(R.id.flipper);


        mDateDisplay = (TextView) findViewById(R.id.dateDisplay);
        mPickDate = (Button) findViewById(R.id.datePick);

        mPickDate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(DATE_DIALOG_ID);
            }
        });

        // get the current date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        // display the current date
        updateDisplay();

        setUpListeners();

        mPickDate.setText("Pick Date");



/*
        MyRequestQueue = Volley.newRequestQueue(this);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        KeyPair key = generate();
*/
       // sendToVerifyTest("gfh");
       // System.out.println("pj"+key.getPublic());

    }
    private void setUpListeners(){


        flipper = (ViewFlipper) findViewById(R.id.flipper);

        pickRecep = (Button) findViewById(R.id.pickRecipient);
        pickRecep.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pickRecepient();

                //goToActivity();
            }
        });

        send = (Button) findViewById(R.id.signTr);
        send.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                System.out.println();
                String day=mDay + "/" + mMonth + "/" + mYear;

                Network.uploadTransaction("{\"target\":\"" + pickRecep.getText() + "\", \"date\":\"" + day + "\", \"ammount\":\"" + ammount + "\" }", this.getClass());
            }
        });

        mPickDate = (Button) findViewById(R.id.datePick);

        mPickDate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(DATE_DIALOG_ID);
            }
        });
        EditText ammout = (EditText) findViewById(R.id.ammount);

        ammout.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                EditText ammout = (EditText) findViewById(R.id.ammount);
                ammount = ammout.getText().toString();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
            }
        });

        if (ammount!=null) {

            ammout.setText(ammount);
        }

        updateDisplay();
        ImageView imageView = (ImageView) findViewById(R.id.qrimage);
        try {
            Bitmap bitmap = encodeAsBitmap(Preferences.USERNAME);
            imageView.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    private void goToActivity(){
        Intent intent = new Intent(this, Scanner.class);
        startActivityForResult(intent, 23);
    }

    private void pickRecepient() {

        LayoutInflater inflater = (LayoutInflater)this.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.pick_rec, null);
        this.setContentView(view);
        Button button = (Button) findViewById(R.id.scan_now);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                goToActivity();

            }
        });
        Button button2 = (Button) findViewById(R.id.pickedRe);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setContentView(R.layout.main_page);
                EditText edit = (EditText) findViewById(R.id.target_email);
                Network.scannedCode = edit.getText().toString();
                System.out.println("aha!");
                setUpListeners();
                pickRecep.setText(Network.scannedCode);

            }
        });
    }

    private DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener() {
                public void onDateSet(DatePicker view, int year,
                                      int monthOfYear, int dayOfMonth) {
                    mYear = year;
                    mMonth = monthOfYear;
                    mDay = dayOfMonth;
                    updateDisplay();
                }
            };

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                return new DatePickerDialog(this,
                        mDateSetListener,
                        mYear, mMonth, mDay);
        }
        return null;
    }

    private void updateDisplay() {
        this.mPickDate.setText(
                new StringBuilder()
                        // Month is 0 based so add 1
                        .append(mMonth + 1).append("-")
                        .append(mDay).append("-")
                        .append(mYear).append(" "));
    }
    ViewFlipper flipper;

    private Animation inFromRightAnimation() {

        Animation inFromRight = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT,  +1.0f, Animation.RELATIVE_TO_PARENT,  0.0f,
                Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f
        );
        inFromRight.setDuration(100);
        inFromRight.setInterpolator(new AccelerateInterpolator());
        return inFromRight;
    }
    private Animation outToLeftAnimation() {
        Animation outtoLeft = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,  -1.0f,
                Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f
        );
        outtoLeft.setDuration(100);
        outtoLeft.setInterpolator(new AccelerateInterpolator());
        return outtoLeft;
    }
    private Animation inFromLeftAnimation() {
        Animation inFromLeft = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT,  -1.0f, Animation.RELATIVE_TO_PARENT,  0.0f,
                Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f
        );
        inFromLeft.setDuration(100);
        inFromLeft.setInterpolator(new AccelerateInterpolator());
        return inFromLeft;
    }
    private Animation outToRightAnimation() {
        Animation outtoRight = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,  +1.0f,
                Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f
        );
        outtoRight.setDuration(100);
        outtoRight.setInterpolator(new AccelerateInterpolator());
        return outtoRight;
    }

    private float x1,x2;
    static final int MIN_DISTANCE = 150;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                x2 = event.getX();
                float deltaX = x2 - x1;

                if (Math.abs(deltaX) > MIN_DISTANCE)
                {
                    // Left to Right swipe action
                    if (x2 < x1)
                    {
                        flipper.setInAnimation(inFromRightAnimation());
                        flipper.setOutAnimation(outToLeftAnimation());
                        flipper.showNext();
                    }

                    // Right to left swipe action
                    else
                    {
                        flipper.setInAnimation(inFromLeftAnimation());
                        flipper.setOutAnimation(outToRightAnimation());
                        flipper.showPrevious();
                    }

                }
                else
                {
                    // consider as something else - a screen tap for example
                }
                break;
        }
        return super.onTouchEvent(event);
    }


    private void sendToVerifyTest(String data) {
        KeyPair key = generate();
        PrivateKey privateKey = key.getPrivate();
        PublicKey publicKey = key.getPublic();
        byte[] bytes = data.getBytes();

        try {
            byte[] answer=sign(bytes, privateKey);
            String text = new String(bytes);
            String text2 = new String(answer);
            System.out.println(text);
            System.out.println(text2);
            Log.w("",publicKey.getFormat());
            TransactionData = new HashMap<String, String>();
            TransactionData.put("publicKey", publicKey.toString());
            TransactionData.put("transactionMessage", text);
            TransactionData.put("signature", text2);

            String url = "http://192.168.43.174:8011/quote";
            StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    //This code is executed if the server responds, whether or not the response contains data.
                    //The String 'response' contains the server's response.
                    Log.w("","xx"+response);
                }
            }, new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.w("","x3333x"+ error.getMessage());
                }
            }) {
                protected Map<String, String> getParams() {

                    return TransactionData;
                }
            };
            MyRequestQueue.add(MyStringRequest);

        } catch(Exception e) {

        }

    }

    private byte[] sign(byte[] bytes, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA", "SC");
        signature.initSign(privateKey);
        signature.update(bytes);
        testNetwork();
        return signature.sign();
    }

    private void testNetwork(){

        System.out.println("sss");
        String url = "http://192.168.43.174:8011/quote";
        StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //This code is executed if the server responds, whether or not the response contains data.
                //The String 'response' contains the server's response.
                Log.w("","xx"+response);
            }
        }, new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("x3333x"+ error.getMessage());
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<String, String>();
                MyData.put("publicKey", "dfdf");
                MyData.put("transactionMessage", "dd");
                MyData.put("signature", "dfgfdg");//Add the data you'd like to send to the server.
                System.out.println("32");
                return MyData;
            }
        };
        MyRequestQueue.add(MyStringRequest);
    }

    public static KeyPair generate() {
        try {
            SecureRandom random = new SecureRandom();
            RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(KEY_SIZE, RSAKeyGenParameterSpec.F4);
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "SC");
            generator.initialize(spec, random);
            return generator.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    Bitmap encodeAsBitmap(String str) throws WriterException {
        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(str,
                    BarcodeFormat.QR_CODE, 700, 700, null);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = result.get(x, y) ?  Color.BLACK : Color.WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, 700, 0, 0, w, h);
        return bitmap;
    }

    @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        setContentView(R.layout.main_page);
        System.out.println("aha!");
        setUpListeners();
        pickRecep.setText(Network.scannedCode);
    }

}
