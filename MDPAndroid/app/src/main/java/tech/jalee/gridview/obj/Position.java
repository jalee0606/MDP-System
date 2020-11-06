/// This class is created for the robot as it is too memory intensive to store map in the robot.

package tech.jalee.gridview.obj;

import androidx.annotation.Nullable;

public class Position {

    private int _rowPos;
    private int _colPos;

    public Position()
    {
    }

    public Position(int row, int col)
    {
        _rowPos = row;
        _colPos = col;
    }

    public void setX(int x)
    {
        _colPos = x;
    }

    public void setY(int y)
    {
        _rowPos = y;
    }

    public int getX()
    {
        return _colPos;
    }

    public int getY()
    {
        return _rowPos;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof Position)
        {
            Position pos2 = (Position)obj;
            return (pos2._colPos == this._colPos && pos2._rowPos == this._rowPos);
        }
        return false;
    }

    @Override
    public String toString()
    {
        return "(" + _colPos + "," + _rowPos + ")";
    }
}
