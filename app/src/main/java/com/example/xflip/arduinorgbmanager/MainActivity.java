package com.example.xflip.arduinorgbmanager;

import android.content.DialogInterface;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;


import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorChangedListener;
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
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    int currentColor = 0;
    int red = 0;
    int green = 0;
    int blue = 0;
    int brightness = 100;

    String url = "http://192.168.1.100/index.html";

    boolean autoSet = true; //Auto set color without pressing apply/confirm
    boolean canSend = true; //For debugging

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        background = findViewById(R.id.background);
        //initBrightnessBar();
        SeekBar seekBar = findViewById(R.id.brightness);
        seekBar.setVisibility(View.GONE);
        //openColorWheel(); //Open on launch. Comment out to not open on startup
    }

    public void setAutoColorChange(View view) {
        Switch switchButton = (Switch) findViewById(R.id.switch1);
        autoSet = switchButton.isChecked();
        Log.d("autoSelect", "autoSet = " + autoSet);
        if(autoSet) {
            //Toast.makeText(getApplicationContext(), "Turned 'Auto Set' on",  Toast.LENGTH_SHORT).show();
        }
        else {
            //Toast.makeText(getApplicationContext(), "Turned 'Auto Set' off", Toast.LENGTH_SHORT).show();
        }
    }

    public void setCanSendColor(View view) {
        Switch switchButton = (Switch) view;
        canSend = switchButton.isChecked();
        Log.d("canSendColor", "canSendColor = " + canSend);
        if(canSend)
            Toast.makeText(this, "Enabled sending color to server", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, "Disabled sending color to server", Toast.LENGTH_SHORT).show();
    }

    void initBrightnessBar() {
        SeekBar brightnessSlider = (SeekBar) findViewById(R.id.brightness);
        brightnessSlider.setMax(100);
        brightnessSlider.setProgress(100);
        brightnessSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                brightness = i;
                sendColor(red, green, blue, brightness);
                background.setBackgroundColor(currentColor);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    public void openWheel(View view) {
        openColorWheel();
    }

    void openColorWheel() {
        ColorPickerDialogBuilder
                .with(MainActivity.this)
                .setTitle("Choose RGB color")
                .initialColor(Color.WHITE)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                //.lightnessSliderOnly() //No alpha bar. I use a separate brightness bar
                .density(12)
                .setOnColorChangedListener(new OnColorChangedListener() {
                    @Override
                    public void onColorChanged(int selectedColor) {

                        currentColor = selectedColor;
                        red = (selectedColor >> 16) & 0xFF;
                        green = (selectedColor >>  8) & 0xFF;
                        blue = (selectedColor >>  0) & 0xFF;

                        Log.d("RGB", "R [" + red + "] - G [" + green + "] - B [" + blue + "] - Brightness [" + brightness + "]");

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

                        if(!autoSet) //Don't send the value twice
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
        Log.d("sendColor", "Sending data to Arduino....");
        background.setBackgroundColor(currentColor);

        if(!canSend)
            return;

        OkHttpClient client = new OkHttpClient();

        //Log rgb/brightness values
        Log.d("sendColor","Current color: " + currentColor);
        Log.d("sendColor","red: " + r + ", green: " + g + ", blue: " + b + ", brightness: " + brightness);

        //Log URL
        Log.d("sendColor","Url = " + url);

        String json = createJSON(r, g, b, brightness);

        //Log JSON
        Log.d("sendColor","JSON = " + json);

        //Build request
        Request req = new Request.Builder()
                .url(url)
                .post(RequestBody.create(JSON, json))
                .build();

        Log.d("sendColor", "Sending data to Arduino...");

        //Call client
        client.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Handle call failure
                //Toast.makeText(MainActivity.this, "Call failed", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // OK Response...inform the user
                Toast.makeText(MainActivity.this, "Call connected!", Toast.LENGTH_SHORT).show();
            }
        });

        Log.d("sendColor", "If seeing this log, sending color was a success. I think");
        Toast.makeText(this, "Sending color sucessful!", Toast.LENGTH_SHORT).show();
    }

    String createJSON(int r, int g, int b, int brightness) {
        String sRed        = Integer.toString(r);
        String sGreen      = Integer.toString(g);
        String sBlue       = Integer.toString(b);
        String sBrightness = Integer.toString(brightness);

        String json = (sRed + ',' + sGreen + ',' + sBlue + ',' + sBrightness);
        return json;
    }
}
