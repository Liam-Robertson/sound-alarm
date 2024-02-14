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

volatile bool startAlarm = false;
volatile bool stopAlarm = false;
volatile bool alarmState = false; 
// Audio audio; -**********

AudioGeneratorMP3 *mp3;
AudioFileSourceSPIFFS *file;
AudioOutputI2S *out;

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
    // audio.init(); -***********

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


    file = new AudioFileSourceSPIFFS("/alarm.mp3");
    out = new AudioOutputI2S();
    out->SetPinout(27, 26, 25);
    mp3 = new AudioGeneratorMP3();
}

void loop() {
    if (alarmState) {
        if (!mp3->isRunning()) {
            if (file) {
                delete file;
            }
            file = new AudioFileSourceSPIFFS("/alarm.mp3");
            mp3->begin(file, out);
        } else {
            if (!mp3->loop()) {
                mp3->stop();
                delete file;
                file = new AudioFileSourceSPIFFS("/alarm.mp3");
                mp3->begin(file, out);
            }
        }
    } else if (mp3->isRunning()) {
        mp3->stop();
        delete file;
        file = nullptr;
    }
}
