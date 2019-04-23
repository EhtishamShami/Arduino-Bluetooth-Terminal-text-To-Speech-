package com.example.danyal.bluetoothhc05;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.UUID;

public class ledControl extends AppCompatActivity implements TextToSpeech.OnInitListener {

    Button btn1, btn2, btn3, btn4, btn5, btnDis;
    String address = null;
    TextView lumn;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent newint = getIntent();
        address = newint.getStringExtra(DeviceList.EXTRA_ADDRESS);

        setContentView(R.layout.activity_led_control);

        btn1 = (Button) findViewById(R.id.button2);
        btn2 = (Button) findViewById(R.id.button3);
        btn3 = (Button) findViewById(R.id.button5);
        btn4 = (Button) findViewById(R.id.button6);
        btn5 = (Button) findViewById(R.id.button7);
        btnDis = (Button) findViewById(R.id.button4);
        lumn = (TextView) findViewById(R.id.textView2);

        new ConnectBT().execute();

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSignal("1");
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSignal("2");
            }
        });

        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSignal("3");
            }
        });

        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSignal("4");
            }
        });

        btn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSignal("5");
            }
        });

        btnDis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Disconnect();
            }
        });

    }

    private void sendSignal(String number) {
        if (btSocket != null) {
            try {
                btSocket.getOutputStream().write(number.toString().getBytes());
            } catch (IOException e) {
                msg("Error");
            }
        }
    }

    private void Disconnect() {
        if (btSocket != null) {
            try {
                btSocket.close();
            } catch (IOException e) {
                msg("Error");
            }
        }

        finish();
    }

    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    void beginListenForData() {

        final Handler handler = new Handler();
        final boolean stopThread = false;
        final TextToSpeech tts = new TextToSpeech(getApplicationContext(), this);
        tts.setLanguage(Locale.US);
        Thread thread = new Thread(new Runnable() {
            public void run() {
                StringBuilder readMessage = new StringBuilder("");
                while (!Thread.currentThread().isInterrupted() && !stopThread) {
                    try {
                        byte[] buffer = new byte[1];
                        int bytes;
                        InputStream inFromServer = btSocket.getInputStream();
                        bytes = inFromServer.read(buffer, 0, 1);
                        String read = new String(buffer, 0, 1);
                        readMessage.append(read);
                        if(read.contains(".")){
                            readMessage.setLength(0);
                            tts.speak(readMessage.toString(), TextToSpeech.QUEUE_ADD, null);
                        }
//                        if (buffer[0] == '}') {
//                            bytes = inFromServer.read(buffer, 0, 1);
//
//                            readMessage.append(read);
//                            String readMessage1 = new String(buffer,
//                                    0, bytes, "US-ASCII");
//                            Log.e("bytes ",
//                                    Integer.toString(bytes));
//
//                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


            }
        }
        );

        thread.start();
    }

    @Override
    public void onInit(int i) {

    }


    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean ConnectSuccess = true;

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(ledControl.this, "Connecting...", "Please Wait!!!");
        }

        @Override
        protected Void doInBackground(Void... devices) {
            try {
                if (btSocket == null || !isBtConnected) {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();
                }
            } catch (IOException e) {
                ConnectSuccess = false;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (!ConnectSuccess) {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            } else {
                msg("Connected");
                isBtConnected = true;

                beginListenForData();
//                byte[] buffer = new byte[1024];
//                int bytes;
//                try {
//                    InputStream inFromServer = btSocket.getInputStream();
//                    bytes = inFromServer.read(buffer);
//                    String readMessage = new String(buffer, 0, bytes);
//                    Log.d("tag", "Message :: " + readMessage);
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }

            }

            progress.dismiss();
        }
    }
}
