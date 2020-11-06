package com.example.testapp4;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class BluetoothQueue {

    private Queue<String> _queue;
    private Handler mHandler;
    private Random random = new Random();
    private Timer _timer;

    public BluetoothQueue(Handler handler)
    {
        _queue = new LinkedList<>();
        mHandler = handler;
    }

    public void add(String s)
    {
        _queue.offer(s);
    }

    public void start()
    {
        _timer = new Timer();
        _timer.schedule(new TimerTask() {
            @Override
            public void run() {
                while(true) {
                    String x = _queue.poll();
                    if(x != null) {
                        byte[] mmBuffer = x.getBytes();
                        Message readMsg = mHandler.obtainMessage(Chat.MESSAGE_READ, mmBuffer.length, -1, mmBuffer);
                        readMsg.sendToTarget();
                    }
                    try {
                        Thread.sleep(20);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }, 0);
    }

    public void stop()
    {
        _timer.cancel();
    }




}
