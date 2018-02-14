package com.example.xflip.arduinorgbmanager;

import android.content.DialogInterface;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;


import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.util.Set;
import java.util.ArrayList;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.TextView;
import android.content.Intent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

public class MainActivity extends AppCompatActivity {
    ConstraintLayout background;
    boolean autoSet = false;
    int numInRow = 0;
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    int currentColor = 0;
    int red = 0;
    int green = 0;
    int blue = 0;
    int brightness = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        background = findViewById(R.id.background);

        openColorWheel();
    }

    void initBrightnessBar() {
        SeekBar brightnessSlider = (SeekBar) findViewById(R.id.brightness);

        brightnessSlider.setMax(100);

        brightnessSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                brightness = i;
                if(autoSet) {
                    sendColor(red, green, blue, brightness);
                    background.setBackgroundColor(currentColor);
                }
                else {

                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    void openColorWheel() {
        ColorPickerDialogBuilder
                .with(MainActivity.this)
                .setTitle("Choose RGB color")
                .initialColor(Color.WHITE)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .lightnessSliderOnly()
                .density(12)
                .setOnColorSelectedListener(new OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int selectedColor) {
                        if(selectedColor == -8388353)
                            numInRow++;
                        else
                            numInRow = 0;

                        currentColor = selectedColor;
                        red = (selectedColor >> 16) & 0xFF;
                        green = (selectedColor >>  8) & 0xFF;
                        blue = (selectedColor >>  0) & 0xFF;

                        Log.d("RGB", "R [" + red + "] - G [" + green + "] - B [" + blue + "] - Brightness [" + brightness + "]");

                        if(numInRow % 5 == 0 && numInRow != 0) {
                            autoSet = !autoSet;
                            if(autoSet)
                                Toast.makeText(getApplicationContext(), "Turned 'Auto Set' on", Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(getApplicationContext(), "Turned 'Auto Set' off", Toast.LENGTH_SHORT).show();
                        }

                        if(autoSet) {
                            sendColor(red, green, blue, brightness);
                        }

                        //Toast.makeText(getApplicationContext(), "Selected R: " + r + ", G: " + g + ", B: " + b, Toast.LENGTH_SHORT).show();
                    }
                })
                .setPositiveButton("Set Color", new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                        currentColor = selectedColor;
                        red = (selectedColor >> 16) & 0xFF;
                        green = (selectedColor >>  8) & 0xFF;
                        blue = (selectedColor >>  0) & 0xFF;
                        Log.d("RGB", "R [" + red + "] - G [" + green + "] - B [" + blue + "] - Brightness [" + brightness + "]");
                        sendColor(red, green, blue, brightness);
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .build()
                .show();
    }

    void getCurrentColor() {}

    void sendColor(int r, int g, int b, int brightness) {
        background.setBackgroundColor(currentColor);

        /*
        OkHttpClient client = new OkHttpClient();

        Log.d("RGB", "Sending data to Arduino....");

        String url = "192.168.1.100";

        url = url + "/index.html";

        Request req = new Request.Builder()
                .url(url)
                .post(RequestBody.create(JSON, createJSON(r,g,b,brightness)))
                .build();

        //tv.setText("Sending data to Arduino...");
        client.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Handle call failure
                Toast.makeText(MainActivity.this, "Call failed", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // OK Response...inform the user
                Toast.makeText(MainActivity.this, "Call connected!", Toast.LENGTH_SHORT).show();
            }
        });

        Toast.makeText(this, "Sent RGB: " + r + " " + g + " " + b + ", but actually didn't. #trolled", Toast.LENGTH_LONG).show();
        return;
        */
    }

    String createJSON(int r, int g, int b, int brightness) {
        String sRed = Integer.toString(r);
        String sGreen = Integer.toString(g);
        String sBlue = Integer.toString(b);
        String sBrightness = Integer.toString(brightness);

        String json = (sRed + ',' + sGreen + ',' + sBlue + ',' + sBrightness);
        return json;
    }
}
