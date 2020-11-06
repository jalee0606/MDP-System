package tech.jalee.gridview.view;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import tech.jalee.gridview.obj.Map;
import tech.jalee.gridview.obj.MapHelper;
import tech.jalee.gridview.obj.Position;
import tech.jalee.gridview.obj.Robot;

public class MapViewLayout extends LinearLayout implements View.OnClickListener {

    private MDPMapView mapView;
    private LinearLayout mButtonLayout;
    private ScrollView mScrollView;
    private LinearLayout mdfLayout;
    private LinearLayout mHorizontalLayout;

    private MapViewInterface _interface;

    private Button mModeBtn, mEnableObstacleBtn, mEnableWaypointBtn, mGenerateMDFBtn;

    private final int mModeBtnId = 1, mEnableObstacleBtnId = 2, mReplayBtnId = 8, mEnableWaypointBtnId = 3, mGenerateMDFBtnId = 4, mRefreshBtnId = 5, mScreenshotBtnId = 6, mClearBtn = 7;

    private final int[] mButtonId = {mModeBtnId, mScreenshotBtnId, mEnableWaypointBtnId, mGenerateMDFBtnId, mClearBtn, mRefreshBtnId, mReplayBtnId, mEnableObstacleBtnId};
    private final String[] mButtonText = {"AUTOMATIC", "LEADERBOARD", "ENABLE WAYPOINT", "GENERATE MDF", "CLEAR MAP", "REFRESH", "REPLAY", "ENABLE OBSTACLE"};

    public MapViewLayout(Context context) {
        super(context);
        init();
    }

    public MapViewLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MapViewLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public MapViewLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    private void init()
    {
        setLayoutOrientation();
        createMapView();
        createButtonLayout();
    }

    private void setLayoutOrientation()
    {
        setOrientation(HORIZONTAL);
    }

    private void createMapView()
    {
        mapView = new MDPMapView(this.getContext());
        this.addView(mapView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.2f));
    }

    public void updateMapWithRpiString(String hex)
    {
        mapView.updateMapWithRpiString(hex);
    }

    public void moveRobot(String movement)
    {
        int direction = -1;
        switch(movement)
        {
            case "f":
                direction = Robot.DIRECTION_TOP;
                break;
            case "r":
                direction = Robot.DIRECTION_BOTTOM;
                break;
            case "sl":
                direction = Robot.DIRECTION_LEFT;
                break;
            case "sr":
                direction = Robot.DIRECTION_RIGHT;
                break;
            default:
                direction = -1;

        }
        mapView.moveRobot(direction);
        mapView.invalidate();
    }

    public void updateMapWithCustomFormat(String mapdata)
    {
        mapView.updateMapWithCustomFormat(mapdata);
    }

    private void createButtonLayout()
    {
        mScrollView = new ScrollView(this.getContext());
        mButtonLayout = new LinearLayout(this.getContext());
        mButtonLayout.setOrientation(VERTICAL);
        for(int i = 0; i < mButtonId.length; i++)
        {
            Button tempBtn = new Button(this.getContext());
            tempBtn.setId(mButtonId[i]);
            tempBtn.setText(mButtonText[i]);
            tempBtn.setTextSize(12);
            tempBtn.setOnClickListener(this);
            tempBtn.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            mButtonLayout.addView(tempBtn);
        }
        mScrollView.addView(mButtonLayout, new LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        this.addView(mScrollView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 0.6f));
    }

    public void resetRobot()
    {
        mapView.reinitializeRobot();
    }

    public void updateRobot(int col, int row, String direction)
    {
        mapView.updateRobotPos(col, row, direction);
    }

    public void addImage(String imageStr)
    {
        Map.getInstance().addImage(imageStr);
        mapView.invalidate();
    }

    public void setInterface(MapViewInterface _interface)
    {
        this._interface = _interface;
        mapView.setInterface(_interface);
    }

    public void refreshMap() {
        mapView.invalidate();
    }

    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case mModeBtnId:
                ((Button)view).setText(mapView.toggleMode() == true ? "AUTOMATIC" : "MANUAL");
                break;
            case mEnableObstacleBtnId:
                Button obstacleEnabledBtn = (Button)view;
                boolean obstacleVal = mapView.toggleObstacle();
                if(obstacleVal)
                {
                    obstacleEnabledBtn.setText("DISABLE OBSTACLE");
                }
                else
                {
                    if(mapView.isWayPointEnabled())
                    {
                        Toast.makeText(MapViewLayout.this.getContext(), "DISABLE WAYPOINT FIRST", Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        obstacleEnabledBtn.setText("ENABLE OBSTACLE");
                    }
                }
                break;
            case mEnableWaypointBtnId:
                Button waypointEnabledBtn = (Button)view;
                boolean waypointVal = mapView.toggleWaypoint();
                if(waypointVal)
                {
                    waypointEnabledBtn.setText("DISABLE WAYPOINT");
                }
                else
                {
                    if(mapView.isObstacleEnabled())
                    {
                        Toast.makeText(MapViewLayout.this.getContext(), "DISABLE OBSTACLE FIRST", Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        waypointEnabledBtn.setText("ENABLE WAYPOINT");
                    }
                }
                break;
            case mGenerateMDFBtnId:
                _interface.onGenerateMDF(MapHelper.GridToMDF1(Map.getInstance().getGrid()), MapHelper.GridToMDF2(Map.getInstance().getGrid()));
                break;
            case mRefreshBtnId:
                mapView.toggleRefresh();
                break;
            case mScreenshotBtnId:
                Button button = (Button)view;
                if (button.getText().equals("LEADERBOARD")) {
                    button.setText("CONTROL");
                } else {
                    button.setText("LEADERBOARD");
                }
                _interface.onResultScreen();
                break;
            case mClearBtn:
                //updateRobot(5,5);
                mapView.clear();
                break;
            case mReplayBtnId:
                Intent i = new Intent(getContext(), ReplayActivity.class);
                getContext().startActivity(i);
                break;
        }
    }

    public interface MapViewInterface
    {
        void onGenerateMDF(String mdfPartOne, String mdfPartTwo);
        void onWaypointCreated(Position pos);
        void onResultScreen();
    }
}
