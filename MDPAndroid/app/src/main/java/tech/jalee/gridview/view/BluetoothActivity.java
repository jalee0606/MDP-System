package tech.jalee.gridview.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import tech.jalee.gridview.NewBluetoothService;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.testapp4.MainActivity;
import com.example.testapp4.R;

import java.util.ArrayList;
import java.util.Set;

import static com.example.testapp4.MainActivity.MESSAGE_DEVICE_NAME;
import static com.example.testapp4.MainActivity.MESSAGE_FAIL_TO_CONNECT;
import static com.example.testapp4.MainActivity.MESSAGE_READ;
import static com.example.testapp4.MainActivity.MESSAGE_STATE_CHANGE;
import static com.example.testapp4.MainActivity.MESSAGE_TOAST;
import static com.example.testapp4.MainActivity.MESSAGE_WRITE;
import static com.example.testapp4.MainActivity.bt;

public class BluetoothActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, AdapterView.OnItemClickListener {

    private ListView mBluetoothListView;
    private BluetoothAdapter mBluetoothAdapter;
    private Switch mBluetoothSwitch;
    private Button mConnectBtn, mDisconnectBtn, mScanBtn;
    private TextView mStatusTv;
    private int REQUEST_ENABLE_BT = 1;

    private int mPreviousSelectionId = -1;

    private ArrayAdapter<String> mAdapter;

    private ArrayList<String> deviceList;

