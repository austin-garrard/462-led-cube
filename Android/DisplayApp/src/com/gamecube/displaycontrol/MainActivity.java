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

package com.gamecube.displaycontrol;

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
import android.widget.ToggleButton;
import android.widget.Toast;
import android.graphics.PorterDuff;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends Activity {
  TextView myLabel;
  EditText myTextbox;
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
  private int current_layer = 1;
  
  boolean[][][] LEDstates = new boolean[8][8][8];
  
  LEDCube LEDcube = new LEDCube(8);

  // Update cube with selected LEDs
  void updateCube() {
	  
	  RelativeLayout rootLayout = (RelativeLayout) findViewById(R.id.root);
	  int count = rootLayout.getChildCount();
	    {
		  for (int i = 0; i <= count; i++) {
			    View v = rootLayout.getChildAt(i);
			    if (v instanceof ToggleButton) {
			    	//
			    	if (((ToggleButton) v).isChecked()) {
			    		
	    		    	String name = getResources().getResourceName(v.getId());
	    		        v.setOnClickListener(OnClickMaker.getOnClick());
	    		        String[] parts = name.split("LED");
	    		        
	    		        // Pulling Coordinates from the Name
	    		        int x = Character.getNumericValue(parts[1].charAt(0))-1;
	    		        int y = Character.getNumericValue(parts[1].charAt(1))-1;
	
	    		        LEDcube.setLED(x, y, current_layer-1);
			    	}
			    }
			}
	     }
  }
  
  // Save LED states and load new layer
  void change_layer(int new_layer) {
	  
	  RelativeLayout rootLayout = (RelativeLayout) findViewById(R.id.root);
	  int count = rootLayout.getChildCount();
	    {
		  for (int i = 0; i <= count; i++) {
			    View v = rootLayout.getChildAt(i);
			    
			    if (v instanceof ToggleButton) {
			    	
			    	String name = getResources().getResourceName(v.getId());
    		        String[] parts = name.split("LED");
    		        
    		        // Pulling Coordinates from the Name
    		        int x = Character.getNumericValue(parts[1].charAt(0))-1;
    		        int y = Character.getNumericValue(parts[1].charAt(1))-1;
			    	
			    	// Save LED states
			    	if (((ToggleButton) v).isChecked()) {
			    		
	    		        LEDstates[x][y][current_layer-1] = true;
	    		        //LEDcube.setLED(x, y, current_layer-1);
			    	}
			    	
			    	// Load new LED states
			    	boolean state = LEDstates[x][y][new_layer-1];
			    	((ToggleButton) v).setChecked(state);
			    	if (state)
			    		v.getBackground().setColorFilter(0xFF00FF00, PorterDuff.Mode.MULTIPLY);
			    	else
			    		v.getBackground().setColorFilter(0xFFFFFFFF, PorterDuff.Mode.MULTIPLY);
			    } 
			}
	     }
	    current_layer = new_layer;
  }
  
  // Save LED states to Cube
  void saveToCube() {
	  
	  for (int x = 0; x < 8; ++x) {
		  for (int y = 0; y < 8; ++y) {
			  for (int z = 0; z < 8; ++z) {
				  if( LEDstates[x][y][z] == true)
					  LEDcube.setLED(x, y, z);
				  else
					  LEDcube.clearLED(x, y, z);
			  }
		  }
	  }
  }
  
  // Highlights current layer button
  void update_layer_buttons() {
	  
	  RelativeLayout rootLayout = (RelativeLayout) findViewById(R.id.layer_buttons);
	  int count = rootLayout.getChildCount();
	  int layer = 1;
	    {
	    	  for (int i = 0; i <= count; i++) {
	    		    View v = rootLayout.getChildAt(i);
	    		    
	    		    if (v instanceof Button) {
	    		    	if (layer == current_layer)
	    		    		v.getBackground().setColorFilter(0xFF00FF00, PorterDuff.Mode.MULTIPLY);
	    		    	else
	    		    		v.getBackground().setColorFilter(0xFFFFFFFF, PorterDuff.Mode.MULTIPLY);
	    		    }
	    		    ++layer;
	    	  }
	    }
	  
  }
  
  // Load LED states
  void loadLEDstates(int i) {
	  
  }
  
  // Sets all LEDs back to OFF
  void resetLEDs() {
	  RelativeLayout rootLayout = (RelativeLayout) findViewById(R.id.root);
	  int count = rootLayout.getChildCount();
	  {
		  for (int i = 0; i <= count; i++) {
		    View v = rootLayout.getChildAt(i);
		    if (v instanceof ToggleButton) {
		    	((ToggleButton) v).setChecked(false);
		    }
		  }
	  }
  }
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Button sendButton = (Button)findViewById(R.id.send);
    Button clearCubeButton = (Button)findViewById(R.id.clearcube);
    Button layer1 = (Button)findViewById(R.id.layerbutton1);
    Button layer2 = (Button)findViewById(R.id.layerbutton2);
    Button layer3 = (Button)findViewById(R.id.layerbutton3);
    Button layer4 = (Button)findViewById(R.id.layerbutton4);
    Button layer5 = (Button)findViewById(R.id.layerbutton5);
    Button layer6 = (Button)findViewById(R.id.layerbutton6);
    Button layer7 = (Button)findViewById(R.id.layerbutton7);
    Button layer8 = (Button)findViewById(R.id.layerbutton8);
    
    myLabel = (TextView)findViewById(R.id.label);
    
    // Highlight Layer 1, start on this layer
    layer1.getBackground().setColorFilter(0xFF00FF00, PorterDuff.Mode.MULTIPLY);	
    
    RelativeLayout rootLayout = (RelativeLayout) findViewById(R.id.root);
    int count = rootLayout.getChildCount();
    {
  	  for (int i = 0; i <= count; i++) {
  		    View v = rootLayout.getChildAt(i);
  		    
  		    if (v instanceof ToggleButton) {
  		    	
  		        v.setOnClickListener(OnClickMaker.getOnClick());
  		    }
  		}
    }
    
    // Open Bluetooth on load
    try {
        findBT();
        openBT();
      }
    catch (IOException ex) { }
    
    // Layer Buttons
    layer1.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
        	if (current_layer != 1) {
	        	change_layer(1);
	        	update_layer_buttons();
        	}
        }
      });
    layer2.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
        	if (current_layer != 2) {
	        	change_layer(2);
	        	update_layer_buttons();
        	}
        }
      });
    layer3.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
        	if (current_layer != 3) {
	        	change_layer(3);
	        	update_layer_buttons();
        	}
        }
      });
    layer4.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
        	if (current_layer != 4) {
	        	change_layer(4);
	        	update_layer_buttons();
        	}
        }
      });
    layer4.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
        	if (current_layer != 4) {
	        	change_layer(4);
	        	update_layer_buttons();
        	}
        }
      });
    layer5.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
        	if (current_layer != 5) {
	        	change_layer(5);
	        	update_layer_buttons();
        	}
        }
      });
    layer6.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
        	if (current_layer != 6) {
	        	change_layer(6);
	        	update_layer_buttons();
        	}
        }
      });
    layer7.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
        	if (current_layer != 7) {
	        	change_layer(7);
	        	update_layer_buttons();
        	}
        }
      });
    layer8.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
        	if (current_layer != 8) {
	        	change_layer(8);
	        	update_layer_buttons();
        	}
        }
      });
    
    // Clear Layer Button
    
    //Send Button
    sendButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        try {
        	//LEDcube.clear();
        	change_layer(1);
        	saveToCube();
        	//updateCube();
        	sendData();
        	
        	for (int i = 0; i < 50; ++i) {
        		saveToCube();
        		sendLastByte();
        	}
        	
        	//sendLastByte();
        }
        catch (IOException ex) {
            showMessage("SEND FAILED");
        }
      }
    });

    //Close button
    clearCubeButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
    	  
    	  
    	  /*
        try {
        	
          closeBT();
        }
        catch (IOException ex) { }
        */
      }
    });
  }

  void findBT() {
	    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	    if(mBluetoothAdapter == null) {
	      myLabel.setText("No bluetooth adapter available");
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
	          myLabel.setText("Arduino Uno Bluetooth Device Found");
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
	    myLabel.setText("Bluetooth Opened to Arduino");
	  }

  void sendData() throws IOException {
	try {
			
	        mmOutputStream.write(LEDcube.cube);
	        //byte[] stream = new byte[65];
	        //mmOutputStream.write(stream);
	        
	        // Wait moment then send last byte
	        
	        //myLabel.setText(LEDcube.cube.length);
	        //Toast msg = Toast.makeText(getBaseContext(),
	        //        "Data sent ", (Toast.LENGTH_LONG)/160);
	        
    } catch (IOException e) {
        throw e;
    }
  }
  
  void sendLastByte() throws IOException {
		try {
		        byte[] stream = {(byte)0xFF};
		        mmOutputStream.write(stream);
		        
	    } catch (IOException e) {
	        throw e;
	    }
	  }

  void closeBT() throws IOException {
    stopWorker = true;
    mmOutputStream.close();
    mmInputStream.close();
    mmSocket.close();
    myLabel.setText("Bluetooth Closed");
  }

  private void showMessage(String theMsg) {
        Toast msg = Toast.makeText(getBaseContext(),
                theMsg, (Toast.LENGTH_LONG)/160);
        msg.show();
    }
}
