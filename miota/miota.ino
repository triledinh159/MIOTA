#include <ESP8266WiFi.h>
#include "DHTesp.h"
#include <ArduinoJson.h>
#include <PubSubClient.h>
#include <WiFiClientSecure.h>

#define DHTpin 2
DHTesp dht;
//----Thay đổi thành thông tin của bạn---------------
const char* ssid = "A207 M";      //Wifi connect
const char* password = "Khongcopass@";   //Password

const char* mqtt_server = "d567f3932ca749f78c9e75dac4e4eab5.s2.eu.hivemq.cloud";
const int mqtt_port = 8883;
const char* mqtt_username = "trild"; //User
const char* mqtt_password = "Tri123456"; //Password
//--------------------------------------------------
WiFiClientSecure espClient;
PubSubClient client(espClient);

unsigned long lastMsg = 0;
#define MSG_BUFFER_SIZE (50)
char msg[MSG_BUFFER_SIZE];
bool button ;

void setup_wifi() {
  delay(10);
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  randomSeed(micros());
  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
}
//------------Connect to MQTT Broker-----------------------------
void reconnect() {
  while (!client.connected()) {
    Serial.print("Attempting MQTT connection...");
    String clientID =  "ESPClient-";
    clientID += String(random(0xffff),HEX);
    if (client.connect(clientID.c_str(), mqtt_username, mqtt_password)) {
      Serial.println("connected");
      client.subscribe("esp8266/client");
    } else {
      Serial.print("failed, rc=");
      Serial.print(client.state());
      Serial.println(" try again in 5 seconds");
      delay(5000);
    }
  }
}
//-----Call back Method for Receiving MQTT massage---------

void action(char a){
  if (a == '1'){
     Serial.println("BAT");
    digitalWrite(LED_BUILTIN, HIGH);  // turn the LED on (HIGH is the voltage level)
  }
  else {
    Serial.println("TAT");
    digitalWrite(LED_BUILTIN, LOW);   // turn the LED off by making the voltage LOW
  }
}

void callback(char* topic, byte* payload, unsigned int length) {
  String incommingMessage = "";
  for(int i=0; i<length;i++) incommingMessage += (char)payload[i];
  action((char)payload[0]);  


  Serial.println("Massage arived ["+String(topic)+"]"+incommingMessage);
}
//-----Method for Publishing MQTT Messages---------
void publishMessage(const char* topic, String payload, boolean retained){
  if(client.publish(topic,payload.c_str(),true))
    Serial.println("Message published ["+String(topic)+"]: "+payload);
}


void setup() {
  Serial.begin(9600);
  while(!Serial) delay(1);
  pinMode(LED_BUILTIN, OUTPUT);

  dht.setup(DHTpin,DHTesp::DHT11);

  setup_wifi();
  espClient.setInsecure();
  client.setServer(mqtt_server, mqtt_port);
  client.setCallback(callback);
}
unsigned long timeUpdata=millis();
void loop() {
  if (!client.connected()) {
    reconnect();
  }
  client.loop();
  //read DHT11
  if(millis()-timeUpdata>5000){
    delay(dht.getMinimumSamplingPeriod());
    float h = dht.getHumidity();
    float t = dht.getTemperature();

    DynamicJsonDocument doc(1024);
    doc["humidity"]=h;
    doc["temperature"]=t;
    char mqtt_message[128];
    serializeJson(doc,mqtt_message);
   // publishMessage("esp8266/dht11", mqtt_message, true);

    timeUpdata=millis();
  }
}