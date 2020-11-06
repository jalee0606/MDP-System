//TODO: update bluetooth scan UI

package com.example.testapp4;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import tech.jalee.gridview.NewBluetoothService;

import android.os.Bundle;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.IntentFilter;
import android.bluetooth.BluetoothDevice;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;


public class BluetoothScan extends AppCompatActivity {
    private static final String TAG = "MY_APP_DEBUG_TAG";
    private static final String NAME = "TestApp";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final static int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter bluetoothAdapter;
    private ListView listview;
    private ArrayAdapter<String> arrayAdapter;
    private TextView connectionStatus;
    public static String remoteClientAddress;
    public static BluetoothDevice remoteDevice;
    private TreeSet<String> deviceList;
    private TreeSet<String> bondedDeviceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_scan);

        listview = (ListView) findViewById(R.id.listView);
        connectionStatus = (TextView) findViewById(R.id.textView);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        deviceList = new TreeSet<String>();
        bondedDeviceList = new TreeSet<String>();
        //arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new ArrayList<String>(deviceList));
        listview.setAdapter(arrayAdapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = (String) parent.getItemAtPosition(position);
                //Toast.makeText(getApplicationContext(), selectedItem.split(" ")[0], Toast.LENGTH_SHORT).show();
                try {
                    String deviceMac = selectedItem.split(" ")[0];

                    BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceMac);
                    remoteDevice = device;
                    device.createBond();
                    //Toast.makeText(getApplicationContext(), selectedItem.split(" ")[0], Toast.LENGTH_SHORT).show();
                    remoteClientAddress = deviceMac;

                    if(!bondedDeviceList.contains(deviceMac)){
                        Toast.makeText(getApplicationContext(), "Connecting", Toast.LENGTH_SHORT).show();
                    }



                    Intent intent = new Intent();
                    setResult(MainActivity.BLUETOOTH_RESULT_OK, intent);
                    finish();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Failed to pair. Check connectivity", Toast.LENGTH_SHORT).show();
                }
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver, filter);


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0x1);
        }
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0x2);
//        }
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0x3);
//        }
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0x4);
//        }
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, 0x5);
//        }
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_ADMIN}, 0x6);
//        }
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_PRIVILEGED) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_PRIVILEGED}, 0x7);
//        }

        scan();
        //Toast.makeText(getApplicationContext(), "testttttttttttttttttttttttt", Toast.LENGTH_LONG).show();
    }


    public void onBtnScanClick(View view) {
        scan();
    }


    private void scan() {
        arrayAdapter.clear();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        bondedDeviceList.clear();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                //arrayAdapter.add(new StringBuilder(deviceHardwareAddress).append(" ").append(deviceName).toString());
                deviceList.add(new StringBuilder(deviceHardwareAddress).append(" ").append(deviceName).toString());
                bondedDeviceList.add(deviceHardwareAddress);
                //arrayAdapter.notifyDataSetChanged();
            }
            arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new ArrayList<String>(deviceList));
            arrayAdapter.notifyDataSetChanged();
            listview.setAdapter(arrayAdapter);
        }
        bluetoothAdapter.startDiscovery();
        if (bluetoothAdapter.isDiscovering())
            connectionStatus.setText("Scanning for device..");


    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                connectionStatus.setText("Scanning for device..");

                //arrayAdapter.add(new StringBuilder(deviceHardwareAddress).append(" ").append(deviceName).toString());
                deviceList.add(new StringBuilder(deviceHardwareAddress).append(" ").append(deviceName).toString());
                arrayAdapter = new ArrayAdapter<String>(BluetoothScan.this, android.R.layout.simple_list_item_1, new ArrayList<String>(deviceList));

                arrayAdapter.notifyDataSetChanged();


                listview.setAdapter(arrayAdapter);
            }else if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)){
                MainActivity.activeConnection = false;
                Toast.makeText(getApplicationContext(), "disconnected", Toast.LENGTH_SHORT).show();
                NewBluetoothService.getInstance(BluetoothScan.this).reconnect();
            }else if(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)){
                MainActivity.activeConnection = false;
                //Toast.makeText(getApplicationContext(), "disconnected", Toast.LENGTH_SHORT).show();
            }else if(BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)){
                MainActivity.activeConnection = true;
                //Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
            }else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                connectionStatus.setText("Click item to connect");
            }
        }
    };


    @Override
    protected void onStart() {
        super.onStart();

        if (bluetoothAdapter == null)
            Toast.makeText(this, "Device doesn't support bluetooth", Toast.LENGTH_SHORT).show();

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        Intent discoverableIntent =
                new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //unregisterReceiver(receiver);
    }


    public void onBtnChatClick(View view) {
        Intent intent = new Intent(this, Chat.class);
        startActivity(intent);
    }
}

