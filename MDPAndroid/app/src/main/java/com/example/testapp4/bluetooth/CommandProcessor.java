package com.example.testapp4.bluetooth;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class CommandProcessor {

    private List<ICommandProcessor> mObservers;

    public CommandProcessor()
    {
        mObservers = new ArrayList<ICommandProcessor>();
    }

    public void addObserver(ICommandProcessor observer)
    {
        mObservers.add(observer);
    }

    public boolean removeObserver(ICommandProcessor observer)
    {
        return mObservers.remove(observer);
    }

    public void parse(String cmd)
    {

        String[] args = cmd.split(":");
        for(ICommandProcessor observer : mObservers)
        {
            try {
                observer.onCommandReceived(args[0], args[1]);
            } catch (Exception e)
            {
                Log.d("CommandProcessor", cmd);
            }
        }
    }

}
