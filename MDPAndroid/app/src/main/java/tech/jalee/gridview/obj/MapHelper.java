package tech.jalee.gridview.obj;

import android.util.Log;

import java.util.ArrayList;

import static tech.jalee.gridview.view.MDPMapView.COL_LENGTH;
import static tech.jalee.gridview.view.MDPMapView.ROW_LENGTH;

public class MapHelper {

    private static String TAG = "MapHelper";

    private static String BinaryToHexadecimal(String binaryStr)
    {
        int decimal = Integer.parseInt(binaryStr,2);
        String hexStr = Integer.toString(decimal,16);
        return hexStr;
    }

    /**
     * Convert the Grid into the Part One Map Descriptor Format
     * @param map
     * @return Map Descriptor Format of the grid for Part One
     */
    public static String GridToMDF1(char[][] map)
    {
        String binaryString = "";
        for(int row = 0; row < ROW_LENGTH; ++row)
        {
            for(int col = 0; col < COL_LENGTH; ++col)
            {
                char val = map[row][col];
                binaryString += val == 'U' ? "0" : "1";
            }
        }
        return ConvertBinaryToMDF(true, binaryString);
    }

    /**
     * Convert the Grid into the Part Two Map Descriptor Format
     * @param map
     * @return Map Descriptor Format of the grid for Part Two
     */
    public static String GridToMDF2(char[][] map)
    {
        String binaryString = "";
        for(int row = 0; row < ROW_LENGTH; ++row)
        {
            for(int col = 0; col < COL_LENGTH; ++col)
            {
                char val = map[row][col];
                binaryString += val == 'O' ? "1" : val == 'U' ? "" : "0";
            }
        }
        return ConvertBinaryToMDF(false, binaryString);
    }

    private static String ConvertBinaryToMDF(boolean isPartOne, String binary)
    {
        Log.d(TAG, "pre-Binary: " + binary);
        if(isPartOne)
        {
            binary = "11" + binary + "11";
        }
        else
        {
            Log.d(TAG, String.valueOf(binary.length()));
            int remainder = binary.length() % 8;
            for(int pad = remainder; pad < 8; ++pad)
            {
                binary += "0";
            }
        }
        Log.d(TAG, "post-Binary: " + binary);
        String hexadecimal = "";
        for(int length = 0; length <  binary.length(); length+=4)
        {
            hexadecimal += BinaryToHexadecimal(binary.substring(length, length+4));
        }
        return hexadecimal.toUpperCase();
    }

    public static String HexToBinary(String hexChar)
    {
        int dec = Integer.parseInt(hexChar, 16);
        String bin = Integer.toBinaryString(dec);
        if(bin.length()==3){bin="0"+bin;}
        if(bin.length()==2){bin="00"+bin;}
        if(bin.length()==1){bin="000"+bin;}
        return bin;
    }

    private static String RemovePadding(boolean isPartOne, String mdf)
    {
        StringBuilder sb = new StringBuilder();
        if(isPartOne)
        {
            for(int i = 0; i < mdf.length(); ++i)
            {
                sb.append(HexToBinary(mdf.substring(i, i+1)));
            }
            return sb.substring(2, sb.length()-3);
            // read all and then substring +2,-3
        }
        else
        {
            for(int i = 0; i < mdf.length(); ++i)
            {
                sb.append(HexToBinary(mdf.substring(i, i+1)));
            }
            return sb.toString();
        }
    }

    public static String[] getMDF(String mdf1, String mdf2)
    {
        String[] mdf = new String[2];
        mdf[0] = RemovePadding(true, mdf1);
        mdf[1] = RemovePadding(false, mdf2);
        return mdf;
    }

}
