

#define CLOCK 11 //74HC595 pin 11
#define LATCH 12  //74HC595 pin 12
#define DATA 13  //74HC595 pin 14


//display data
byte data[2];
int count;
byte dir;
int iter;


void setup()
{
  /**CONFIGURE PINS**/
  pinMode(LATCH, OUTPUT);
  pinMode(DATA, OUTPUT);
  pinMode(CLOCK, OUTPUT);
  
  /**CONFIGURE DISPLAY DATA**/
  data[0] = 0xFF;
  data[1] = 0xFF;
  count = 0;
  dir = 0;
  iter = 0;
  
  /**CONFIGURE TIMERS**/
  cli();//disable interrupts
  
  //set timer0 interrupt at 2kHz
  TCCR0A = 0;// set entire TCCR2A register to 0
  TCCR0B = 0;// same for TCCR2B
  TCNT0  = 0;//initialize counter value to 0
  // set compare match register for 2khz increments
  OCR0A = 255;// = (16*10^6) / (2000*64) - 1 (must be <256)
  // turn on CTC mode
  TCCR0A |= (1 << WGM01);
  // Set CS01 and CS00 bits for 64 prescaler
  TCCR0B |= (1 << CS01) | (1 << CS00);   
  // enable timer compare interrupt
  TIMSK0 |= (1 << OCIE0A);
  
  //set timer1 interrupt at 10Hz
  TCCR1A = 0;// set entire TCCR1A register to 0
  TCCR1B = 0;// same for TCCR1B
  TCNT1  = 0;//initialize counter value to 0
  // set compare match register for 1hz increments
  OCR1A = 1562;// = (16*10^6) / (10*1024) - 1 (must be <65536)
  // turn on CTC mode
  TCCR1B |= (1 << WGM12);
  // Set CS12 and CS10 bits for 1024 prescaler
  TCCR1B |= (1 << CS12) | (1 << CS10);  
  // enable timer compare interrupt
  TIMSK1 |= (1 << OCIE1A);
  
  sei();//enable interrupts
}

//display the data @ 2kHz
ISR(TIMER0_COMPA_vect)
{
  digitalWrite(LATCH, LOW);
  
  byte mask = 1 << iter;
  iter = (iter==7 ? 0:iter+1);

  shiftOut(DATA, CLOCK, MSBFIRST, data[0]);
  shiftOut(DATA, CLOCK, MSBFIRST, data[1]); 

  digitalWrite(LATCH, HIGH);
}

//update the data @ 10Hz
ISR(TIMER1_COMPA_vect)
{
  
  
  if(++count == 9) {
    dir = (dir==0 ? 1:0);
    count = 0;
  } else {
    if(dir) {
      data[0] = (data[0] << 1) | 0x1;
      data[1] = (data[1] >> 1) | 0x80;
    } else {
      data[0] = data[0] >> 1;
      data[1] = data[1] << 1;
    }
  }
}

void loop()
{
  
}
