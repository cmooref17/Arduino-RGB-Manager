package com.example.xflip.arduinorgbmanager;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

    String url = ""; // = "192.168.1.100/index.html";

    boolean autoSet; //Auto set color without pressing apply/confirm
    boolean canSend; //For debugging

    String[] modes = {"Solid","Flash","Pulse","Heartbeat"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        background = findViewById(R.id.background);
        loadPrefs();
        initUrlBox();
    }

    void loadPrefs() {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        autoSet = preferences.getBoolean("autoSet", false);
        canSend = preferences.getBoolean("canSend", true);
        url = preferences.getString("url", "");

        //Set switch states, not just the booleans
        Switch autoSwitch = findViewById(R.id.switch1);
        autoSwitch.setChecked(autoSet);
        Switch canSendSwitch = findViewById(R.id.switch2);
        canSendSwitch.setChecked(canSend);
    }

    void initUrlBox() {
        EditText urlBox = (EditText) findViewById(R.id.editText);
        urlBox.setText(url);
    }

    void saveUrlToPreferences() {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor edit = preferences.edit();
        edit.putString("url", url);
        edit.apply();
    }

    public void setAutoColorChange(View view) {
        Switch switchButton = (Switch) findViewById(R.id.switch1);
        autoSet = switchButton.isChecked();
        Log.d("autoSelect", "autoSet = " + autoSet);
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor edit = preferences.edit();
        edit.putBoolean("autoSet", autoSet);
        edit.apply();
    }

    public void setCanSendColor(View view) {
        Switch switchButton = (Switch) view;
        canSend = switchButton.isChecked();
        Log.d("canSendColor", "canSendColor = " + canSend);
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor edit = preferences.edit();
        edit.putBoolean("canSend", canSend);
        edit.apply();
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
                //.lightnessSliderOnly()
                .density(12)
                .setOnColorChangedListener(new OnColorChangedListener() {
                    @Override
                    public void onColorChanged(int selectedColor) {

                        currentColor = selectedColor;
                        red = (selectedColor >> 16)   & 0xFF;
                        green = (selectedColor >>  8) & 0xFF;
                        blue = (selectedColor >>  0)  & 0xFF;

                        Log.d("RGB", "R [" + red + "] - G [" + green + "] - B [" + blue + "] - Brightness [" + brightness + "]");

                        if(autoSet) {
                            sendColor(red, green, blue, brightness);
                        }
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

    void sendColor(int r, int g, int b, int brightness) {
        Log.d("sendColor", "Sending data to Arduino....");
        EditText urlBox = (EditText) findViewById(R.id.editText);
        url = urlBox.getText().toString();
        Log.d("test", url);
        background.setBackgroundColor(currentColor);

        if(!canSend)
            return;

        OkHttpClient client = new OkHttpClient();

        //Log rgb/brightness values
        Log.d("sendColor","Current color: " + currentColor);
        Log.d("sendColor","red: " + r + ", green: " + g + ", blue: " + b + ", brightness: " + brightness);

        String thisURL = "http://" + url;

        //Log URL
        Log.d("sendColor","Url = " + thisURL);

        String json = createJSON(r, g, b, brightness);

        //Log JSON
        Log.d("sendColor","JSON = " + json);

        //Build request
        Request req = new Request.Builder()
                .url(thisURL)
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
                //Toast.makeText(MainActivity.this, "Call connected!", Toast.LENGTH_SHORT).show();
            }
        });

        saveUrlToPreferences();
        Log.d("sendColor", "If seeing this log, sending color was a success. I think");
    }

    String createJSON(int r, int g, int b, int brightness) {
        String sRed        = Integer.toString(r);
        String sGreen      = Integer.toString(g);
        String sBlue       = Integer.toString(b);
        //String sBrightness = Integer.toString(brightness);
        String sBrightness = "100";

        //String json = (sRed + ',' + sGreen + ',' + sBlue + ',' + sBrightness);
        //return json;
        String json = "{\"color\": [" + sRed + "," + sGreen + "," + sBlue + "," + sBrightness + "]}";
        Log.d("createJSON", "Created JSON: " + json);
        return json;
    }
}
