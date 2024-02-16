#include "BLEManager.h"
#include <ArduinoJson.h>

#define SERVICE_UUID "f261adff-f939-4446-82f9-2d00f4109dfe"
#define CHARACTERISTIC_UUID "a2932117-5297-476b-96f7-a873b1075803"

BLEServer *pServer = nullptr;
BLECharacteristic *pCharacteristic = nullptr;

// Define a subclass of BLECharacteristicCallbacks
class MyCallbacks: public BLECharacteristicCallbacks {
    void onWrite(BLECharacteristic *pCharacteristic) override {
        std::string value = pCharacteristic->getValue();
        if (!value.empty()) {
            DynamicJsonDocument doc(1024);
            DeserializationError error = deserializeJson(doc, value.c_str());
            if (!error) {
                if (doc.containsKey("startAlarm")) {
                    AudioManager::playAlarm();
                } else if (doc.containsKey("stopAlarm")) {
                    AudioManager::stopAlarm();
                }
            }
        }
    }
};

void BLEManager::init() {
    BLEDevice::init("ESP32_BLE_Alarm_Server");
    pServer = BLEDevice::createServer();
    BLEService *pService = pServer->createService(SERVICE_UUID);
    pCharacteristic = pService->createCharacteristic(
                        CHARACTERISTIC_UUID,
                        BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE);
    pCharacteristic->setCallbacks(new MyCallbacks());
    pService->start();
    BLEDevice::getAdvertising()->start();
    Serial.println("BLE server is running and advertising.");
}

void BLEManager::loop() {
    // No operation required for this example, but you could check for BLE events here.
}
