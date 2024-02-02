package com.example.sos;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.time.Duration;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private CameraManager cameraManager;
    private String getCameraID;
    private EditText inputET;
    private TextToSpeech textToSpeech;
    private RadioGroup outputRG;

    private int choice;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inputET = (EditText) findViewById(R.id.inputET);
        outputRG = findViewById(R.id.outputRadioGroup);

        // cameraManager to interact with camera devices
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        // create an object textToSpeech and adding features into it
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {

                // if No error is found then only it will run
                if(i!=TextToSpeech.ERROR){
                    // To Choose language of speech
                    textToSpeech.setLanguage(Locale.UK);
                }
            }
        });

        choice=1;
        // on below line we are adding check change listener for our radio group.
        outputRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                // on below line we are getting radio button from our group.
                RadioButton radioButton = findViewById(checkedId);
                if (radioButton.getText().toString().equals("Light")) choice = 1;
                else if (radioButton.getText().toString().equals("Sound")) choice = 2;
                else choice = 3;
            }
        });

        // Exception is handled, because to check whether
        // the camera resource is being used by another
        // service or not.
        try {
            // O means back camera unit,
            // 1 means front camera unit
            getCameraID = cameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    // RequiresApi is set because, the devices which are
    // below API level 10 don't have the flash unit with
    // camera.
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void toggleFlashLight(View view) {
                // Exception is handled, because to check
                // whether the camera resource is being used by
                // another service or not.
                try {
                    // true sets the torch in ON mode
                    String helpText = inputET.getText().toString();
                    String morseCodeText;
                    if (helpText.isEmpty() == false) {
                        helpText=helpText.toLowerCase();
                        if ((choice == 2) || (choice == 3))
                            textToSpeech.speak(helpText, TextToSpeech.QUEUE_FLUSH, null, null);
                        if ((choice == 1) || (choice == 3)) {
                            morseCodeText = englishToMorse(helpText);
                            for (int i = 0; i < morseCodeText.length(); i++) {
                                if (morseCodeText.charAt(i) == '-') {
                                    cameraManager.setTorchMode(getCameraID, true);
                                    try {
                                        Thread.sleep(3000);
                                        cameraManager.setTorchMode(getCameraID, false);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (morseCodeText.charAt(i) == '.') {
                                    cameraManager.setTorchMode(getCameraID, true);
                                    try {
                                        Thread.sleep(1000);
                                        cameraManager.setTorchMode(getCameraID, false);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (morseCodeText.charAt(i) == ' ') {
                                    cameraManager.setTorchMode(getCameraID, false);
                                    try {
                                        Thread.sleep(2000);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (morseCodeText.charAt(i) == '/') {
                                    cameraManager.setTorchMode(getCameraID, false);
                                    try {
                                        Thread.sleep(4000);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            cameraManager.setTorchMode(getCameraID, false);
                        }
                    }

                } catch (CameraAccessException e) {
                    // prints stack trace on standard error
                    // output error stream
                    e.printStackTrace();
                }
    }

    public static String englishToMorse(String englishLang)
    {   // store the all the alphabet in an array
        char[] letter = { 'a', 'b', 'c', 'd', 'e', 'f',
                'g', 'h', 'i', 'j', 'k', 'l',
                'm', 'n', 'o', 'p', 'q', 'r',
                's', 't', 'u', 'v', 'w', 'x',
                'y', 'z', '1', '2', '3', '4',
                '5', '6', '7', '8', '9', '0' };
        // Morse code by indexing
        String[] code
                = { ".-",   "-...", "-.-.", "-..",  ".",
                "..-.", "--.",  "....", "..",   ".---",
                "-.-",  ".-..", "--",   "-.",   "---",
                ".--.", "--.-", ".-.",  "...",  "-",
                "..-",  "...-", ".--",  "-..-", "-.--",
                "--..", "|" };

        String morseCode = "";
        for (int i = 0; i < englishLang.length(); i++) {
            for (int j = 0; j < letter.length; j++) {
                if (englishLang.charAt(i) == letter[j]) {
                    morseCode+=code[j] +" ";
                    break;
                }
            }
            if (englishLang.charAt(i) == ' ') {
                morseCode+="/";
            }
        }
        return morseCode;
    }



    // when you click on button and torch open and
    // you do not close the torch again this code
    // will off the torch automatically
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void finish() {
        super.finish();
        try {
            // true sets the torch in OFF mode
            cameraManager.setTorchMode(getCameraID, false);

            // Inform the user about the flashlight
            // status using Toast message
            Toast.makeText(getApplicationContext(), "Flashlight is turned OFF", Toast.LENGTH_SHORT).show();
        } catch (CameraAccessException e) {
            // prints stack trace on standard error
            // output error stream
            e.printStackTrace();
        }
    }

    public void clearButtonOnClick(View view){
        inputET.setText("");
        outputRG.check(R.id.lightRadioButton);
        try {
            // false sets the torch in OFF mode
            cameraManager.setTorchMode(getCameraID, false);
        } catch (CameraAccessException e) {
            // prints stack trace on standard error
            // output error stream
            e.printStackTrace();
        }
    }
}