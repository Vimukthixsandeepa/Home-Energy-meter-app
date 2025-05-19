#define s0 4
#define s1 5
#define s2 6
#define s3 7
#define senserout 13
void setup(){
  pinMode(s0,OUTPUT);
  pinMode(s1,OUTPUT);
  pinMode(s2,OUTPUT);
  pinMode(s3,OUTPUT);
  pinMode(senserout,INPUT);
  digitalWrite(s0,HIGH);
  digitalWrite(s1,LOW);
  Serial.begin(9600);

}

void loop(){
digitalWrite(s2,LOW);
digitalWrite(s3,LOW);
int red = plusin(senserout,LOW);
Serial.print("red intencity :");
Serial.println("red");
delay(200);

digitalWrite(s2,HIGH);
digitalWrite(s3,HIGH
int red = plusin(senserout,LOW);
Serial.print("red intencity :");
Serial.println("red");
delay(200);

digitalWrite(s2,LOW);
digitalWrite(s3,HIGH);
int red = plusin(senserout,LOW);
Serial.print("red intencity :");
Serial.println("red");
delay(200);

int sum =red+blue+green;
int r = (red*100)/sum;
int b = (red*100)/sum;
int b = (red*100)/sum;

if(r>b&&r>g){
  




}









}