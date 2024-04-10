// BLEManager.cpp
#include "BLEManager.h"
#include <Arduino.h>

#define SERVICE_UUID        "f261adff-f939-4446-82f9-2d00f4109dfe"
#define CHARACTERISTIC_UUID "a2932117-5297-476b-96f7-a873b1075803"

void BLEManager::init() {
    BLEDevice::init("ESP32_BLE_Server");
    BLEServer* pServer = BLEDevice::createServer();
    pServer->setCallbacks(new ServerCallbacks());

    BLEService* pService = pServer->createService(SERVICE_UUID);
    BLECharacteristic* pCharacteristic = pService->createCharacteristic(
                                            CHARACTERISTIC_UUID,
                                            BLECharacteristic::PROPERTY_READ |
                                            BLECharacteristic::PROPERTY_WRITE |
                                            BLECharacteristic::PROPERTY_NOTIFY |
                                            BLECharacteristic::PROPERTY_INDICATE);

    pCharacteristic->setCallbacks(new CharacteristicCallbacks());
    pCharacteristic->addDescriptor(new BLE2902());

    pService->start();
    pServer->getAdvertising()->start();
    Serial.println("BLE initialized, waiting for clients...");
}

void BLEManager::ServerCallbacks::onConnect(BLEServer* pServer) {
    Serial.println("Client connected");
}

void BLEManager::ServerCallbacks::onDisconnect(BLEServer* pServer) {
    Serial.println("Client disconnected");
}

void BLEManager::CharacteristicCallbacks::onWrite(BLECharacteristic* pCharacteristic) {
    std::string rxValue = pCharacteristic->getValue();

    if (!rxValue.empty()) {
        Serial.print("Received Value: ");
        for (char const &c: rxValue) {
            Serial.print(c);
        }
        Serial.println();
    }
}
