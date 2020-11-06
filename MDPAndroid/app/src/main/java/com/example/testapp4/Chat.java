package com.example.testapp4;

import androidx.appcompat.app.AppCompatActivity;
import tech.jalee.gridview.NewBluetoothService;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


public class Chat extends AppCompatActivity {
    private static NewBluetoothService bt;
    private EditText tbSend;
    public static EditText tbReceive;
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        //bt = new MyBluetoothService(this, mHandler);
        bt = NewBluetoothService.getInstance(this);
        Toast.makeText(getApplicationContext(), String.format("Connected to %s", bt.deviceName), Toast.LENGTH_LONG).show();

        tbSend = (EditText) findViewById(R.id.tbSend);
        tbReceive = (EditText) findViewById(R.id.tbReceive);

        //bt.connect(BluetoothScan.remoteDevice);
        //bt.receive();
    }



    public void onBtnSendClick(View view) {
        bt.sendMessage(tbSend.getText().toString().getBytes());
        tbSend.getText().clear();
    }


}