/***********************
 Bluetooth test program
***********************/

#include <SoftwareSerial.h>

int BluetoothTx = 2;
int BluetoothRx = 3;

// Output Pins
int OUT1 = 8;
int OUT2 = 9;
int OUT3 = 10;

SoftwareSerial bluetooth(BluetoothTx, BluetoothRx); // RX, TX

void setup() 
{
  Serial.begin(9600);
  bluetooth.begin(9600);
  
  pinMode(3,INPUT);
  
  pinMode(OUT1, OUTPUT);
  pinMode(OUT2, OUTPUT);
  pinMode(OUT1, OUTPUT);
  
  // Start State
  digitalWrite(OUT1, LOW);
  digitalWrite(OUT2, LOW);
  digitalWrite(OUT3, LOW);
}

void loop()
{

  if( bluetooth.available() )
  {
    int first = bluetooth.read();
    
    // Z Positive
    if (first == 241) {
        digitalWrite(OUT1, LOW);
        digitalWrite(OUT2, LOW);
        digitalWrite(OUT3, HIGH);
    }
    // Z Negative
    else if (first == 242) {
        digitalWrite(OUT1, LOW);
        digitalWrite(OUT2, HIGH);
        digitalWrite(OUT3, LOW);
    }
    // Y Positive
    else if (first == 243) {
        digitalWrite(OUT1, LOW);
        digitalWrite(OUT2, HIGH);
        digitalWrite(OUT3, HIGH);
    }
    // Y Negative
    else if (first == 244) {
        digitalWrite(OUT1, HIGH);
        digitalWrite(OUT2, LOW);
        digitalWrite(OUT3, LOW);
    }
    // X Positive
    else if (first == 245) {
        digitalWrite(OUT1, HIGH);
        digitalWrite(OUT2, LOW);
        digitalWrite(OUT3, HIGH);
    }
    // X Negative
    else if (first == 246) {
        digitalWrite(OUT1, HIGH);
        digitalWrite(OUT2, HIGH);
        digitalWrite(OUT3, LOW);
    }
    // Start Game
    else if (first == 247) {
      
        digitalWrite(OUT1, HIGH);
        digitalWrite(OUT2, HIGH);
        digitalWrite(OUT3, HIGH);
        
        delay(100);
        
        // Stop resetting
        digitalWrite(OUT1, LOW);
        digitalWrite(OUT2, LOW);
        digitalWrite(OUT3, LOW);
    }
    
    Serial.println(first);
  }
}
