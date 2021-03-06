package com.dashhud.crigerkwok.dashboardhud;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.UUID;


public class BT_Service implements Serializable{
    private static final String TAG = "Bluetooth Service";
    private static final String app_name = "Automotive HUD";
    private static final UUID app_uuid = UUID.fromString("00101101-0000-1000-8000-A0803F9B34FB");

    private BluetoothAdapter bt_adapter;
    Context my_context;

    private AcceptThread my_accept_thread;
    private ConnectThread my_connect_thread;
    private BluetoothDevice my_device;
    private UUID device_UUID;
    ProgressDialog my_progress_dialog;
    private ConnectedThread my_connected_thread;

    Handler handler;

    public BT_Service(Context context)
    {
        my_context = context;
        bt_adapter = BluetoothAdapter.getDefaultAdapter();
        //start();
    }

    //Start the service. Start AcceptThread to begin a session in listening mode. Called by onResume
    public synchronized void start()
    {
        Log.d(TAG, "start");

        //Cancel any thread attempting to make a connection
        if(my_connect_thread != null)
        {
            my_connect_thread.cancel();
            my_connect_thread = null;
        }
        if(my_accept_thread == null)
        {

            //my_accept_thread = new AcceptThread();
            //my_accept_thread.start();
        }
    }

    public void acceptClient()
    {
        Log.d(TAG, "acceptClient: Started");
        //initiate dialog
        my_progress_dialog = ProgressDialog.show(my_context, "Accepting Bluetooth", "Wait for device...",true);

        my_accept_thread = new AcceptThread();
        my_accept_thread.start();
    }

    //AcceptThread starts and sits waiting for a connection.
    //Then ConnectThread starts and attempts to make a connection with other devices
    public void startClient(BluetoothDevice device, UUID uuid)
    {
        Log.d(TAG, "startClient: Started");
        //initiate dialog
        my_progress_dialog = ProgressDialog.show(my_context, "Connecting Bluetooth", "Please Wait...",true);

        my_connect_thread = new ConnectThread(device, uuid);
        my_connect_thread.start();
    }

    //thread that sits and waits for a connection
    private class AcceptThread extends Thread
    {
        //local server socket
        private final BluetoothServerSocket my_server_socket;

        public AcceptThread()
        {
            BluetoothServerSocket tmp = null;

            //create new listening server socket
            try {
                tmp = bt_adapter.listenUsingInsecureRfcommWithServiceRecord(app_name, app_uuid);

                Log.d(TAG, "AcceptThread: Setting up server using: " + app_uuid);
            }
            catch (IOException e) {
                Log.e(TAG, "Socket's listen() method failed", e);
            }
            my_server_socket = tmp;
        }

