#include <Arduino.h>
#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>
#include <ArduinoJson.h>
#include "SPIFFSSetup.h"
#include "Audio.h"

#define SERVICE_UUID "f261adff-f939-4446-82f9-2d00f4109dfe"
#define CHARACTERISTIC_UUID "a2932117-5297-476b-96f7-a873b1075803"

BLEServer *pServer = nullptr;
BLECharacteristic *pCharacteristic = nullptr;
volatile bool alarmState = false;
Audio audio;

class MyCallbacks : public BLECharacteristicCallbacks {
    void onWrite(BLECharacteristic *pCharacteristic) override {
        Serial.println("Received BLE message");
        std::string value = pCharacteristic->getValue();
        Serial.println("BLE message content: " + String(value.c_str()));
        if (!value.empty()) {
            DynamicJsonDocument doc(1024);
            DeserializationError error = deserializeJson(doc, value.c_str());
            if (error) {
                Serial.print("deserializeJson() failed: ");
                Serial.println(error.c_str());
                return;
            }
            if (doc.containsKey("startAlarm")) {
                Serial.println("startAlarm key found. Setting alarmState to true.");
                alarmState = true;
            } else {
                Serial.println("startAlarm key not found.");
            }
        } else {
            Serial.println("Received BLE message is empty.");
        }
    }
};

void setup() {
    Serial.begin(9600);
    Serial.println("Initializing system...");
    SPIFFSSetup::setupSPIFFS();
    audio.init();

    BLEDevice::init("ESP32_BLE_Alarm_Server");
    Serial.println("BLE Device initialized.");
    pServer = BLEDevice::createServer();
    Serial.println("BLE Server created.");
    BLEService *pService = pServer->createService(SERVICE_UUID);
    pCharacteristic = pService->createCharacteristic(
                        CHARACTERISTIC_UUID,
                        BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE);
    pCharacteristic->setCallbacks(new MyCallbacks());
    pService->start();
    BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
    pAdvertising->addServiceUUID(SERVICE_UUID);
    pAdvertising->setScanResponse(true);
    pAdvertising->setMinPreferred(0x06);
    pAdvertising->setMinPreferred(0x12);
    BLEDevice::startAdvertising();
    Serial.println("BLE server is running and advertising.");
}

void loop() {
    if (alarmState) {
        Serial.println("Alarm state is true, handling MP3 playback.");
        audio.handleMP3Playback(alarmState);
        alarmState = false; // Reset alarm state after handling
    }
    delay(1000); // Adjust based on your needs
}
