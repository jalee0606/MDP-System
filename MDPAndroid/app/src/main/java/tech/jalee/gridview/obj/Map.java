/**
 * Map class - Singleton instance for multiple access point without using
 * too much interface and callback for transmitting of data from one access point to another.
 * Map data are used by too many objects and in order to ensure all objects are referencing to
 * the latest map data, we decided to make Map a singleton isntance.
 */

package tech.jalee.gridview.obj;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import tech.jalee.gridview.view.IMapObserver;


public class Map {

    private static Map _map;

    private static String TAG = "Map";

    private static final int ROW_LENGTH = 20;
    private static final int COL_LENGTH = 15;

    public static final String MAP_PREFERENCES = "MAP_PREFERENCES";
    public static final String MAP_KEY_VALUE = "MAP_ARRAY";
    public static final String MAP_BACKUP_KEY_VALUE = "MAP_BACKUP_ARRAY";

    public static final String MAP_IMAGES_KEY_VALUE = "MAP_IMAGES_JSON";

    private List<IMapObserver> _observers;

    private List<Image> _images;

    private Position _waypoint = null;

    private final Position _start;

    private final Position _goal;

    private Activity _parentActivity;
    private char[][] _grid;

    /// Singleton Method

    public static Map getInstance()
    {
        if(_map == null)
            _map = new Map();
        return _map;
    }

    /// Class Method

    public Map()
    {
        _observers = new ArrayList<IMapObserver>();
        _images = new ArrayList<Image>();
        _start = new Position(1, 1);
        _goal = new Position(18, 13);
    }

    public Position getGoal()
    {
        return _goal;
    }

    public Position getStart()
    {
        return _start;
    }

    public void setActivity(Activity activity)
    {
        this._parentActivity = activity;
    }

    public void initialize()
    {
        initializeIfNoSharedPreferences();
    }

    public void readMapFromRpiString(String hex)
    {
        String binary = "";
        for(int i = 0; i < hex.length(); ++i)
        {
            binary += MapHelper.HexToBinary(hex.substring(i, i+1));
        }
        updateMap(binary);
    }

    public void backup()
    {
        saveToSharedPref(MAP_BACKUP_KEY_VALUE, saveGridMap());
    }

    public void setWaypoint(Position pos)
    {
        this._waypoint = pos;
    }

    public Position getWaypoint()
    {
        return this._waypoint;
    }

    public void setMap(String customFormat)
    {
        synchronized (_grid) {
            for (int row = 0; row < ROW_LENGTH; ++row) {
                for (int col = 0; col < COL_LENGTH; ++col) {
                    _grid[row][col] = GridType.fromValue(customFormat.charAt((row * COL_LENGTH) + col));
                }
            }
        }
        saveToSharedPref(MAP_KEY_VALUE, saveGridMap());
    }

