#include <ArduinoBLE.h>
#include "Ultrasonic.h"

#define MEASURE_INTERVAL_MS 200
#define WAITING_INTERVAL_MS 1000
#define MAX_DISTANCE 250 // Maximum distance (in cm) to ping.

BLEService distanceService("6b6ea109-9067-4a59-8baf-26f37740dc7d");
BLEUnsignedIntCharacteristic distanceChar("d18b1ec7-022d-423d-8bc6-8c19a57d7242", BLERead | BLENotify);
BLECharCharacteristic  manufacturerString("00002a29-0000-1000-8000-00805f9b34fb", BLERead);
BLEDescriptor cmLabelDescriptor("2901", "Distance in cm");
String manufacturerName = "Andreas Burger";

Ultrasonic ultrasonic(2); // PIN 2 connected to SIG on Ultrasonic Distance Sensor V2.0

void setup() {
  Serial.begin(9600);

  pinMode(LED_BUILTIN, OUTPUT);
  if (!BLE.begin()) {
    Serial.println("starting BLE failed!");
    while (1);
  }

  BLE.setLocalName("DistanceSensor");
  BLE.setAdvertisedService(distanceService);
  distanceChar.addDescriptor(cmLabelDescriptor);
  distanceService.addCharacteristic(distanceChar);
  distanceService.addCharacteristic(manufacturerString);
  BLE.addService(distanceService);

  BLE.advertise();
  Serial.println("Bluetooth device active, waiting for connections...");

  //manufacturerString.writeValue(manufacturerName);
}

int count = 0;

void loop()
{
  BLEDevice central = BLE.central();

  digitalWrite(LED_BUILTIN, HIGH);
  if (central)  {
    Serial.print("Connected to central: ");
    Serial.println(central.address());

    while (central.connected()) {
      int distance = ultrasonic.MeasureInCentimeters();
      Serial.print(distance);
      Serial.println(" cm");
      distanceChar.writeValue(distance);
      delay(MEASURE_INTERVAL_MS);
    }
  }
  Serial.print("Disconnected from central: ");
  Serial.println(central.address());

  delay(WAITING_INTERVAL_MS / 2);
  digitalWrite(LED_BUILTIN, LOW);
  delay(WAITING_INTERVAL_MS / 2);
}