#include "BLEManager.h"
#include <Arduino.h>

// Define the UUIDs for the BLE service and characteristics
#define SERVICE_UUID           "6E400001-B5A3-F393-E0A9-E50E24DCCA9E" // UART service UUID
#define CHARACTERISTIC_UUID_RX "6E400002-B5A3-F393-E0A9-E50E24DCCA9E" // RX Characteristic UUID
#define CHARACTERISTIC_UUID_TX "6E400003-B5A3-F393-E0A9-E50E24DCCA9E" // TX Characteristic UUID

// Initialize static member variables
BLEServer* BLEManager::pServer = nullptr;
BLECharacteristic* BLEManager::pTxCharacteristic = nullptr;
bool BLEManager::deviceConnected = false;
bool BLEManager::oldDeviceConnected = false;

// Declaration of callback classes to be defined later
class ServerCallbacks : public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) override {
        BLEManager::setDeviceConnected(true);
    }

    void onDisconnect(BLEServer* pServer) override {
        BLEManager::setDeviceConnected(false);
    }
};

class CharacteristicCallbacks : public BLECharacteristicCallbacks {
    void onWrite(BLECharacteristic* pCharacteristic) override {
        std::string rxValue = pCharacteristic->getValue();
        if (!rxValue.empty()) {
            Serial.print("Received Value: ");
            Serial.println(rxValue.c_str());
            // Here you can add code to handle the received data
        }
    }
};

void BLEManager::init() {
    BLEDevice::init("ESP32_BLE_UART");
    BLEManager::setupBLE();
}

void BLEManager::setupBLE() {
    pServer = BLEDevice::createServer();
    pServer->setCallbacks(new ServerCallbacks());
    
    BLEService* pService = pServer->createService(SERVICE_UUID);

    pTxCharacteristic = pService->createCharacteristic(
                            CHARACTERISTIC_UUID_TX,
                            BLECharacteristic::PROPERTY_NOTIFY);
    pTxCharacteristic->addDescriptor(new BLE2902());

    BLECharacteristic* pRxCharacteristic = pService->createCharacteristic(
                                             CHARACTERISTIC_UUID_RX,
                                             BLECharacteristic::PROPERTY_WRITE);
    pRxCharacteristic->setCallbacks(new CharacteristicCallbacks());

    pService->start();
    startAdvertising();
}

void BLEManager::startAdvertising() {
    BLEAdvertising* pAdvertising = BLEDevice::getAdvertising();
    pAdvertising->addServiceUUID(SERVICE_UUID);
    pAdvertising->setScanResponse(false);
    pAdvertising->setMinPreferred(0x06);  // Functions that help with iPhone connections issue
    pAdvertising->setMaxPreferred(0x12);
    BLEDevice::startAdvertising();
    Serial.println("Waiting for a client to connect...");
}

bool BLEManager::isDeviceConnected() {
    return deviceConnected;
}

void BLEManager::setDeviceConnected(bool connected) {
    deviceConnected = connected;
}
