package tech.jalee.gridview.view;

import androidx.appcompat.app.AppCompatActivity;
import tech.jalee.gridview.ReplaySystem;
import tech.jalee.gridview.obj.Map;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.testapp4.R;

public class ReplayActivity extends AppCompatActivity implements View.OnClickListener, ILoader {

    private MDPMapView mMapView;

    private Button mPlayBtn, mStopBtn, mLoadBtn, mSaveBtn;

    private EditText mFileNameEditText;

    private boolean hasPerformBackup = false;

    private Loader mLoader;

    private String value;

    private Handler mReplayHandler = new Handler() {
        @Override
        public void handleMessage(Message msg)
        {
            switch(msg.what) {
                case REPLAY_PLAY:
                    Bundle bundle = msg.getData();
                    String map = bundle.getString("map");
                    String[] data = map.split(":");
                    if(data[0].equals("robot")) {
                        String[] coordinate = data[1].split(",");
                        int robotPosX = Integer.parseInt(coordinate[0]) + 1; // offset the grid
                        int robotPosY = Integer.parseInt(coordinate[1]) + 1; // offset the grid
                        String direction = String.valueOf(coordinate[2].charAt(0)).toUpperCase();
                        mMapView.updateRobotPos(robotPosX, robotPosY, direction);
                    } else {
                        mMapView.updateMapWithCustomFormat(data[1]);
                    }
                    break;
                case REPLAY_PAUSE:
                    break;
                case REPLAY_STOP:
                    // Save and stop
                    break;

            }
        }
    };

    public static final int REPLAY_PLAY = 0;
    public static final int REPLAY_PAUSE = 1;
    public static final int REPLAY_STOP = 2;

    private static final int LOAD_REPLAY_FILE = 0;
    private static final int SAVE_REPLAY_FILE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.replay_activity);
        ReplaySystem.getInstance().addReplayHandler(mReplayHandler);
        mMapView = (MDPMapView) findViewById(R.id.replaymap);
        mPlayBtn = (Button) findViewById(R.id.playBtn);
        mStopBtn = (Button) findViewById(R.id.stopBtn);
        mLoadBtn = (Button) findViewById(R.id.loadBtn);
        mSaveBtn = (Button) findViewById(R.id.saveBtn);
        mFileNameEditText = (EditText) findViewById(R.id.fileEditText);
        mPlayBtn.setOnClickListener(this);
        mStopBtn.setOnClickListener(this);
        mSaveBtn.setOnClickListener(this);
        mLoadBtn.setOnClickListener(this);
        mLoader = new Loader();
        mLoader.setLoader(this, this);
        setTitle(getResources().getString(R.string.replay_activity_title));
        Map.getInstance().setActivity(this);
    }

    protected void onPause()
    {
        // Map.getInstance().initialize();
        Log.d("ReplayActivity", "onPause() called");
        super.onPause();
    }

    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.playBtn:
                if(!hasPerformBackup) {
                    hasPerformBackup = true;
                    Map.getInstance().backup();
                }
                ReplaySystem.getInstance().start();
                break;
            case R.id.stopBtn:
                ReplaySystem.getInstance().stop();
                break;
            case R.id.loadBtn:
                mLoader.loadingScreen(LOAD_REPLAY_FILE, false);
                break;
            case R.id.saveBtn:
                mLoader.loadingScreen(SAVE_REPLAY_FILE,false);
                break;
        }
    }

    private String getFileName()
    {
        return String.format("%s.txt", mFileNameEditText.getText().toString());
    }

    @Override
    public void makeRequest(int request_type) throws Exception {
        switch(request_type)
        {
            case SAVE_REPLAY_FILE:
                value = ReplaySystem.getInstance().save(ReplayActivity.this, getFileName());
                break;
            case LOAD_REPLAY_FILE:
                value = ReplaySystem.getInstance().load(ReplayActivity.this, getFileName());
                break;
        }
    }

    @Override
    public void handleResponse(int request_type) throws Exception {
        switch(request_type)
        {
            case SAVE_REPLAY_FILE:
                break;
            case LOAD_REPLAY_FILE:
                break;
            default:
                value = "NIL";
        }
        Toast.makeText(ReplayActivity.this, value, Toast.LENGTH_SHORT).show();
    }
}