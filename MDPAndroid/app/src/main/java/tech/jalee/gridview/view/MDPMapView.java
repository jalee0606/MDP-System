/**
 * @author: Lee Jun Ao
 * MapView for Nanyang Technological University Multi-Disciplinary Project 2020
 *
 * This CustomView is designed to simulate the movement of robot as well as provide
 * a graphical user interface to the Android user such that he can see the robot.
 *
 * TODO: beautiful the code and make it less messy by pushing similar call into methods
 */

package tech.jalee.gridview.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.Iterator;
import java.util.List;

import androidx.annotation.Nullable;
import com.example.testapp4.R;
import tech.jalee.gridview.obj.Image;
import tech.jalee.gridview.obj.Map;
import tech.jalee.gridview.obj.MapHelper;
import tech.jalee.gridview.obj.Position;
import tech.jalee.gridview.obj.Robot;

public class MDPMapView extends View implements Robot.IRobotListener {

    private static final String TAG = "MDPMapView";
    private Robot _robot;

    private MapViewLayout.MapViewInterface _callback;
    private int _viewHeight;
    private int _viewWidth;
    private int _canvasWidth;
    private int _canvasHeight;
    private int _cellSize;
    private float _hPadding;
    private float _wPadding;

    private boolean _waypointEnabled = false, _obstacleEnabled = false, _automatedMode = true, _refreshBtn = false;

    /// Constant Variables
    public static final int ROW_LENGTH = 20;
    public static final int COL_LENGTH = 15;
    private static final boolean ENABLE_GRID_NUMBERING = true;
    private static final int NUMBERING_GRID_SIZE = 1;

    // Canvas Paint
    private static final Paint STROKE_PENCIL = new Paint();
    private static final Paint OBSTACLE_PENCIL = new Paint();
    private static final Paint DIRECTION_PENCIL = new Paint();
    private static final Paint UNEXPLORED_PENCIL = new Paint();
    private static final Paint EXPLORED_PENCIL = new Paint();
    private static final Paint START_PENCIL = new Paint();
    private static final Paint GOAL_PENCIL = new Paint();
    private static final Paint TEXT_PENCIL = new Paint();
    private static final Paint WAYPOINT_PENCIL = new Paint();

    // 2D Grid State Declaration
    private static final char EXPLORED = 'E';
    private static final char UNEXPLORED = 'U';
    private static final char OBSTACLE = 'O';
    private static final char GOAL = 'G';
    private static final char START = 'S';
    private static final char WAYPOINT = 'W';

    public MDPMapView(Context context) {
        super(context);
        setSaveEnabled(true);
    }

    public MDPMapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setSaveEnabled(true);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
        Log.d(TAG, "onRestoreInstanceState()");
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Log.d(TAG, "onSaveInstanceState()");
        return super.onSaveInstanceState();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        Log.d("MapView:onMeasure()", String.format("(%s,%s)", MeasureSpec.toString(widthMeasureSpec), MeasureSpec.toString(heightMeasureSpec)));
        int additionalCell = calculate(widthSize, heightSize, ENABLE_GRID_NUMBERING);
        Log.d("MapView:calculate()", String.format("Cell Size: %d", _cellSize));
        // Check how much my canvas need and resize the view according to the Canvas need.
        _canvasWidth = (_cellSize*(COL_LENGTH+additionalCell)) + getPaddingLeft() + getPaddingRight();
        _canvasHeight = (_cellSize*(ROW_LENGTH+additionalCell)) + getPaddingTop() + getPaddingBottom();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setInterface(MapViewLayout.MapViewInterface _interface)
    {
        _callback = _interface;
    }

    public void moveRobot(int id)
    {
        _robot.move(id);
        this.invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.d("MapView:onSizeChanged()", String.format("(%d,%d) -> (%d,%d)", oldw, oldh, w, h));
        _viewHeight = h;
        _viewWidth = w;
        _hPadding = (h - _canvasHeight)/2;
        _wPadding = (w - _canvasWidth)/2;
        _robot = Robot.getInstance();
        _robot.initialize(_cellSize);
        _robot.setListener(this);
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //canvas.drawColor(Color.WHITE);
        // Log.d("MDPMapView:onDraw()", String.format("Dimen: (%d, %d)", canvas.getWidth(), canvas.getHeight()));
        moveCanvasToOrigin(canvas);
        drawGrid(canvas);
        //drawStartGoal(canvas);
        moveCanvasToNumbering(canvas);
        drawNumbering(canvas);
        moveCanvasToOrigin(canvas);
        drawBorder(canvas);
        drawRobot(canvas);
        drawImage(canvas);
    }

