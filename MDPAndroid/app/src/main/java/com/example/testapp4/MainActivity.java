package com.example.testapp4;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.testapp4.bluetooth.CommandProcessor;
import com.example.testapp4.bluetooth.ICommandProcessor;

import java.util.Iterator;

import tech.jalee.gridview.NewBluetoothService;
import tech.jalee.gridview.ReplaySystem;
import tech.jalee.gridview.Stopwatch;
import tech.jalee.gridview.obj.Image;
import tech.jalee.gridview.obj.Map;
import tech.jalee.gridview.obj.MapHelper;
import tech.jalee.gridview.obj.Position;
import tech.jalee.gridview.view.BluetoothActivity;
import tech.jalee.gridview.view.MapViewLayout;


//public class MainActivity extends AppCompatActivity{
public class MainActivity extends AppCompatActivity implements MapViewLayout.MapViewInterface,SensorEventListener, ICommandProcessor {
    private ImageButton btnBluetooth;
    private ImageButton btnChat;
    private ImageButton btnF1;
    private ImageButton btnF2;
    private ImageButton btnConfig;
    private ImageButton btnUp;
    private ImageButton btnDown;
    private ImageButton btnLeft;
    private ImageButton btnRight;
    private ImageButton btnRotateLeft;
    private ImageButton btnRotateRight;
    private EditText tbX;
    private EditText tbY;
    private Button btnSetCoordinates;
    private TextView tvRobotStatus;
    private ToggleButton btnGyro;

    private Button mExploreBtn, mFastestPathBtn, mCalibrateBtn, mSensorBtn, mViewImageBtn, mSendWaypointBtn;

    private TextView mExplorationTiming, mFastestPathTiming, mRobotStatusTv, mRobotPositionTv, mRobotDirectionTv;

    private BluetoothQueue _queue;

    private LinearLayout mControlLayout;
    private LinearLayout mdfLayout;

    private Stopwatch mExploreTimer;
    private Stopwatch mFastestTimer;

    private TextView imageTv, mdfOneTv, mdfTwoTv;

    public NewBluetoothService bt;
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_FAIL_TO_CONNECT = 6;
    public static final String DEVICE_NAME = "name_of_device";

    private static final int EXPLORE_TIMER_CHANGE = 6;
    private static final int FASTEST_TIMER_CHANGE = 7;

    public static final int MAIN_ACITIVITY_RESULT_CODE = 1;
    public static final int BLUETOOTH_RESULT_OK = 20;

    public static final String FORWARD = "ARD:W1|"; //"f";
    public static final String STRAFE_LEFT = "ARD:A|W1|D|"; //"sl";
    public static final String STRAFE_RIGHT = "ARD:D|W1|A|"; //"sr";
    public static final String REVERSE = "ARD:S1|"; //"r";
    public static final String ROTATE_LEFT = "ARD:A|"; //"tl";
    public static final String ROTATE_RIGHT = "ARD:D|"; //"tr";
    public static final String BEGIN_EXPLORATION = "ALL:E|"; //beginExplore";
    public static final String BEGIN_FASTEST_PATH = "ALL:F|"; //"beginFastest";
    public static final String START_CALIBRATION = "ARD:D|D|L|A|A|";
    public static final String REQUEST_SENSOR = "ALL:Q|";
    public static final String SEND_ARENA_INFO = "sendArena";
    public static final String APP_PREFERENCE = "AppPreference";
    public static boolean activeConnection = false;

    public static String F1 = "F1";
    public static String F2 = "F2";
    private SharedPreferences sharedPreferences;
    private SensorManager sensorManager;
    private Sensor sensor;
    private SensorEventListener sensorEventListener;

    private CommandProcessor mParser;

