package com.example.configuracionarduino;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import java.util.ArrayList;
public class MainActivity extends AppCompatActivity {
    //Para identificar un disposito Bluetooth
    public static final String EXTRA_DEVICE_SELECTED = "extra_bt_selected";
    private Button btnBluetooth;
    private ListView lvDeviceList;
    private ArrayAdapter<BluetoothDevice> listAdapter;
    private BluetoothAdapter bluetoothAdapter;
    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Lista de permisos relacionados con Bluetooth
        String[] permissions = new String[]{
                "android.permission.BLUETOOTH",
                "android.permission.BLUETOOTH_ADMIN",
                "android.permission.BLUETOOTH_SCAN",
                "android.permission.BLUETOOTH_CONNECT",
                "android.permission.ACCESS_COARSE_LOCATION",
                "android.permission.ACCESS_FINE_LOCATION",
        };
        ActivityCompat.requestPermissions(this, permissions, 100);

        btnBluetooth = findViewById(R.id.btnBeginBluetoothScan);
        lvDeviceList = findViewById(R.id.lvBluetoothDeviceList);

        listAdapter = new BluetoothDeviceArrayAdapter(this, new ArrayList<>());
        lvDeviceList.setAdapter(listAdapter);

        BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = manager.getAdapter();


        if (!bluetoothAdapter.isEnabled()) {
            btnBluetooth.setText(R.string.bluetooth_request_enable);
            btnBluetooth.setEnabled(false);
        }

        // Inicializar el receptor de difusión para eventos Bluetooth
        initializeBroadcastReceiver();

        // Configurar la acción al hacer clic en un elemento de la lista de dispositivos Bluetooth
        lvDeviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MainActivity.this, BluetoothCommunicationActivity.class);
                Bundle bundle = new Bundle();

                // Agregar el dispositivo Bluetooth seleccionado al Bundle
                bundle.putParcelable(EXTRA_DEVICE_SELECTED, listAdapter.getItem(i));
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @SuppressLint("MissingPermission")
    public void bluetoothStartDiscoveryOnClick(View v) {
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }

        listAdapter.clear();
        listAdapter.notifyDataSetChanged();

        bluetoothAdapter.startDiscovery();
    }

    // Método para inicializar el receptor de difusión para eventos Bluetooth
    private void initializeBroadcastReceiver() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action == null) {
                    return;
                }
                switch (action) {
                    case BluetoothAdapter.ACTION_STATE_CHANGED:
                        handleBluetoothStateChanged(context, intent);
                        break;
                    case BluetoothDevice.ACTION_FOUND:
                        handleBluetoothDeviceFound(context, intent);
                        break;
                    case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                        handleBluetoothDiscoveryStarted(context, intent);
                        break;
                    case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                        handleBluetoothDiscoveryFinished(context, intent);
                        break;
                }
            }
        };

        // Registrar el receptor para eventos Bluetooth
        registerReceiver(receiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        registerReceiver(receiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
        registerReceiver(receiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
    }

    // Manejar el cambio de estado de Bluetooth
    private void handleBluetoothStateChanged(Context context, Intent intent) {
        int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

        switch (state) {
            case BluetoothAdapter.STATE_ON:
                btnBluetooth.setText(R.string.bluetooth_begin_scan);
                btnBluetooth.setEnabled(true);
                break;
            case BluetoothAdapter.STATE_OFF:
                btnBluetooth.setText(R.string.bluetooth_request_enable);
                btnBluetooth.setEnabled(false);
                break;
        }
    }

    @SuppressLint("MissingPermission")
    private void handleBluetoothDeviceFound(Context context, Intent intent) {
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        if (device.getName() != null && device.getName().contains("Sensor") || true) {
            listAdapter.add(device);
            listAdapter.notifyDataSetChanged();
        }
    }

    private void handleBluetoothDiscoveryStarted(Context context, Intent intent) {
        Toast.makeText(this, "Búsqueda iniciada", Toast.LENGTH_SHORT).show();
    }

    private void handleBluetoothDiscoveryFinished(Context context, Intent intent) {
        Toast.makeText(this, "Búsqueda finalizada", Toast.LENGTH_SHORT).show();
    }
}