    public void updateMapWithCustomFormat(String mapdata)
    {
        Map.getInstance().setMap(mapdata);
        Log.d("MDPMapView", "Update Map");
        this.invalidate();
    }

    private void moveCanvasToOrigin(Canvas canvas)
    {
        if(ENABLE_GRID_NUMBERING)
            canvas.translate(_cellSize, 0);
    }

    private void moveCanvasToNumbering(Canvas canvas)
    {
        if(ENABLE_GRID_NUMBERING)
            canvas.translate(-_cellSize, 0);
    }

    private void drawGrid(Canvas canvas)
    {
        char[][] _gridMap = Map.getInstance().getGrid();
        canvas.translate(_wPadding, _hPadding);
        for(int col = 0; col < COL_LENGTH; ++col)
        {
            for(int row = ROW_LENGTH-1; row >= 0; --row)
            {
                int actualRow = 19 - row;
                char val = _gridMap[row][col];
                int left = (col * _cellSize);
                int right = (col+1)*_cellSize;
                int top = (actualRow * _cellSize);
                int bottom = (actualRow+1)*_cellSize;
                canvas.drawRect(left, top,
                        right, bottom,
                        val == 'U' ? UNEXPLORED_PENCIL : val == 'O' ? OBSTACLE_PENCIL : val == 'G' ? GOAL_PENCIL : val == 'S' ? START_PENCIL : val == 'W' ? WAYPOINT_PENCIL : EXPLORED_PENCIL);
                canvas.drawRect(left, top,
                        right, bottom, STROKE_PENCIL);
            }
        }
        Position wp = Map.getInstance().getWaypoint();
        if(wp != null)
        {
            int actualRow = 19 - wp.getY();
            int left = wp.getX()*_cellSize;
            int right = (wp.getX()+1)*_cellSize;
            int top = actualRow * _cellSize;
            int bottom = (actualRow+1)*_cellSize;
            canvas.drawRect(left, top,
                    right, bottom,
                    WAYPOINT_PENCIL);
            canvas.drawRect(left, top,
                    right, bottom, STROKE_PENCIL);
        }
    }

    public void updateMapWithRpiString(String hex)
    {
        Map.getInstance().readMapFromRpiString(hex);
        this.invalidate();
    }

    public void drawStartGoal(Canvas canvas)
    {
        Position goalPos = Map.getInstance().getGoal();
        Position startPos = Map.getInstance().getStart();
        for(int i = -1; i < 2; i++)
        {
            for(int j = -1; j < 2; j++)
            {
                int startRow = 19 - (startPos.getY()+i);
                int startLeft = ((startPos.getX()+j) * _cellSize);
                int startRight = ((startPos.getX()+j)+1)*_cellSize;
                int startTop = (startRow * _cellSize);
                int startBottom = (startRow+1)*_cellSize;
                int goalRow = 19 - (goalPos.getY()+i);
                int goalLeft = ((goalPos.getX()+j) * _cellSize);
                int goalRight = ((goalPos.getX()+j)+1)*_cellSize;
                int goalTop = (goalRow * _cellSize);
                int goalBottom = (goalRow+1)*_cellSize;
                canvas.drawRect(startLeft, startTop,
                        startRight, startBottom,
                        START_PENCIL);
                canvas.drawRect(startLeft, startTop,
                        startRight, startBottom, STROKE_PENCIL);
                canvas.drawRect(goalLeft, goalTop,
                        goalRight, goalBottom,
                        GOAL_PENCIL);
                canvas.drawRect(goalLeft, goalTop,
                        goalRight, goalBottom, STROKE_PENCIL);
            }
        }

    }

