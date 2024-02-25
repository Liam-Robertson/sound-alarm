#include "BLEManager.h"
#include <ArduinoJson.h>

#define SERVICE_UUID "f261adff-f939-4446-82f9-2d00f4109dfe"
#define CHARACTERISTIC_UUID "a2932117-5297-476b-96f7-a873b1075803"

BLEServer *pServer = nullptr;
BLECharacteristic *pCharacteristic = nullptr;

// Define a subclass of BLECharacteristicCallbacks
class MyCallbacks: public BLECharacteristicCallbacks {
    void onWrite(BLECharacteristic *pCharacteristic) override {
        Serial.println("[BLEManager] Received message");
        std::string value = pCharacteristic->getValue();
        if (!value.empty()) {
            DynamicJsonDocument doc(1024);
            DeserializationError error = deserializeJson(doc, value.c_str());
            if (!error) {
                if (doc.containsKey("startAlarm")) {
                    Serial.println("[BLEManager] Received startAlarm message");
                    AudioManager::playAlarm();
                } else if (doc.containsKey("stopAlarm")) {
                    Serial.println("[BLEManager] Received stopAlarm message");
                    AudioManager::stopAlarm();
                }
            }
        }
    }
};

class MyServerCallbacks: public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) {
        Serial.println("[BLEManager] Client Connected");
    }

    void onDisconnect(BLEServer* pServer) {
        Serial.println("[BLEManager] Client Disconnected, restarting advertising");
        BLEManager::startAdvertising();
    }
};

void BLEManager::init() {
    Serial.println("[BLEManager] Initialization started.");
    BLEDevice::init("ESP32_BLE_Alarm_Server");
    pServer = BLEDevice::createServer();
    pServer->setCallbacks(new MyServerCallbacks()); // Set server callbacks

    BLEService *pService = pServer->createService(SERVICE_UUID);
    pCharacteristic = pService->createCharacteristic(
                        CHARACTERISTIC_UUID,
                        BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE);
    pCharacteristic->setCallbacks(new MyCallbacks());
    pService->start();
    startAdvertising();
    Serial.println("[BLEManager] BLE server is running and advertising.");
}

void BLEManager::startAdvertising() {
    Serial.println("[BLEManager] Starting or restarting advertising.");
    BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
    pAdvertising->addServiceUUID(SERVICE_UUID); // Ensure your service UUID is advertised
    pAdvertising->setScanResponse(true);
    pAdvertising->setMinPreferred(0x06);  // functions that help with iPhone connections issue
    pAdvertising->setMaxPreferred(0x12);
    BLEDevice::startAdvertising();
    Serial.println("[BLEManager] Advertising started.");
}