    //2d map
    //code
    private MapViewLayout _mapLayout;
    private Map _map;
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bt = NewBluetoothService.getInstance(this);
        sharedPreferences = getSharedPreferences(MainActivity.APP_PREFERENCE, MODE_PRIVATE);
        btnBluetooth = (ImageButton) findViewById(R.id.btnBluetooth);
        btnChat = (ImageButton) findViewById(R.id.btnChat);
        btnF1 = (ImageButton) findViewById(R.id.btnF1);
        btnF2 = (ImageButton) findViewById(R.id.btnF2);
        btnConfig = (ImageButton) findViewById(R.id.btnConfig);
        btnUp = (ImageButton) findViewById(R.id.btnUp);
        btnDown = (ImageButton) findViewById(R.id.btnDown);
        btnLeft = (ImageButton) findViewById(R.id.btnLeft);
        btnRight = (ImageButton) findViewById(R.id.btnRight);
        btnRotateLeft = (ImageButton) findViewById(R.id.btnRotateLeft);
        btnRotateRight = (ImageButton) findViewById(R.id.btnRotateRight);
        tbX = (EditText) findViewById(R.id.tbX);
        tbY = (EditText) findViewById(R.id.tbY);
        btnSetCoordinates = (Button) findViewById(R.id.btnSetCoordinates);
        tvRobotStatus = (TextView) findViewById(R.id.tvRobotStatus);
        btnGyro = (ToggleButton) findViewById(R.id.btnGyro);

        mControlLayout = (LinearLayout) findViewById(R.id.controlScreen);
        mdfLayout = (LinearLayout) findViewById(R.id.screenshotScreen);

        mExploreTimer = new Stopwatch(timerHandler, EXPLORE_TIMER_CHANGE);
        mFastestTimer = new Stopwatch(timerHandler, FASTEST_TIMER_CHANGE);

        imageTv = (TextView) findViewById(R.id.imageValues);
        mdfOneTv = (TextView) findViewById(R.id.p1Value);
        mdfTwoTv = (TextView) findViewById(R.id.p2Value);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        mRobotStatusTv = (TextView) findViewById(R.id.robotStatusTextView);
        mRobotDirectionTv = (TextView) findViewById(R.id.robotDirectionTextView);
        mRobotPositionTv = (TextView) findViewById(R.id.robotCoordinateTextView);
        mExplorationTiming = (TextView) findViewById(R.id.explorationTiming);
        mFastestPathTiming = (TextView) findViewById(R.id.fastestTiming);

        mExploreBtn = (Button) findViewById(R.id.explorationBtn);
        mFastestPathBtn = (Button) findViewById(R.id.fastestBtn);

        mSendWaypointBtn = (Button) findViewById(R.id.sendWaypoint);

        mCalibrateBtn = (Button) findViewById(R.id.calibrateBtn);
        mSensorBtn = (Button) findViewById(R.id.requestSensorBtn);

        F1 = sharedPreferences.getString("F1", "F1");
        F2 = sharedPreferences.getString("F2", "F2");

        //2d map
        //code
        _mapLayout = (MapViewLayout) findViewById(R.id.maplayout);
        _mapLayout.setInterface(this);

        mParser = new CommandProcessor();
        //

        _queue = new BluetoothQueue(uiHandler);

        // ReplaySystem.getInstance().addReplayHandler(timerHandler);

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

        if(sensor == null)
            btnGyro.setEnabled(false);

//        sensorEventListener = new SensorEventListener() {
//            @Override
//            public void onSensorChanged(SensorEvent sensorEvent) {
//                StringBuilder testString = new StringBuilder();
//                float axisX = sensorEvent.values[0];
//                float axisY = sensorEvent.values[1];
//                float axisZ = sensorEvent.values[2];
//
//                testString.append("X: ").append(axisX)
//                        .append("Y: ").append(axisY)
//                        .append("Z: ").append(axisZ);
//
//                Toast.makeText(MainActivity.this, testString.toString(), Toast.LENGTH_LONG).show();
//            }
//
//            @Override
//            public void onAccuracyChanged(Sensor sensor, int i) {
//
//            }
//        };