    private void drawNumbering(Canvas canvas)
    {
        if(!ENABLE_GRID_NUMBERING)
            return;
        float centerVal = (TEXT_PENCIL.descent() + TEXT_PENCIL.ascent()) / 2.0f;
        for (int row = 21; row > 1; --row) {
            int actualRow = 21 - row;
            String value = String.valueOf(row-2);
            float xPos = (_cellSize - TEXT_PENCIL.getTextSize() * Math.abs(value.length()/2))/3;
            float yPos = (actualRow * _cellSize * 1.0f) + ((_cellSize / 2.0f) - centerVal);
            canvas.drawText(value, xPos, yPos, TEXT_PENCIL);
        }
        for (int col = 0; col < COL_LENGTH; ++col) {
            String value = String.valueOf(col);
            float xPos = ((col+1)*_cellSize) + _cellSize/4f; //+ (_cellSize - TEXT_PENCIL.getTextSize() * Math.abs(value.length()/2))/2;
            float yPos = (ROW_LENGTH*_cellSize) + ((_cellSize/2.0f) - centerVal);
            canvas.drawText(value, xPos, yPos, TEXT_PENCIL);
        }
    }

    private void drawImage(Canvas canvas)
    {
        Iterator<Image> images = Map.getInstance().getImageBlocks();
        while(images.hasNext())
        {
            Image image = images.next();
            float posX = image.getX()*_cellSize;
            float posY = ((ROW_LENGTH)-(image.getY()+1))*_cellSize;
            canvas.drawBitmap(image.getBitmap(this.getContext(), _cellSize), posX, posY, null);
        }

    }

    public void updateRobotPos(int col, int row, String direction)
    {
        _robot.setPosition(col, row, direction);
        this.invalidate();
    }

    private void drawBorder(Canvas canvas)
    {
        int height = _canvasHeight;
        int width = _canvasWidth;
        if(ENABLE_GRID_NUMBERING) {
            height -= _cellSize;
            width -= _cellSize;
        }
        for (int i = 0; i <= COL_LENGTH; i++) {
            canvas.drawLine(i * _cellSize, 0, i * _cellSize, height, STROKE_PENCIL);
        }

        for (int i = 0; i <= ROW_LENGTH; i++) {
            canvas.drawLine(0, i * _cellSize, width, i * _cellSize, STROKE_PENCIL);
        }

    }

    private void drawRobot(Canvas canvas)
    {
        canvas.drawCircle(_robot.getOriginX(), _robot.getOriginY(), _robot.getRadius(), _robot.getRobotPaint());
        canvas.drawCircle(_robot.getOriginX(), _robot.getOriginY(), _robot.getRadius(), _robot.getBorderPaint());
        canvas.drawPath(_robot.getDirectionPath(), DIRECTION_PENCIL);
    }

    public boolean isWayPointEnabled()
    {
        return _waypointEnabled;
    }

    public boolean isObstacleEnabled()
    {
        return _obstacleEnabled;
    }

    public boolean toggleWaypoint()
    {
        if(isWayPointEnabled()) {
            _waypointEnabled = false;
        } else if(!isWayPointEnabled() && !isObstacleEnabled()) {
            _waypointEnabled = true;
            return true;
        }
        return false;
    }

    public boolean toggleObstacle()
    {
        if(isObstacleEnabled()) {
            _obstacleEnabled = false;
        } else if(!isWayPointEnabled() && !isObstacleEnabled()) {
            _obstacleEnabled = true;
            return true;
        }
        return false;
    }

    public boolean toggleMode()
    {
        _automatedMode = !_automatedMode;
        return _automatedMode;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        initializePaint();
    }

    public void reinitializeRobot()
    {
        _robot = Robot.getInstance();
        _robot.initialize(_cellSize);
        _robot.setListener(this);
    }

    private void initializePaint()
    {
        STROKE_PENCIL.setStyle(Paint.Style.STROKE);
        STROKE_PENCIL.setColor(Color.BLACK);
        WAYPOINT_PENCIL.setStyle(Paint.Style.FILL);
        WAYPOINT_PENCIL.setColor(Color.argb(255, 135,206,235));
        UNEXPLORED_PENCIL.setStyle(Paint.Style.FILL);
        UNEXPLORED_PENCIL.setColor(Color.argb(255, 231, 230, 230));
        DIRECTION_PENCIL.setStyle(Paint.Style.FILL);
        DIRECTION_PENCIL.setColor(Color.argb(255, 0, 0, 0));
        EXPLORED_PENCIL.setStyle(Paint.Style.FILL);
        EXPLORED_PENCIL.setColor(Color.argb(255,115, 240, 112));
        START_PENCIL.setStyle(Paint.Style.FILL);
        START_PENCIL.setColor(Color.argb(255, 180, 231, 254));
        GOAL_PENCIL.setStyle(Paint.Style.FILL);
        GOAL_PENCIL.setColor(Color.argb(255, 204, 204, 255));
        OBSTACLE_PENCIL.setStyle(Paint.Style.FILL);
        OBSTACLE_PENCIL.setColor(Color.argb(255, 255, 83, 83));
        TEXT_PENCIL.setColor(Color.BLACK);
        TEXT_PENCIL.setTextSize(15);
        TEXT_PENCIL.setTypeface(Typeface.DEFAULT_BOLD);
    }

