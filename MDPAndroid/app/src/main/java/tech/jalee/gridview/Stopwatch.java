package tech.jalee.gridview;

import android.os.Handler;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class Stopwatch {

    private long _timeStart;
    private Handler _handler;
    private Timer _timer;
    private boolean _started = false;
    private int _type;

    public Stopwatch(Handler handler, int type)
    {
        this._handler = handler;
        this._type = type;
    }

    public void start()
    {
        if(!IsStarted()) {
            this._timer = new Timer();
            this._timeStart = System.currentTimeMillis();
            this._started = true;
            final int type = this._type;
            this._timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    _handler.obtainMessage(type).sendToTarget();
                }
            }, 0, 10);
        }
        else
        {
            stop();
        }
    }

    public boolean IsStarted()
    {
        return _started;
    }

    public void stop()
    {
        this._timer.cancel();
        this._started = false;
    }

    public String elapsed()
    {
        long millisUntilFinished = System.currentTimeMillis() - _timeStart;
        long m = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished);
        long s = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished - m*60*1000);
        long ms = millisUntilFinished - m*60*1000 - s*1000;
        return String.format("%02d:%02d.%03d", m,s,ms);
    }

}
