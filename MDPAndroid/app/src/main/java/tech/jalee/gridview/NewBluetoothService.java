package tech.jalee.gridview;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.testapp4.Chat;
import com.example.testapp4.MainActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.example.testapp4.MainActivity.MESSAGE_STATE_CHANGE;

public class NewBluetoothService {

    private static final String TAG = "NewBluetoothService";
    private static final UUID UUID_STRING = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final BluetoothAdapter mAdapter;
    private final List<Handler> mHandlers;
    private final Context mContext;

    private boolean activeConnection = false;
    private boolean isConnecting = false;

    private static NewBluetoothService _instance = null;

    public String deviceName;

    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;

    private BluetoothDevice mPreviousDevice;

    private BluetoothDevice remoteDevice;

    private int mState;

    public static final int STATE_NONE = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;

    private interface MessageConstants {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;
    }

    public static NewBluetoothService getInstance(Context context)
    {
        if(_instance == null)
            _instance = new NewBluetoothService(context);
        return _instance;
    }

    private NewBluetoothService(Context context)
    {
        this.mContext = context;
        this.mHandlers = new ArrayList<Handler>();
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
    }

    public void set(BluetoothDevice device)
    {
        this.remoteDevice = device;
    }

    private synchronized void setState(int state)
    {
        mState = state;
        for(Handler handler : mHandlers)
            handler.obtainMessage(MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    public void addObserver(Handler handler)
    {
        if(!this.mHandlers.contains(handler))
            this.mHandlers.add(handler);
    }

    public boolean removeObserver(Handler handler)
    {
        if(this.mHandlers.contains(handler))
            return this.mHandlers.remove(handler);
        return false;
    }

    public synchronized int getState()
    {
        return mState;
    }

    public synchronized void start()
    {
        if (mConnectThread != null && isConnecting == false)
        {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null)
        {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
    }

    public void writeFastestPath()
    {
        // Position pos = Map.getInstance().getWaypoint();
        ConnectedThread r;
        synchronized (this)
        {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        try {
            r.write("ARD:F|".getBytes());
            r.write("ALG:F|".getBytes());
        } catch (Exception e)
        {
            Log.e(TAG, e.getLocalizedMessage());
        }

    }

    public void writeImageRec()
    {
        ConnectedThread r;
        synchronized (this)
        {
            if(mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        try {
            r.write("ARD:E|".getBytes());
            r.write("ALG:I|".getBytes());
        } catch (Exception e)
        {
            Log.e(TAG, e.getLocalizedMessage());
        }
    }


    private class ConnectedThread extends Thread
    {
        private final BluetoothSocket mmSocket;
        private final OutputStream mmOutStream;
        private final InputStream mmInputStream;
        private byte[] mmBuffer;

        public ConnectedThread(BluetoothSocket socket, String socketType)
        {
            mmSocket = socket;
            OutputStream tmpOut = null;
            InputStream tmpIn = null;
            try
            {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
                deviceName = mmSocket.getRemoteDevice().getName();
            }

            catch (IOException e)
            {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmOutStream = tmpOut;
            mmInputStream = tmpIn;
            activeConnection = true;
        }

        public void run()
        {
            mmBuffer = new byte[1024];
            int numBytes;
            while(mmSocket.isConnected())
            {
                try {
                    // Read from the InputStream.
                    numBytes = mmInputStream.read(mmBuffer);
                    // Send the obtained bytes to the UI activity.
                    for(Handler handler : mHandlers) {
                        Message readMsg = handler.obtainMessage(
                                Chat.MESSAGE_READ, numBytes, -1,
                                mmBuffer);
                        readMsg.sendToTarget();
                    }
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        public void write(byte[] buffer)
        {
            try
            {
                mmOutStream.write(buffer);
            }
            catch (IOException e)
            {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel()
        {
            try
            {
                mmSocket.close();
            }

            catch (IOException e)
            {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    private class ConnectThread extends Thread
    {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private String mSocketType;

        public ConnectThread(BluetoothDevice device)
        {
            mmDevice = device;
            BluetoothSocket tmp = null;
            mSocketType = "Secure";

            // another option createInsecureRfcommSocketToServiceRecord
            try
            {
                tmp = device.createRfcommSocketToServiceRecord(UUID_STRING);
            }

            catch (IOException e)
            {
                Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e);
            }

        /*
        Method m = null;
        try {
            m = mmDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
            tmp = (BluetoothSocket) m.invoke(mmDevice, 1);

        } catch (Exception e) {

            e.printStackTrace();
        }*/


            mmSocket = tmp;
        }

        public void run()
        {
            Log.i(TAG, "BEGIN mConnectThread SocketType:" + mSocketType);
            setName("ConnectThread" + mSocketType);

            mAdapter.cancelDiscovery();

            try {
                mmSocket.connect();
                Log.i(TAG, "ConnectThread running");
            }
            catch (Exception e)
            {
                try
                {
                    mmSocket.close();
                }
                catch (Exception e2)
                {
                    Log.e(TAG, "unable to close() " + mSocketType + " socket during connection failure", e2);
                }

                connectionFailed();
                return;
            }

            synchronized (NewBluetoothService.this)
            {
                mConnectThread = null;
            }

            connected(mmSocket, mmDevice, mSocketType);
        }

        public void cancel()
        {
            try
            {
                mmSocket.close();
            }

            catch (IOException e)
            {
                Log.e(TAG, "close() of connect " + mSocketType + " socket failed", e);
            }
        }
    }

    public synchronized void connect()
    {
        connect(remoteDevice);
    }

    public synchronized void connect(BluetoothDevice device)
    {
        if (mState == STATE_CONNECTING)
        {
            if (mConnectThread != null)
            {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        if (mConnectedThread != null)
        {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        mConnectThread = new ConnectThread(device);
        mConnectThread.start();


        setState(STATE_CONNECTING);
    }

    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device, final String socketType)
    {
        if (mConnectThread != null)
        {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null)
        {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        isConnecting = false;

        mConnectedThread = new ConnectedThread(socket, socketType);
        mConnectedThread.start();
        for(Handler handler : mHandlers) {
            Message msg = handler.obtainMessage(MainActivity.MESSAGE_DEVICE_NAME);
            Bundle bundle = new Bundle();
            bundle.putString(MainActivity.DEVICE_NAME, device.getName());
            msg.setData(bundle);
            msg.sendToTarget();
        }
        mPreviousDevice = device;
        setState(STATE_CONNECTED);
    }

    /**
     * Reconnection code to reattempt reconnect when state changes (Thursday 12:06)
     */
    public synchronized void reconnect()
    {
        if(mPreviousDevice!=null && !isConnecting) {
            isConnecting = true;
            connect(mPreviousDevice);
        }
    }

    public synchronized void stop()
    {
        if (mConnectThread != null)
        {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null)
        {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(STATE_NONE);
        activeConnection = false;
    }

    public void write(byte[] out)
    {
        ConnectedThread r;
        synchronized (this)
        {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        r.write(out);
    }

    public boolean isActiveConnection()
    {
        return activeConnection;
    }

    private void connectionFailed()
    {
        this.activeConnection = false;
        for(Handler handler : mHandlers) {
            Message msg = handler.obtainMessage(MainActivity.MESSAGE_FAIL_TO_CONNECT);
            msg.sendToTarget();
        }
        NewBluetoothService.this.start();
        /// NEW CODE (Thursday 12:06)
        if(mPreviousDevice != null)
            reconnect();
    }

    public void sendMessage(byte[] message){
        try {
            if(mConnectedThread.mmSocket.isConnected())
                mConnectedThread.write(message);
        }catch(Exception e){
            Log.e(TAG, e.getLocalizedMessage());
            //connect first
        }
    }


}
