// BLEManager.cpp
#include "BLEManager.h"
#include <Arduino.h>

#define SERVICE_UUID        "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define CHARACTERISTIC_UUID "beb5483e-36e1-4688-b7f5-ea07361b26a8"

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