        btnBluetooth.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BluetoothActivity.class);
                startActivity(intent);
                //startActivityForResult(intent, MAIN_ACITIVITY_RESULT_CODE);
            }
        });

        mExploreBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Button button = (Button)v;
                if(bt != null && bt.isActiveConnection()){
                    mExploreTimer.start();
                    if(mExploreTimer.IsStarted())
                    {
                        bt.sendMessage(BEGIN_EXPLORATION.getBytes());
                        mExploreBtn.setText("Stop");
                        mFastestPathBtn.setEnabled(false);
                        setRobotStatus("EXPLORATION");
                        _queue.start();
                    }
                    else
                    {

                        mExploreBtn.setText("Start");
                        mFastestPathBtn.setEnabled(true);
                        setRobotStatus("IDLE");
                    }
                }
            }
        });

        mFastestPathBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Position waypoint = Map.getInstance().getWaypoint();
                if(waypoint == null)
                {
                    Toast.makeText(MainActivity.this, "Please set a waypoint first.", Toast.LENGTH_LONG).show();
                    return;
                }
                if(bt != null && bt.isActiveConnection()){
                    mFastestTimer.start();
                    if(mFastestTimer.IsStarted()) {
                        bt.writeFastestPath();
                        mFastestPathBtn.setText("Stop");
                        mExploreBtn.setEnabled(false);
                        setRobotStatus("FASTEST PATH");
                    } else {
                        mFastestPathBtn.setText("Start");
                        mExploreBtn.setEnabled(true);
                        setRobotStatus("IDLE");
                    }
                }
            }
        });

        mCalibrateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if(bt != null && bt.isActiveConnection()){
                    bt.sendMessage(START_CALIBRATION.getBytes());
                    //tvRobotStatus.setText("EXPLORATION");
                }
            }
        });

        mSensorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if(bt != null && bt.isActiveConnection()){
                    bt.sendMessage(REQUEST_SENSOR.getBytes());
                    //tvRobotStatus.setText("EXPLORATION");
                }
            }
        });

        btnChat.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (bt != null && bt.isActiveConnection()) {
                    Intent intent = new Intent(MainActivity.this, Chat.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "no active bluetooth device connected", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnF1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(bt != null && bt.isActiveConnection()){
                    bt.sendMessage(F1.getBytes());
                }
            }
        });

        btnF2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(bt != null && bt.isActiveConnection()){
                    bt.sendMessage(F2.getBytes());
                }
            }
        });

        btnConfig.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Config config = new Config();
                config.show(getSupportFragmentManager(), "Config");
            }
        });






        btnUp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (bt != null && bt.isActiveConnection()) {
                    bt.sendMessage(FORWARD.getBytes());
                    _mapLayout.moveRobot(FORWARD);
                }
            }
        });

        btnDown.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(bt != null && bt.isActiveConnection()){
                    bt.sendMessage(REVERSE.getBytes());
                    _mapLayout.moveRobot(REVERSE);
                }
            }
        });

        btnLeft.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(bt != null && bt.isActiveConnection()){
                    bt.sendMessage(STRAFE_LEFT.getBytes());
                    _mapLayout.moveRobot(STRAFE_LEFT);
                }
            }
        });

        btnRight.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(bt != null && bt.isActiveConnection()){
                    bt.sendMessage(STRAFE_RIGHT.getBytes());
                    _mapLayout.moveRobot(STRAFE_RIGHT);
                }
            }
        });

        btnRotateLeft.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(bt != null && bt.isActiveConnection()){
                    bt.sendMessage(ROTATE_LEFT.getBytes());
                }
            }
        });

        btnRotateRight.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(bt != null && bt.isActiveConnection()){
                    bt.sendMessage(ROTATE_RIGHT.getBytes());
                }
            }
        });

        btnSetCoordinates.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(bt != null && bt.isActiveConnection()) {
                    String x = tbX.getText().toString();
                    String y = tbY.getText().toString();

                    if (x.length() > 0 && y.length() > 0) {
                        // _mapLayout.updateRobot(Integer.parseInt(x), Integer.parseInt(y));
                        bt.sendMessage(new StringBuilder("coordinate(").append(x).append(",").append(y).append(")").toString().getBytes());
                    }
                }
            }
        });

        mSendWaypointBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if(bt != null && bt.isActiveConnection()) {
                    Position pos = Map.getInstance().getWaypoint();
                    if(pos != null)
                        bt.sendMessage(String.format("ALG:W|%d|%d", pos.getX(), pos.getY()).getBytes());
                }
            }
        });


    }