        public void run()
        {
            Log.d(TAG, "run: AcceptThread running");
            BluetoothSocket socket = null;

            while(true) {
                try {
                    //This is a blocking call and will only return on a successful connection or exception
                    Log.d(TAG, "run: RFCOM server socket start...");
                    socket = my_server_socket.accept();
                    Log.d(TAG, "run: RFCOM server socket accepted connection");
                } catch (IOException e) {
                    Log.e(TAG, "AcceptThread: IOException: " + e.getMessage());
                    break;
                }
                if (socket != null) {
                    //A connection was accepted. Perform work associated with the connection in a separate thread
                    connected(socket, my_device);
                    try {
                        my_server_socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
            Log.i(TAG, "END AcceptThread");
        }
        public void cancel()
        {
            Log.d(TAG, "cancel: Cancelling AcceptThread");
            try{
                my_server_socket.close();
            }
            catch (IOException e) {
                Log.e(TAG, "cancel: Close of AcceptThread ServerSocket failed " + e.getMessage());
            }
        }
    }


    //Thread runs while attempting to make outgoing connection with a device.
    private class ConnectThread extends Thread
    {
        private BluetoothSocket my_socket;

        public ConnectThread(BluetoothDevice device, UUID uuid)
        {
            Log.d(TAG, "ConnectThread: started");
            my_device = device;
            device_UUID = uuid;
            BluetoothSocket tmp = null;

            try{
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(app_uuid);
                Log.e(TAG, "Socket's create() method success");
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            my_socket = tmp;
        }
        public void run()
        {
            //Always cancel discovery because it will slow down a connection
            bt_adapter.cancelDiscovery();
            //Get a socket for a connection with the given device
            try
            {
                my_socket.connect();
            }
            catch (IOException connect_e) {
                try{
                    my_progress_dialog.dismiss();
                    my_socket.close();
                    Log.e(TAG, "ConnectThread: Socket.close() called ");
                } catch (IOException close_e){
                    Log.e(TAG, "Could not close the client socket", close_e);
                    my_progress_dialog.dismiss();
                }
                Log.e(TAG, "ConnectThread: Could not create socket " + connect_e.getMessage());
                my_progress_dialog.dismiss();
                return;
            }
            connected(my_socket, my_device);
        }

        public void cancel()
        {
            Log.d(TAG, "cancel: Cancelling AcceptThread");
            try{
                my_progress_dialog.dismiss();
                my_socket.close();
            }
            catch (IOException e) {
                Log.e(TAG, "cancel: Close of AcceptThread ServerSocket failed " + e.getMessage());
                my_progress_dialog.dismiss();
            }
        }
    }


    private class ConnectedThread extends Thread
    {
        private final BluetoothSocket my_socket;
        private final InputStream my_in_stream;
        private final OutputStream my_out_stream;

        public ConnectedThread(BluetoothSocket socket)
        {
            Log.d(TAG, "ConnectedThread: Starting");
            my_socket = socket;
            InputStream tmp_in = null;
            OutputStream tmp_out = null;

            //dismiss the processdialog when connection is established
            try {
                my_progress_dialog.dismiss();
            }catch (NullPointerException e) {
                e.printStackTrace();
            }

            try
            {
                tmp_in = my_socket.getInputStream();
                tmp_out = my_socket.getOutputStream();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            my_in_stream = tmp_in;
            my_out_stream = tmp_out;
        }

        public void run()
        {
            //buffer store for the stream
            byte[] buffer = new byte[1024];
            //bytes returned from read()
            int bytes;
            while(true)
            {
                try {
                    bytes = my_in_stream.read(buffer);
                    String incoming_message = new String(buffer, 0, bytes);
                    Log.d(TAG, "InputStream: " + incoming_message);
                }
                catch (IOException e)
                {
                    Log.d(TAG, "write: Error reading input stream. " + e.getMessage());
                    break;
                }
            }
        }

        //Call this from main activity to send data to the remote device
        public void write(byte[] bytes)
        {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: Writing to outputstream: " + text);
            try
            {
                my_out_stream.write(bytes);

                /*// Share the sent message with the UI activity.
                Message writtenMsg = mHandler.obtainMessage(
                        MessageConstants.MESSAGE_WRITE, -1, -1, mmBuffer);
                writtenMsg.sendToTarget();*/
            }
            catch (IOException e)
            {
                Log.d(TAG, "write: Error writing to outputstream. " + e.getMessage());
            }
        }

        //Call this from main activity to shutdown the connection
        public void cancel()
        {
            try{
                my_socket.close();
            }
            catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }

    private void connected(BluetoothSocket my_socket, BluetoothDevice my_device)
    {
        Log.d(TAG, "connected: Starting.");

        //Start the thread to manage the connection and perform transmissions
        my_connected_thread = new ConnectedThread(my_socket);
        my_connected_thread.start();
    }

    //Write to ConnectedThread in an unsynchronized manner
    public void write(byte[] out)
    {
        //Log.d(TAG, "write: Write called");
        my_connected_thread.write(out);
    }
}

/*public class BT_Service {

    private OutputStream outputStream;
    private InputStream inStream;

    private void init() throws IOException {
        BluetoothAdapter blueAdapter = BluetoothAdapter.getDefaultAdapter();
        if (blueAdapter != null) {
            if (blueAdapter.isEnabled()) {
                Set<BluetoothDevice> bondedDevices = blueAdapter.getBondedDevices();

                if (bondedDevices.size() > 0) {
                    Object[] devices = (Object[]) bondedDevices.toArray();
                    BluetoothDevice device = (BluetoothDevice) devices[position];
                    ParcelUuid[] uuids = device.getUuids();
                    BluetoothSocket socket = device.createRfcommSocketToServiceRecord(uuids[0].getUuid());
                    socket.connect();
                    outputStream = socket.getOutputStream();
                    inStream = socket.getInputStream();
                }

                Log.e("error", "No appropriate paired devices.");
            } else {
                Log.e("error", "Bluetooth is disabled.");
            }
        }
    }

    public void write(String s) throws IOException {
        outputStream.write(s.getBytes());
    }

    public void run() {
        final int BUFFER_SIZE = 1024;
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytes = 0;
        int b = BUFFER_SIZE;

        while (true) {
            try {
                bytes = inStream.read(buffer, bytes, BUFFER_SIZE - bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}*/














