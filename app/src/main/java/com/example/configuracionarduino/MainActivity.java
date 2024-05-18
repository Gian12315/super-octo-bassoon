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
    private ArrayAdapter<BluetoothDevice> listAdapter;
    private BluetoothAdapter bluetoothAdapter;
    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Solicitar permisos relacionados con Bluetooth
        requestBluetoothPermissions();

        // Buscar y asignar vistas a las variables
        btnBluetooth = findViewById(R.id.btnBeginBluetoothScan);
        ListView lvDeviceList = findViewById(R.id.lvBluetoothDeviceList);

        // Crear un adaptador para la lista de dispositivos Bluetooth
        listAdapter = new BluetoothDeviceArrayAdapter(this, new ArrayList<>());
        lvDeviceList.setAdapter(listAdapter);

        // Obtener el adaptador Bluetooth del sistema
        BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = manager.getAdapter();

        // Verificar si el dispositivo soporta Bluetooth
        if (bluetoothAdapter == null) {
            btnBluetooth.setText(R.string.bluetooth_unsupported);
            btnBluetooth.setEnabled(false);
            return;
        }

        // Verificar si Bluetooth está desactivado
        if (!bluetoothAdapter.isEnabled()) {
            btnBluetooth.setText(R.string.bluetooth_request_enable);
            btnBluetooth.setEnabled(false);
        }

        // Inicializar el receptor de difusión para eventos Bluetooth
        initializeBroadcastReceiver();

        // Configurar la acción al hacer clic en un elemento de la lista de dispositivos Bluetooth
        lvDeviceList.setOnItemClickListener((adapterView, view, i, l) -> {
            // Crear un Intent para iniciar la actividad de comunicación Bluetooth
            Intent intent = new Intent(this, BluetoothCommunicationActivity.class);
            Bundle bundle = new Bundle();

            // Agregar el dispositivo Bluetooth seleccionado al Bundle
            bundle.putParcelable(EXTRA_DEVICE_SELECTED, listAdapter.getItem(i));
            intent.putExtras(bundle);
            startActivity(intent);
        });
    }

    // Método llamado cuando la actividad está siendo destruida
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Desregistrar el receptor de difusión para evitar pérdidas de memoria
        unregisterReceiver(receiver);
    }

    // Método llamado al hacer clic en el botón de inicio de escaneo Bluetooth
    @SuppressLint("MissingPermission")
    public void bluetoothStartDiscoveryOnClick(View v) {
        // Cancelar el descubrimiento Bluetooth si ya está en curso
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }

        // Limpiar la lista de dispositivos y notificar al adaptador del cambio
        listAdapter.clear();
        listAdapter.notifyDataSetChanged();

        // Iniciar el descubrimiento Bluetooth
        bluetoothAdapter.startDiscovery();
    }

    // Método para solicitar permisos relacionados con Bluetooth
    private void requestBluetoothPermissions() {
        // Lista de permisos relacionados con Bluetooth
        String[] permissions = new String[]{
                "android.permission.BLUETOOTH",
                "android.permission.BLUETOOTH_ADMIN",
                "android.permission.BLUETOOTH_SCAN",
                "android.permission.BLUETOOTH_CONNECT",
                "android.permission.ACCESS_COARSE_LOCATION",
                "android.permission.ACCESS_FINE_LOCATION",
        };

        // Lista de permisos que aún no han sido concedidos
        ArrayList<String> required = new ArrayList<>(permissions.length);

        // Verificar si cada permiso ha sido concedido
        for (String name : permissions) {
            if (ActivityCompat.checkSelfPermission(this, name) != PackageManager.PERMISSION_GRANTED) {
                required.add(name);
            }
        }

        // Solicitar permisos si es necesario
        if (required.size() > 0) {
            String[] requiredPermissions = new String[required.size()];
            required.toArray(requiredPermissions);

            ActivityCompat.requestPermissions(this, requiredPermissions, 100);
        }
    }

    // Método para inicializar el receptor de difusión para eventos Bluetooth
    private void initializeBroadcastReceiver() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Obtener la acción de la intención
                String action = intent.getAction();
                if (action == null) {
                    return;
                }

                // Realizar acciones según la acción de la intención
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
        // Obtener el nuevo estado de Bluetooth
        int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

        // Realizar acciones según el nuevo estado
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

    // Manejar la detección de un dispositivo Bluetooth
    private void handleBluetoothDeviceFound(Context context, Intent intent) {
        // Obtener el dispositivo Bluetooth descubierto
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        // Agregar el dispositivo a la lista y notificar al adaptador del cambio
        listAdapter.add(device);
        listAdapter.notifyDataSetChanged();
    }

    // Manejar el inicio del descubrimiento Bluetooth
    private void handleBluetoothDiscoveryStarted(Context context, Intent intent) {
        // Mostrar un mensaje indicando que el descubrimiento ha comenzado
        Toast.makeText(this, "Iniciando búsqueda", Toast.LENGTH_SHORT).show();
    }

    // Manejar el final del descubrimiento Bluetooth
    private void handleBluetoothDiscoveryFinished(Context context, Intent intent) {
        // Mostrar un mensaje indicando que el descubrimiento ha finalizado
        Toast.makeText(this, "Búsqueda finalizada", Toast.LENGTH_SHORT).show();
    }
}
