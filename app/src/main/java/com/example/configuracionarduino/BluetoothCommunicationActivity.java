package com.example.configuracionarduino;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothCommunicationActivity extends AppCompatActivity {
    //private EditText etMessageInput;
    private UUID uuid;
    private BluetoothSocket socket;
    private InputStream input;
    private OutputStream output;

    private String lastResponse;

    private EditText etWifiSsid;
    private EditText etWifiPass;
    private EditText etServerUrl;
    private EditText etSendInterval;


    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_communication);
        //Se pása la informacion de la actividad
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        if (bundle == null) {
            // Asegurarse de que el Bundle(informacion) no sea nulo
            Log.e("onCreate", "Bundle es nulo");
            finish(); // Cerrar la actividad si el Bundle es nulo
            return;
        }

        //Obtenemos el objeto BluettothDevice del Bundle
        BluetoothDevice device = bundle.getParcelable(MainActivity.EXTRA_DEVICE_SELECTED);

        if (device == null) {
            Log.e("onCreate", "Dispositivo es nulo");
            finish(); // Cerrar la actividad si el dispositivo es nulo
            return;
        }

        etWifiSsid = findViewById(R.id.etWifiSsid);
        etWifiPass = findViewById(R.id.etWifiPass);
        etServerUrl = findViewById(R.id.etServerUrl);
        etSendInterval = findViewById(R.id.etSendInterval);

        try {
            TextView deviceName = findViewById(R.id.tvDeviceNameDisplay);
            TextView deviceAddr = findViewById(R.id.tvDeviceAddressDisplay);

            // Direccion y nombre del dispositvo Bluetooth
            deviceName.setText(device.getName());
            deviceAddr.setText(device.getAddress());

            // Servicio Bluetooth
            //Identificador unico y universal
            uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
            // Socket para la conexion utilizando el UUID
            socket = device.createRfcommSocketToServiceRecord(uuid);
            //Conexion
            socket.connect();
            //Flujo de entrada y salida
            input = socket.getInputStream();
            output = socket.getOutputStream();
            Toast.makeText(this, "Conectado!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "No se pudo realizar la conexión!", Toast.LENGTH_SHORT).show();
            Log.e("onCreate", "IOException al conectar: " + e.getMessage());
            finish(); // Cerrar la actividad si hay un error en la conexión
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (input != null) {
                input.close();
            }

            if (output != null) {
                output.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            Log.e("onDestroy", "IOException al cerrar: " + e.getMessage());
        }
    }
    //Info obtenidad de arduino
    public void arduinoRequestData(View v) {
        //Al darle click se envia cmd:2
        sendMessage("cmd:2");
        //Si la respuesta de arduino es vacia
        if (lastResponse == null || lastResponse.isEmpty()) {
            Toast.makeText(this, "Error de BT: Intente de nuevo.", Toast.LENGTH_SHORT).show();
            return;
        }
        //Se divide la respuesta
        for (String component : lastResponse.split(";")) {
            String[] pair = component.split(":", 2);
            String name = pair[0];
            String value = pair[1];

            switch (name) {
                case "ssid":
                    etWifiSsid.setText(value);
                    break;
                case "pass":
                    etWifiPass.setText(value);
                    break;
                case "url":
                    etServerUrl.setText(value);
                    break;
                case "interval":
                    etSendInterval.setText(value);
                    break;
            }
        }
        Toast.makeText(this, "Info actualizada", Toast.LENGTH_SHORT).show();
    }
    //Se envia 1 para prender el sensor y espera una respuesta
    public void arduinoStartup(View v) {
        sendMessageExpectingResponse("cmd:1");
    }
    //Se envia 0 para apagar el sensor y espera una respuesta
    public void arduinoShutdown(View v) {
        sendMessageExpectingResponse("cmd:0");
    }
    //Se envia el wifi que estara conectgado el arduino
    public void arduinoSaveWifiSsid(View v) {
        sendMessageExpectingResponse("ssid:" + etWifiSsid.getText().toString().trim());
    }
    //Se envia la contraseña del wifi
    public void arduinoSaveWifiPassword(View v) {
        sendMessageExpectingResponse("pass:" + etWifiPass.getText().toString().trim());
    }
    //Se guarda el servidor
    public void arduinoSaveServerUrl(View v) {
        String url = etServerUrl.getText().toString().trim();
        if (url.startsWith("https:")) {
            url = url.replace("https:", "http:");
        }
        sendMessageExpectingResponse("url:" + url);
    }
    /*public void arduinoSaveServerUrl(View v) {
        sendMessageExpectingResponse("url:" + etServerUrl.getText().toString().trim());
    }*/
    //Tiempo designado al monitore(segundos)
    public void arduinoSaveInterval(View v) {
        sendMessageExpectingResponse("interval:" + etSendInterval.getText().toString().trim());
    }
    //Envia el mensaje y Espera
    private void sendMessageExpectingResponse(String message) {
        sendMessage(message);//Envia el mensaje
        String response = lastResponse;

        if (response.isEmpty()) {
            response = "Se recibió una respuesta vacía";
        }

        Toast.makeText(this, response, Toast.LENGTH_SHORT).show();
    }

    private void sendMessage(String message) {
        if (output == null) {
            Toast.makeText(this, "No se a enviado nada", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            byte[] data = message.getBytes();//
            output.write(data);//envio
            output.flush();//limpio

            data = new byte[4096];
            int size = input.read(data);

            if (size == 0) {
                // try again
                input.read(data);
            }
            lastResponse = new String(data).trim();//Se convierte a string los bits
        } catch (IOException e) {
            Toast.makeText(this, "Falló el envío!", Toast.LENGTH_SHORT).show();
            Log.e("sendMessage", "IOException al enviar mensaje: " + e.getMessage());
        }
    }
}
