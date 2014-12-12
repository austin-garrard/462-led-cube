/*
package com.example.myfirstapp;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
*/

package com.gamecube.snakegame;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.EditText;  
import android.widget.Button;
import android.widget.Toast;
import android.graphics.Color;
import android.graphics.PorterDuff;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends Activity {
  //TextView myLabel;
  //EditText myTextbox;
  BluetoothAdapter mBluetoothAdapter;
  BluetoothSocket mmSocket;
  BluetoothDevice mmDevice;
  OutputStream mmOutputStream;
  InputStream mmInputStream;
  Thread workerThread;
  byte[] readBuffer;
  int readBufferPosition;
  int counter;
  volatile boolean stopWorker;
  private ImageButton Xpos, Xneg, Ypos, Yneg, Zpos, Zneg;
  private int rotation = 35;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    
    RelativeLayout main = (RelativeLayout)findViewById(R.id.mainlayout);
    main.setBackgroundColor(Color.argb(255, 250, 250, 210));

    Button startButton = (Button)findViewById(R.id.start);
    startButton.getBackground().setColorFilter(0xFF00FF00, PorterDuff.Mode.MULTIPLY);
    
    Ypos = (ImageButton) findViewById(R.id.y_pos_button);
    Ypos.setRotation(- rotation);
    Yneg = (ImageButton) findViewById(R.id.y_neg_button);
    Yneg.setRotation(180 - rotation);
    Xneg = (ImageButton) findViewById(R.id.x_neg_button);
    Xneg.setRotation(rotation);
    Xpos = (ImageButton) findViewById(R.id.x_pos_button);
    Xpos.setRotation(rotation + 180);
    Zpos = (ImageButton) findViewById(R.id.z_pos_button);
    Zpos.setRotation(-90);
    Zneg = (ImageButton) findViewById(R.id.z_neg_button);
    Zneg.setRotation(90);
    
    try {
        findBT();
        openBT();
      }
      catch (IOException ex) { }
    
    
    // Snake Control Buttons
    Zpos.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
        	
        	try {
                sendData((byte)0xF1);
                sendData((byte)0xF1);
                sendData((byte)0xF1);
                sendData((byte)0xF1);
                sendData((byte)0xF1);
              }
              catch (IOException ex) {
                  showMessage("SEND FAILED");
              }
              
        }
      });
    Zneg.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
        	
        	try {
                sendData((byte)0xF2);
              }
              catch (IOException ex) {
                  showMessage("SEND FAILED");
              }
              
        }
      });
    Ypos.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
        	
        	try {
        		// Send X pos
                //sendData((byte)0xF3);
                
                sendData((byte)0xF5);
              }
              catch (IOException ex) {
                  showMessage("SEND FAILED");
              }
              
        }
      });
    Yneg.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
        	
        	try {
        		// Send X neg
                //sendData((byte)0xF4);
                
                sendData((byte)0xF6);
              }
              catch (IOException ex) {
                  showMessage("SEND FAILED");
              }
        }
      });
    Xpos.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
        	
        	try {
        		// Send Y pos
                //sendData((byte)0xF5);
                
                sendData((byte)0xF3);
              }
              catch (IOException ex) {
                  showMessage("SEND FAILED");
              }
              
        }
      });
    Xneg.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
        	
        	try {
        		// Send Y neg
                //sendData((byte)0xF6);
                
                sendData((byte)0xF4);
              }
              catch (IOException ex) {
                  showMessage("SEND FAILED");
              }
              
        }
      });
    
    //Start Button
    startButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        try {
        	sendData((byte)0xF7);
        }
        catch (IOException ex) {
            showMessage("SEND FAILED");
        }
      }
    });
  }
     
  void findBT() {
    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    if(mBluetoothAdapter == null) {
      //myLabel.setText("No bluetooth adapter available");
    }

    if(!mBluetoothAdapter.isEnabled()) {
      Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
      startActivityForResult(enableBluetooth, 0);
    }
    
    Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
    if(pairedDevices.size() > 0) {
      for(BluetoothDevice device : pairedDevices) {
        if(device.getName().contains("HC-06")) {
          mmDevice = device;
          //myLabel.setText("Arduino Uno Bluetooth Device Found");
          break;
        }
      }
    }
    /*
    Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
    mmDevice = mBluetoothAdapter.getRemoteDevice("24:0a:64:2c:f3:9a");
    if (pairedDevices.contains(mmDevice))
       {
           Log.d("Bluetooth Device Found, address: ", mmDevice.getAddress() );
           Log.d("ArduinoBT", "BT is paired");
       }
       */
    

  }

  void openBT() throws IOException {
    UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard //SerialPortService ID
    mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);    
    
    mmSocket.connect();
    
    mmOutputStream = mmSocket.getOutputStream();
    
    //mmInputStream = mmSocket.getInputStream();
    //beginListenForData();
    //myLabel.setText("Bluetooth Opened to Arduino");
  }

  void sendData(byte data) throws IOException {
    try {
        
        mmOutputStream.write(data);
    } catch (IOException e) {
        throw e;
    }
  }

  void closeBT() throws IOException {
    stopWorker = true;
    mmOutputStream.close();
    mmInputStream.close();
    mmSocket.close();
    //myLabel.setText("Bluetooth Closed");
  }

  private void showMessage(String theMsg) {
        Toast msg = Toast.makeText(getBaseContext(),
                theMsg, (Toast.LENGTH_LONG)/160);
        msg.show();
    }
}
