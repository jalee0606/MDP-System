package tech.jalee.gridview.obj;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;

import tech.jalee.gridview.view.MDPMapView;

public class Robot {

    private static final String TAG = "Robot";
    private static final int MAX_GRID_TRAVELLED = 2;

    public static final int DIRECTION_TOP = 1;
    public static final int DIRECTION_RIGHT = 2;
    public static final int DIRECTION_BOTTOM = 3;
    public static final int DIRECTION_LEFT = 4;

    private static final String ROBOT_COORDINATE_PREF = "robot_coordinate";

    private static Robot _robot;

    private Robot.IRobotListener _listener;

    private Paint _borderColor = new Paint();
    private Paint _robotColor = new Paint();

    private Path _directionPath = new Path();

    private float _x;
    private float _y;

    private int _gridSize;

    private int _direction;

    private float _radius;

    public static Robot getInstance()
    {
        if(_robot == null)
            _robot = new Robot();
        return _robot;
    }

    public void initialize(int cellSize)
    {
        if(this._gridSize == cellSize) // Check if Robot has been shifted from MainActivity back to Robot View
            return;
        Log.d("Robot", "initialize() pass check");
        this._gridSize = cellSize;
        _radius = (cellSize*3.0f)/2.0f;
        _direction = DIRECTION_TOP;
        _borderColor.setStyle(Paint.Style.STROKE);
        _borderColor.setColor(Color.BLACK);
        _robotColor.setStyle((Paint.Style.FILL));
        _robotColor.setColor(Color.MAGENTA);
        _x = _radius;
        _y = (_gridSize * MDPMapView.ROW_LENGTH) - _radius;
        drawPath();
    }

    private Robot()
    {
    }

    public void setListener(IRobotListener listener)
    {
        _listener = listener;
    }

    private void drawPath()
    {
        _directionPath.reset();
        float x1 = 0.0f, x2 = 0.0f,x3 = 0.0f,y1 = 0.0f,y2 = 0.0f,y3 = 0.0f;
        float triFactor = _gridSize/2.0f;
        switch(_direction)
        {
            case DIRECTION_TOP:
                x1 = _x;
                y1 = _y - _radius + 5;
                x2 = _x + triFactor;
                y2 = _y - _radius + (triFactor);
                x3 = _x - triFactor;
                y3 = _y - _radius + (triFactor);
                break;
            case DIRECTION_RIGHT:
                x1 = _x + _radius - 5;
                y1 = _y;
                x2 = _x + _radius - (triFactor);
                y2 = _y - triFactor;
                x3 = _x + _radius - (triFactor);
                y3 = _y + (triFactor);
                break;
            case DIRECTION_BOTTOM:
                x1 = _x;
                y1 = _y + _radius - 5;
                x2 = _x + triFactor;
                y2 = _y + _radius - (triFactor);
                x3 = _x - triFactor;
                y3 = _y + _radius - (triFactor);
                break;
            case DIRECTION_LEFT:
                x1 = _x - _radius + 5;
                y1 = _y;
                x2 = _x - _radius + (triFactor);
                y2 = _y - triFactor;
                x3 = _x - _radius + (triFactor);
                y3 = _y + (triFactor);
                break;

        }
        _directionPath.moveTo(x1, y1);
        _directionPath.lineTo(x2, y2);
        _directionPath.lineTo(x3, y3);
        _directionPath.close();
    }

    public void setPosition(int col, int row, String val)
    {
        _x = (col-2)*_gridSize+_radius;
        _y = (_gridSize * (MDPMapView.ROW_LENGTH-(row-2))) - _radius;
        switch(val)
        {
            case "N":
                _direction = DIRECTION_TOP;
                break;
            case "S":
                _direction = DIRECTION_BOTTOM;
                break;
            case "E":
                _direction = DIRECTION_RIGHT;
                break;
            case "W":
                _direction = DIRECTION_LEFT;
                break;
        }
        drawPath();
    }


    public int getRowPos()
    {
        return MDPMapView.ROW_LENGTH-1-((int)getOriginY()/_gridSize);
    }

    public int getColPos()
    {
        return (int) getOriginX()/_gridSize;
    }

    public float getOriginX()
    {
        return _x;
    }

    public float getOriginY()
    {
        return _y;
    }

    private boolean allowToMove(int direction)
    {
        boolean isAllow = false;
        int rowPos = getRowPos();
        int colPos = getColPos();
        if(direction == DIRECTION_TOP && rowPos < (MDPMapView.ROW_LENGTH - 2))
        {
            isAllow = true;
            rowPos += 2;
        }
        else if(direction == DIRECTION_BOTTOM && rowPos > 1)
        {
            isAllow = true;
            rowPos -= 2;
        }
        else if(direction == DIRECTION_RIGHT && colPos < (MDPMapView.COL_LENGTH - 2))
        {
            isAllow = true;
            colPos += 2;
        }
        else if(direction == DIRECTION_LEFT && colPos > 1)
        {
            isAllow = true;
            colPos -= 2;
        }
        return isAllow && !obstacleCheck(direction, rowPos, colPos);
    }

    private boolean obstacleCheck(int direction, int originRow, int originCol)
    {
        char[][] grid = Map.getInstance().getGrid();
        if(direction == DIRECTION_TOP || direction == DIRECTION_BOTTOM)
        {
            boolean value = (grid[originRow][originCol] == 'O' || grid[originRow][originCol - 1] == 'O' || grid[originRow][originCol + 1] == 'O');
            Log.d(TAG, String.format("TOP|BOTTOM --> Obstacle Detected: %s", String.valueOf(value)));
            return value;
        }
        else
        {
            boolean value = (grid[originRow][originCol] == 'O' || grid[originRow-1][originCol] == 'O' || grid[originRow+1][originCol] == 'O');
            Log.d(TAG, String.format("TOP|BOTTOM --> Obstacle Detected: %s", String.valueOf(value)));
            return value;
        }

    }

    public void move(int direction)
    {
        if(allowToMove(direction)) {
            switch (direction) {
                case DIRECTION_TOP:
                    _y -= _gridSize;
                    break;
                case DIRECTION_RIGHT:
                    _x += _gridSize;
                    break;
                case DIRECTION_BOTTOM:
                    _y += _gridSize;
                    break;
                case DIRECTION_LEFT:
                    _x -= _gridSize;
                    break;
            }
            _direction = direction;
            drawPath();
            _listener.RobotMoved(getColPos(), getRowPos());
        }
    }

    public Path getDirectionPath()
    {
        return this._directionPath;
    }

    public float getRadius()
    {
        return _radius;
    }

    public Paint getBorderPaint()
    {
        return _borderColor;
    }

    public Paint getRobotPaint()
    {
        return _robotColor;
    }

    public interface IRobotListener {

        void RobotMoved(int x, int y);

    }
}