/*
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.menu_bluetooth:
                Intent i = new Intent(MainActivity.this, BluetoothActivity.class);
                startActivity(i);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
*/
    @Override
    protected void onResume() {
        super.onResume();
        mParser.addObserver(this);
        if(bt != null)
            bt.addObserver(mHandler);
        _map = Map.getInstance();
        _map.setActivity(this);
        _map.initialize();
        _mapLayout.resetRobot();
    }

    @Override
    protected void onPause() {
        mParser.removeObserver(this);
        if(bt != null)
            bt.removeObserver(mHandler);
        super.onPause();
    }

    private final Handler uiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg)
        {
            switch(msg.what) {
                case MESSAGE_READ:
                    String readMessage = "";
                    try {
                        byte[] readBuf = (byte[]) msg.obj;
                        // construct a string from the valid bytes in the buffer
                        readMessage = new String(readBuf, 0, msg.arg1);
                    }catch(Exception e){

                    }
                    mParser.parse(readMessage);
                    break;
            }
        }
    };

    private final Handler timerHandler = new Handler() {
        @Override
        public void handleMessage(Message msg)
        {
            switch(msg.what) {
                case EXPLORE_TIMER_CHANGE:
                    mExplorationTiming.setText(mExploreTimer.elapsed());
                    break;
                case FASTEST_TIMER_CHANGE:
                    mFastestPathTiming.setText(mFastestTimer.elapsed());
                    break;
            }
        }
    };

    private void setRobotStatus(String text)
    {
        mRobotStatusTv.setText(text);
        tvRobotStatus.setText(text);
    }

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    System.out.println(MESSAGE_STATE_CHANGE);
                    switch(msg.arg1) {
                        case 0:
                            // Attempt to reconnect (Thursday 12:06)
                            if(bt != null)
                                bt.reconnect();
                            Toast.makeText(MainActivity.this, "None", Toast.LENGTH_SHORT).show();
                            break;
                        case 1:
                            Toast.makeText(MainActivity.this, "Connecting", Toast.LENGTH_SHORT).show();
                            break;
                        case 2:
                            Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
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
                    String readMessage="";

                    try {
                        byte[] readBuf = (byte[]) msg.obj;
                        // construct a string from the valid bytes in the buffer
                        readMessage = new String(readBuf, 0, msg.arg1);

                        Chat.tbReceive.append(readMessage);
                        Chat.tbReceive.append("\n");
                    }catch(Exception e){

                    }
                    _queue.add(readMessage);
                    break;
                case MESSAGE_DEVICE_NAME:
                    String deviceName = msg.getData().getString(MainActivity.DEVICE_NAME);
                    Toast.makeText(MainActivity.this, String.format("Connected to %s", deviceName), Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    String toastMsg = msg.getData().getString("toast");
                    Toast.makeText(MainActivity.this, toastMsg, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    //2d map
    //code
    /// Handle the state of GridMap by CustomView
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d("MainActivity", "onConfigChange()");
    }

    @Override
    public void onGenerateMDF(String mdfPartOne, String mdfPartTwo) {
        Log.d("MDF String 1", mdfPartOne);
        Log.d("MDF String 2", mdfPartTwo);
        mdfOneTv.setText(mdfPartOne);
        mdfTwoTv.setText(mdfPartTwo);
        Iterator<Image> images = Map.getInstance().getImageBlocks();
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        while(images.hasNext())
        {
            Image image = images.next();
            sb.append(image.toString());
            if(images.hasNext())
            {
                sb.append(",");
            }
        }
        sb.append("}");
        imageTv.setText(sb.toString());
    }

    @Override
    public void onWaypointCreated(Position pos) {
        // Waypoint(x,y)
        StringBuilder sb = new StringBuilder("Waypoint").append(pos.toString());
        Log.d("MainActivity", sb.toString());
        //bt.sendMessage(String.format("ALG:W|%d|%d", pos.getX(), pos.getY()).getBytes());
        //bt.sendMessage("ALL:F|".getBytes());
        //bt.sendMessage(("ALG:"+String.valueOf(pos.getX())).getBytes());
        //bt.sendMessage(("ALG:"+String.valueOf(pos.getY())).getBytes());
    }

    @Override
    public void onResultScreen() {
        this.toggleBottomControl();
    }

    private void toggleBottomControl()
    {
        if(mControlLayout.getVisibility() == View.VISIBLE) {
            mControlLayout.setVisibility(View.GONE);
            mdfLayout.setVisibility(View.VISIBLE);
        } else {
            mControlLayout.setVisibility(View.VISIBLE);
            mdfLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        StringBuilder testString = new StringBuilder();
        float axisX = event.values[0];
        float axisY = event.values[1];
        float axisZ = event.values[2];

//        testString.append("X: ").append(axisX).append("\n")
//                .append("Y: ").append(axisY).append("\n")
//                .append("Z: ").append(axisZ);


        if(btnGyro.isChecked()) {
            if (axisX > 2.5)
                btnLeft.performClick();
            else if (axisX < -2.5)
                btnRight.performClick();
            else if (axisY < -2.5)
                btnUp.performClick();
            else if (axisY > 2.5)
                btnDown.performClick();
        }

        //Toast.makeText(MainActivity.this, testString.toString(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onCommandReceived(String type, String data) {
        Log.d("MainActivity", String.format("onCommand(%s, %s)", type, data));
        try {
            if (type.equals("coordinate")) {

                String[] coordinate = data.split(",");
                mRobotPositionTv.setText(coordinate[1] + "," + coordinate[0]);
                String direction = String.valueOf(coordinate[2].charAt(0)).toUpperCase();
                mRobotDirectionTv.setText(direction + coordinate[2].substring(1));
                String fullDirection = coordinate[2];
                if(fullDirection.length() > 4 && (fullDirection.contains("east")||fullDirection.contains("west")))
                {
                    String mapdata = fullDirection.substring(4);
                    ReplaySystem.getInstance().putMap(mapdata);
                    _mapLayout.updateMapWithCustomFormat(mapdata);
                }
                else if(fullDirection.length() > 5 && (fullDirection.contains("north")||fullDirection.contains("south")))
                {
                    String mapdata = fullDirection.substring(5);
                    ReplaySystem.getInstance().putMap(mapdata);
                    _mapLayout.updateMapWithCustomFormat(mapdata);
                }
                int robotPosX = Integer.parseInt(coordinate[0]) + 1; // offset the grid
                int robotPosY = Integer.parseInt(coordinate[1]) + 1; // offset the grid
                _mapLayout.updateRobot(robotPosX, robotPosY, direction);
                // x,y,direction
                ReplaySystem.getInstance().putRobot(data);
            } else if (type.equals("images")) {
                //(x,y,id)
                _mapLayout.addImage(data);
            } else if (type.equals("status")) {
                if (data.equals("end")) {
                    if (mExploreTimer.IsStarted()) {
                        mExploreBtn.performClick();
                        _queue.stop();
                    }
                    if (mFastestTimer.IsStarted())
                        mFastestPathBtn.performClick();
                    onGenerateMDF(MapHelper.GridToMDF1(Map.getInstance().getGrid()), MapHelper.GridToMDF2(Map.getInstance().getGrid()));
                }
            } else if (type.equals("map")) {
                ReplaySystem.getInstance().putMap(data);
                _mapLayout.updateMapWithCustomFormat(data);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    //
}