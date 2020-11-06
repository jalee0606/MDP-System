package tech.jalee.gridview;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import tech.jalee.gridview.view.ReplayActivity;

public class ReplaySystem {

    private ArrayList<String> plot = new ArrayList<String>();

    private int _frameNo = 0;
    private Timer _replayTimer;

    private boolean isStarted = false;

    private static ReplaySystem _instance = null;

    private Handler mHandler;

    public static ReplaySystem getInstance()
    {
        if(_instance == null)
            _instance = new ReplaySystem();
        return _instance;
    }


    public void addReplayHandler(Handler handler)
    {
        mHandler = handler;
    }

    public void putMap(String map)
    {
        plot.add("map:"+map);
    }

    public void putRobot(String robot)
    {
        plot.add("robot:"+robot);
    }

    public void start()
    {
        if(isStarted) {
            _replayTimer.cancel();
            isStarted = false;
        } else {
            _replayTimer = new Timer();
            _replayTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if(_frameNo < plot.size()) {
                        Message message = mHandler.obtainMessage(ReplayActivity.REPLAY_PLAY);
                        Bundle bundle = new Bundle();
                        bundle.putString("map", plot.get(_frameNo));
                        message.setData(bundle);
                        message.sendToTarget();
                        _frameNo += 1;
                    } else {
                        stop();
                    }
                }
            }, 0, 200);
            isStarted = true;
        }
    }

    public void stop()
    {
        if(isStarted) {
            reset();
            start();
        }

    }

    private void reset()
    {
        _frameNo = 0;
    }

    public String save(Context context, String sFileName){
        if(plot.size() <= 0)
            return "Replay data not found";
        File dir = new File(context.getFilesDir(), "replay");
        if(!dir.exists()){
            dir.mkdir();
        }
        try {
            File replayFile = new File(dir, sFileName);
            FileWriter writer = new FileWriter(replayFile);
            for(String str : plot) {
                writer.write(str + "\r\n");
            }
            writer.flush();
            writer.close();
            return String.format("Replay %s saved successfully", sFileName);
        } catch (Exception e){
            e.printStackTrace();
            return String.format("%s", e.getMessage());
        }
    }

    public String load(Context context, String sFileName)
    {
        File dir = new File(context.getFilesDir(), "replay");
        if(!dir.exists()){
            dir.mkdir();
        }
        try {
            plot.clear();
            File replayFile = new File(dir, sFileName);
            if(!replayFile.exists())
                return String.format("Replay %s does not exist", sFileName);
            FileInputStream fis = new FileInputStream(replayFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            String line = reader.readLine();
            while(line != null) {
                plot.add(line);
                line = reader.readLine();
            }
            return String.format("Replay %s loaded successfully.", sFileName);
        } catch (Exception e){
            e.printStackTrace();
            return String.format("%s", e.getMessage());
        }
    }

    public void clear()
    {
        plot.clear();
        _frameNo = 0;
    }



}
