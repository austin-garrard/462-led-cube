#include <SoftwareSerial.h>

int BluetoothTx = 2;
int BluetoothRx = 3;
SoftwareSerial bluetooth(BluetoothTx, BluetoothRx); // RX, TX


//signal for anodes w/in layer
#define CLOCK 11 //74HC595 pin 11
#define LATCH 12  //74HC595 pin 12
#define DATA 13  //74HC595 pin 14

//signal for layer selector
#define CLOCK1 5 //74HC595 pin 11
#define LATCH1 6  //74HC595 pin 12
#define DATA1 7  //74HC595 pin 14

//display data
#define DIM 8
volatile byte data[DIM*DIM];
volatile int layer_iter;

//pattern selector
byte select;

//test_display variables
volatile byte count;
volatile byte count1;
volatile byte dir;


void setup()
{
  //test_display variables
  count = 0;
  count1 = 0;
  dir = 0;
  
  
  /**CONFIGURE PINS**/
  pinMode(LATCH, OUTPUT);
  pinMode(DATA, OUTPUT);
  pinMode(CLOCK, OUTPUT);
  pinMode(LATCH1, OUTPUT);
  pinMode(DATA1, OUTPUT);
  pinMode(CLOCK1, OUTPUT);
  
  /**CONFIGURE DISPLAY DATA**/
  int i, x;
  for(x = 0; x < DIM*DIM; x++) {
      data[x] = 0;
  }
  
  layer_iter = 0;
  
  /**CONFIGURE TIMERS**/
  cli();//disable interrupts
  //set timer1 interrupt at 520Hz
  TCCR1A = 0;// set entire TCCR1A register to 0
  TCCR1B = 0;// same for TCCR1B
  TCNT1  = 0;//initialize counter value to 0
  // set compare match register for 1hz increments
  OCR1A = 30;// = (16*10^6) / (10*1024) - 1 (must be <65536)
  // turn on CTC mode
  TCCR1B |= (1 << WGM12);
  // Set CS12 and CS10 bits for 1024 prescaler
  TCCR1B |= (1 << CS12) | (1 << CS10);  
  // enable timer compare interrupt
  //TIMSK1 |= (1 << OCIE1A);
  sei();//enable interrupts
  
  bluetooth.begin( 9600);
}


void display()
{
  switch(select) {
    case 1: test_display();break;
    //case 2: ...
  }
    
  
  //turn off all LED's
  digitalWrite(LATCH, LOW);
  int i;
  for(i = 0; i < DIM; i++)
    shiftOut(DATA, CLOCK, MSBFIRST, 0);
  digitalWrite(LATCH, HIGH);
  
  //select next layer
  layer_iter = (layer_iter==DIM-1 ? 0:layer_iter+1);
  digitalWrite(LATCH1, LOW);
  shiftOut(DATA1, CLOCK1, MSBFIRST, 0xFF & (1 << layer_iter));
  digitalWrite(LATCH1, HIGH);
  
  //display data
  digitalWrite(LATCH, LOW);
  for(i = 0; i < DIM; i++)
    shiftOut(DATA, CLOCK, MSBFIRST, data[layer_iter*8+i]);
  digitalWrite(LATCH, HIGH);
}

//display data @ 520Hz, update data @ 10Hz
ISR(TIMER1_COMPA_vect)
{
  display();
}

void loop()
{
  if(bluetooth.available()) {
    //wait for the message
    byte check = bluetooth.read();
    if(check == 65) {
      //read which pattern to display
      delay(100);
      select = bluetooth.read();
      
      //enable the loops
      cli();
      TIMSK1 |= (1 << OCIE1A);//enable timer interrupt
      sei(); 
    }
  }
  
}


void test_display()
{
  if(++count == 50) {
    if(++count1 == 9) {
      dir = (dir==0 ? 1:0);
      count1 = 0;
    } else {
      int x, i;
      if(dir) {
        for(x = 0; x < 8; x++) {
          for(i = 0; i < 8; i++) {
            if(i%2 == 0) 
              data[i*8+x] = (data[i*8+x] << 1) | 0x1;
            else
              data[i*8+x] = (data[i*8+x] >> 1) | 0x80;
          }
        }
      } else {
        for(x = 0; x < 8; x++) {
          for(i = 0; i < 8; i++) {
            if(i%2 == 0) 
              data[i*8+x] = (data[i*8+x] >> 1);
            else
              data[i*8+x] = (data[i*8+x] << 1);
          }
        }
      }
    }
    count = 0;
  }
}