    private Set<BluetoothDevice> mBondedList;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                mStatusTv.setText("Scanning");
                if(!mBondedList.contains(device))
                {
                    mAdapter.add(new StringBuilder(deviceHardwareAddress).append(" ").append(deviceName).toString());
                }
                //arrayAdapter.add(new StringBuilder(deviceHardwareAddress).append(" ").append(deviceName).toString());
                /*
                arrayAdapter = new ArrayAdapter<String>(BluetoothScan.this, android.R.layout.simple_list_item_1, new ArrayList<String>(deviceList));

                arrayAdapter.notifyDataSetChanged();


                listview.setAdapter(arrayAdapter);
                 */
                //mAdapter.notifyDataSetChanged();
            }else if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)){
                MainActivity.activeConnection = false;
                Toast.makeText(getApplicationContext(), "disconnected", Toast.LENGTH_SHORT).show();
                bt.reconnect();
            }else if(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)){
                MainActivity.activeConnection = false;
                //Toast.makeText(getApplicationContext(), "disconnected", Toast.LENGTH_SHORT).show();
            }else if(BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)){
                MainActivity.activeConnection = true;
                //Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
                // finish();
            }else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                mStatusTv.setText(R.string.select_device);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        mBluetoothListView = (ListView) findViewById(R.id.bluetoothList);
        mBluetoothListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mBluetoothListView.setOnItemClickListener(this);
        mBluetoothSwitch = (Switch) findViewById(R.id.bluetoothSwitch);
        mConnectBtn = (Button) findViewById(R.id.connectBtn);
        mDisconnectBtn = (Button) findViewById(R.id.disconnectBtn);
        mScanBtn = (Button) findViewById(R.id.scanBtn);
        mStatusTv = (TextView) findViewById(R.id.statusTextView);
        mConnectBtn.setOnClickListener(this);
        mDisconnectBtn.setOnClickListener(this);
        mScanBtn.setOnClickListener(this);
        deviceList = new ArrayList<String>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0x1);
        }
    }

    protected void onStart()
    {
        super.onStart();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null)
            return;
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        Intent discoverableIntent =
                new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
    }

    protected void onResume()
    {
        super.onResume();
        mBluetoothSwitch.setChecked(mBluetoothAdapter.isEnabled());
        mBluetoothSwitch.setOnCheckedChangeListener(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver, filter);
        NewBluetoothService.getInstance(this).addObserver(mHandler);
        if(NewBluetoothService.getInstance(this).getState() == NewBluetoothService.STATE_CONNECTED)
        {
            mStatusTv.setText(R.string.connected);
            enable(true);
        }
        else
        {
            enable(false);
        }
    }

    private void enable(boolean isConnected)
    {
        if(isConnected)
        {
            mConnectBtn.setEnabled(false);
            mDisconnectBtn.setEnabled(true);
        }
        else
        {
            mConnectBtn.setEnabled(true);
            mDisconnectBtn.setEnabled(false);
        }
    }

    private void scan()
    {
        deviceList.clear();
        mBondedList = mBluetoothAdapter.getBondedDevices();
        if (mBondedList.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : mBondedList) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                //arrayAdapter.add(new StringBuilder(deviceHardwareAddress).append(" ").append(deviceName).toString());
                deviceList.add(new StringBuilder(deviceHardwareAddress).append(" ").append(deviceName).toString());
                //bondedDeviceList.add(deviceHardwareAddress);
                //arrayAdapter.notifyDataSetChanged();
            }
            mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new ArrayList<String>(deviceList));
            //mAdapter.notifyDataSetChanged();
            mBluetoothListView.setAdapter(mAdapter);
        }
        mBluetoothAdapter.startDiscovery();
        if (mBluetoothAdapter.isDiscovering())
            mStatusTv.setText("Scanning");
    }

    protected void onPause()
    {
        // unregisterReceiver(receiver);
        NewBluetoothService.getInstance(this).removeObserver(mHandler);
        mBluetoothSwitch.setOnCheckedChangeListener(null);
        super.onPause();
    }


    @SuppressLint("NewApi")
    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.connectBtn:
                if(mPreviousSelectionId != -1) {
                    String selectedItem = mBluetoothListView.getItemAtPosition(mPreviousSelectionId).toString();
                    try {
                        String deviceMac = selectedItem.split(" ")[0];

                        BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceMac);
                        NewBluetoothService.getInstance(this).set(device);
                        device.createBond();

                        if(!mBondedList.contains(device)){
                            Toast.makeText(getApplicationContext(), "Connecting", Toast.LENGTH_SHORT).show();
                        }

                        NewBluetoothService.getInstance(this).connect();
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "Failed to pair. Check connectivity", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.disconnectBtn:
                NewBluetoothService.getInstance(this).stop();
                break;
            case R.id.scanBtn:
                scan();
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if(b)
        {
            if(!mBluetoothAdapter.isEnabled())
                mBluetoothAdapter.enable();
        }
        else
        {
            if(mBluetoothAdapter.isEnabled())
                mBluetoothAdapter.disable();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if(mPreviousSelectionId != -1) {
            mBluetoothListView.setItemChecked(mPreviousSelectionId, false);
        }
        mPreviousSelectionId = i;
        mBluetoothListView.setItemChecked(mPreviousSelectionId, true);
        Log.d("BluetoothActivity", String.format("Selection: %s", mBluetoothListView.getItemAtPosition(mPreviousSelectionId)));
    }

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch(msg.arg1) {
                        case 0:
                            // don't do anything first
                            break;
                        case 1:
                            mStatusTv.setText("Connecting");
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    try {
                        byte[] writeBuf = (byte[]) msg.obj;
                        // construct a string from the buffer
                        String writeMessage = new String(writeBuf);
                    }catch(Exception e){

                    }
                    break;
                case MESSAGE_READ:
                    break;
                case MESSAGE_DEVICE_NAME:
                    String deviceName = msg.getData().getString(MainActivity.DEVICE_NAME);
                    enable(true);
                    mStatusTv.setText(String.format("Connected", deviceName));
                    break;
                case MESSAGE_TOAST:
                    String toastMsg = msg.getData().getString("toast");
                    Toast.makeText(BluetoothActivity.this, toastMsg, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_FAIL_TO_CONNECT:
                    mStatusTv.setText("Connection Failed");
                    break;
            }
        }
    };
}