    private int calculate(int width, int height, boolean enableNumbering)
    {
        int additionalCell = enableNumbering ? 1 : 0;
        int baseValue;
        _canvasWidth = height;
        _canvasHeight = width;
        Log.d("MDPMapView:calculate()", String.format("Dimen: (%d, %d)", _canvasWidth, _canvasHeight));
        if (_canvasWidth / _canvasHeight < COL_LENGTH / ROW_LENGTH) {
            baseValue = width / (COL_LENGTH + additionalCell);
        } else {
            baseValue = height / (ROW_LENGTH + additionalCell);
        }
        _cellSize = baseValue;
        return additionalCell;
        // _padding = _viewWidth - (baseValue*COL_LENGTH);
        //this.setMeasuredDimension(_cellWidth*COL_LENGTH,_cellHeight*ROW_LENGTH);
    }

    public void updateMap(int row, int col, char val)
    {
        char[][] _gridMap = Map.getInstance().getGrid();
        if(_gridMap[row][col] == GOAL || _gridMap[row][col] == START) // do not update goal and start
            return;
        if(val == Map.GridType.WAYPOINT) {
            Position waypoint = new Position(row, col);
            Map.getInstance().setWaypoint(waypoint);
            this._callback.onWaypointCreated(waypoint);
        }
        else {
            Map.getInstance().updateGrid(row, col, val);
        }
        this.invalidate(); // Invalidate the current view, so the view will call onDraw to render the canvas.
    }

    private String getValue(char val)
    {
        switch(val)
        {
            case 'S':
            case 'G':
            case 'E':
            case 'O':
                return "1";
            default:
                return "0";
        }
    }

    @Override
    public void RobotMoved(int x, int y) {
        // think of logic
    }

    public interface IMapViewListener {
        void onGridValueChanged(int row, int col, char value); // Depend
        void onGridInstantiated(char[][] grid);
    }

    public void updateMapWithHex(String hex)
    {
        Map.getInstance().readMapFromRpiString(hex);
        this.invalidate();
    }

    public void toggleRefresh()
    {
        this._refreshBtn = true;
        this.invalidate();
    }

    public void clear()
    {
        Map.getInstance().clear();
        this.invalidate();
    }

    public void invalidate()
    {
        if(this._automatedMode || this._refreshBtn) {
            this._refreshBtn = false;
            super.invalidate();
        }
    }

    /*
     * Handle touch event for tapping onto the Coordinate
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float canvasX = event.getX() - _wPadding;
            float canvasY = event.getY() - _hPadding;
            float minX = 0;
            float minY = 0;
            float maxX = COL_LENGTH*_cellSize;
            float maxY = ROW_LENGTH*_cellSize;
            if(ENABLE_GRID_NUMBERING)
            {
                // Determine offset
                minX += NUMBERING_GRID_SIZE*_cellSize;
                maxY -= NUMBERING_GRID_SIZE*_cellSize;
                canvasX -= NUMBERING_GRID_SIZE*_cellSize;
            }
            if((canvasX < minX || canvasY < minY)||(canvasX > maxX || canvasY > maxY))
            {
                return false;
            }
            int column = (int)(canvasX/ _cellSize);
            int row = (int)(canvasY / _cellSize);
            Log.d("MDPMapView", String.format("Tapped (%d,%d)", row, column));
            char val = isObstacleEnabled() ? 'O' : isWayPointEnabled() ? 'W' : 'E';
            updateMap(ROW_LENGTH - (row+1), column, val);
            //invalidate();
        }
        return super.onTouchEvent(event);
    }
}
