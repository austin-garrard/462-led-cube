//signal for anodes w/in layer
#define CLOCK 11 //74HC595 pin 11
#define LATCH 12  //74HC595 pin 12
#define DATA 13  //74HC595 pin 14

//signal for layer selector
#define CLOCK1 5 //74HC595 pin 11
#define LATCH1 6  //74HC595 pin 12
#define DATA1 7  //74HC595 pin 14

#define DATA2 8
#define DATA3 9 
#define DATA4 10 

//display data
#define DIM 8
volatile byte data[DIM*DIM];
volatile int layer_iter;
volatile int dir1, dir2, dir3, olddir1, olddir2, olddir3;
class LED
{
 public:
  LED() {};
  LED(int a, int b, int c)
     :x(a), y(b), z(c) {};
  int x, y, z; 
};

LED snake[50];
int snake_size;
LED oldLED;
LED apple;
boolean first, apple_ate, game_over;

//test_display variables
volatile byte count;
volatile byte count1;
volatile byte dir;

int index(int y, int z){return (z*8) + y;}
void setLED(int x, int y, int z) { data[index(y,z)] |= (0x1 << (x%8)); }
void clearLED(int x, int y, int z) { data[index(y,z)] &= ~(0x1 << (x%8)); }

void reset()
{
  if(!first)
  {
    int i;
  for (i = 0; i < snake_size; i++)
  {
    clearLED(snake[i].x, snake[i].y, snake[i].z);
  }
  clearLED(apple.x, apple.y, apple.z);
  
  }
  first = false;
  
  snake_size = 3;
  apple = LED(5,0,2);
  
  snake[0] = LED(0,0,2);
  snake[1] = LED(1,0,2);
  snake[2] = LED(2,0,2);
  apple_ate = false;
  olddir1 = HIGH;
  olddir2 = LOW;
  olddir3 = HIGH;
  game_over = false;
}
void setup()
{
  //test_display variables
  count = 0;
  count1 = 0;
  dir = 0;
  first = true;
  reset();
  /**CONFIGURE PINS**/
  pinMode(LATCH, OUTPUT);
  pinMode(DATA, OUTPUT);
  pinMode(CLOCK, OUTPUT);
  pinMode(LATCH1, OUTPUT);
  pinMode(DATA1, OUTPUT);
  pinMode(CLOCK1, OUTPUT);
  pinMode(DATA2, INPUT);
  pinMode(DATA3, INPUT);
  pinMode(DATA4, INPUT);
  
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
  TIMSK1 |= (1 << OCIE1A);
  sei();//enable interrupts
  //Serial.begin(9600);
  
}


void display()
{
  dir1 = digitalRead(DATA2);
  dir2 = digitalRead(DATA3);
  dir3 = digitalRead(DATA4);
  
  snake_display();
    
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
  
}

void snake_display()
{
  int maxY = 7; //rows being used, change it when you wire the whole cube
  int maxZ = 7;
  int maxX = 7;
  
  if (dir1 == LOW && dir2 == LOW && dir3 == LOW) //when the bluetooth sends 000, keep going in the same direction
        {
         dir1 = olddir1;
         dir2 = olddir2;
         dir3 = olddir3;  
        }
        else //otherwise save the new direction
        {
         olddir1 = dir1;
         olddir2 = dir2;
         olddir3 = dir3; 
        }
        
  if(++count == 200) {
    //Serial.println(dir1);
    //Serial.println(dir2);
    //Serial.println(dir3);
  
    if (game_over) reset();
    setLED(apple.x, apple.y, apple.z);     //apple LED
    if (!apple_ate) clearLED(oldLED.x, oldLED.y, oldLED.z); // if the snake is not growing, clear the tail LED
    
    int i;
    
    for (i = 0; i < snake_size; i++)
    {
      setLED(snake[i].x, snake[i].y, snake[i].z); //set all the leds of the snake
     if (i != snake_size-1 && snake[snake_size-1].x == snake[i].x && snake[snake_size-1].y == snake[i].y && snake[snake_size-1].z == snake[i].z)
    { // if the head is in the same spot as one of the other LEDs
      game_over = true;
      return;
    }
      
    }
    oldLED = snake[0]; //mark the tail LED
    apple_ate = false;
    if (snake[snake_size-1].x == apple.x && snake[snake_size-1].y == apple.y && snake[snake_size-1].z == apple.z)
    { //if the head LED is the same as the apple LED
      apple_ate = true;
      snake_size++; 
    }
    for (i = 0; i < snake_size; i++)
    {
      int newX;
      int newY;
      int newZ;
      
      
      if (i == snake_size-1) //creating the next head LED
      {
        
        switch (dir1)
      {
       case LOW:
        switch (dir2)
       {
        case LOW:
          switch (dir3)
          {
           case LOW:
            
            break;
           case HIGH:
           
             if (snake[i-1].z-1 < 0) //hitting a wall
             {
             game_over = true;
             return;
             }
             else{
             newZ = snake[i-1].z-1; 
             snake[i] = LED(snake[i-1].x, snake[i-1].y, newZ); //move forward
             }
            break; 
          }
          break;
        case HIGH:
          switch (dir3)
          {
           case LOW:
           if(snake[i-1].z+1 > maxZ)
           {
           game_over = true;
           return;
           }
           else
           {
            newZ = snake[i-1].z+1;
            snake[i] = LED(snake[i-1].x, snake[i-1].y, newZ);
           }
            break;
           case HIGH:
           if (snake[i-1].y+1 > maxY)
           {
           game_over = true;
           return;
           }
           else
           {
            newY = snake[i-1].y+1;
            snake[i] = LED(snake[i-1].x, newY, snake[i-1].z);
           }
            break; 
          }
          break;
       }
       break;
       case HIGH:
        switch (dir2)
       {
        case LOW:
          switch (dir3)
          {
           case LOW:
           if (snake[i-1].y-1 < 0)
           {
             game_over = true;
             return;
           }
           else
           {
            newY = snake[i-1].y-1;
            snake[i] = LED(snake[i-1].x, newY, snake[i-1].z);
           }
            break;
           case HIGH:
            if (snake[i-1].x-1 < 0)
            {
              game_over = true;
              return;
            }
            else
            {
            newX = snake[i-1].x-1;
            snake[i] = LED(newX, snake[i-1].y, snake[i-1].z);
            }
            break; 
          }
          break;
        case HIGH:
          switch (dir3)
          {
           case LOW:
           if (snake[i-1].x+1 > maxX)
           {
           game_over = true;
           return;
           }
           else
           {
            newX = snake[i-1].x+1;
            snake[i] = LED(newX, snake[i-1].y, snake[i-1].z);
           }
            break;
           case HIGH:
            
            game_over = true;
            return;
            break; 
          }
          break;
       }
       break;
      }
      }
      else if (!apple_ate) //for all the othe LEDs as long as we dont eat an apple
        snake[i] = LED(snake[i+1].x, snake[i+1].y, snake[i+1].z); // move the body forward
      else if (apple_ate) //make a new apple
        apple = LED(random(8), random(8), random(8));
    }
    
    count = 0;
  }
}