    public void clear()
    {
        if(_parentActivity == null) // detect if parent activiy have been set.
            return;
        SharedPreferences sharedPref = _parentActivity.getSharedPreferences(MAP_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(MAP_KEY_VALUE);
        editor.commit();
        initializeIfNoSharedPreferences();
    }

    /**
     * Adding an image into the collection/memory to indicate and display the block at the (x,y) position. x and y are zero-based index.
     * @param imageStr (id, x, y)
     */
    public void addImage(String imageStr)
    {
        Image image = Image.fromString(imageStr);
        if(image != null)
            _images.add(image);
    }

    private void updateMap(String binary)
    {
        char[] chArr = binary.toCharArray();
        for(int row = 0; row < ROW_LENGTH; ++row)
        {
            for(int col = 0; col < COL_LENGTH; ++col)
            {
                if(row < 3 && col < 3) {
                    _grid[row][col] = GridType.START;
                } else if(row > 16 && col > 11) {
                    _grid[row][col] = GridType.GOAL;
                } else {
                    _grid[row][col] = chArr[((ROW_LENGTH-1-row)*COL_LENGTH)+col] == '1' ? GridType.OBSTACLE : GridType.EXPLORED;
                }
            }
        }
    }

    private void initializeIfNoSharedPreferences()
    {
        if(_parentActivity == null) // detect if parent activiy have been set.
            return;
        SharedPreferences sharedPref = _parentActivity.getSharedPreferences(MAP_PREFERENCES, Context.MODE_PRIVATE);
        if(sharedPref.contains(MAP_BACKUP_KEY_VALUE))
        {
            String value = sharedPref.getString(MAP_BACKUP_KEY_VALUE, null);
            loadGridMap(value);
            clearSharedPref(MAP_BACKUP_KEY_VALUE);
            saveToSharedPref(MAP_KEY_VALUE, saveGridMap());
        }
        else if(sharedPref.contains(MAP_KEY_VALUE))
        {
            String value = sharedPref.getString(MAP_KEY_VALUE, null);
            loadGridMap(value);
        }
        else
        {
            initializeGridMap();
        }
    }

    private void loadGridMap(String value)
    {
        char[] chrArr = value.toCharArray();
        _grid = new char[ROW_LENGTH][COL_LENGTH];
        for(int row = 0; row < ROW_LENGTH; ++row)
        {
            for(int col = 0; col < COL_LENGTH; ++col)
            {
                _grid[row][col] = chrArr[(row*COL_LENGTH)+col];
            }
        }
    }

    private String saveGridMap()
    {
        char[] chArr = new char[ROW_LENGTH*COL_LENGTH];
        for(int row = 0; row < ROW_LENGTH; ++row)
        {
            for(int col = 0; col < COL_LENGTH; ++col)
            {
                chArr[(row*COL_LENGTH)+col] = _grid[row][col];
            }
        }
        return String.valueOf(chArr);
    }

    private void clearSharedPref(String key)
    {
        if(_parentActivity == null) // detect if parent activiy have been set.
            return;
        SharedPreferences sharedPref = _parentActivity.getSharedPreferences(MAP_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(key);
        editor.apply();
    }

    private void saveToSharedPref(String key, String val)
    {
        if(_parentActivity == null) // detect if parent activiy have been set.
            return;
        SharedPreferences sharedPref = _parentActivity.getSharedPreferences(MAP_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, val);
        editor.apply();
    }

    public void updateGrid(int row, int col, char val)
    {
        _grid[row][col] = val;
        Log.d(TAG, String.format("(%d,%d) = %s", row, col, val));
        saveToSharedPref(MAP_KEY_VALUE, saveGridMap());
        for(int i = 0; i < _observers.size(); ++i)
        {
            _observers.get(i);
        }
    }

    public Iterator<Image> getImageBlocks()
    {
        return _images.iterator();
    }

    public void subscribe(IMapObserver observer)
    {
        _observers.add(observer);
    }

    public void unsubscribe(IMapObserver observer)
    {
        _observers.remove(observer);
    }

    private void initializeGridMap()
    {
        _grid = new char[ROW_LENGTH][COL_LENGTH];
        for(int row = 0; row < ROW_LENGTH; ++row)
        {
            for(int col = 0; col < COL_LENGTH; ++col) // my friend say ++var is more efficient than var++
            {
                _grid[row][col] = GridType.UNEXPLORED;
            }
        }
    }

    public char[][] getGrid()
    {
        char[][] grid;
        synchronized (_grid) {
            grid = _grid;
        }
        return grid;
    }

    public static class GridType {
        // 2D Grid State Declaration
        public static final char EXPLORED = 'E';
        public static final char UNEXPLORED = 'U';
        public static final char OBSTACLE = 'O';
        public static final char GOAL = 'G';
        public static final char START = 'S';
        public static final char WAYPOINT = 'W';

        public static final char fromValue(char val)
        {
            switch(val)
            {
                case '1':
                    return OBSTACLE;
                case '0':
                    return EXPLORED;
                default:
                    return UNEXPLORED;
            }
        }
    }

}
