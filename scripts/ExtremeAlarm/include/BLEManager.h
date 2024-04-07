// BLEManager.h
#ifndef BLEMANAGER_H
#define BLEMANAGER_H

#include "BLEDevice.h"
#include "BLEServer.h"
#include "BLEUtils.h"
#include "BLE2902.h"

class BLEManager {
public:
    static void init();
    static bool isDeviceConnected();
    static void setDeviceConnected(bool connected);

private:
    static BLEServer *pServer;
    static BLECharacteristic *pTxCharacteristic;
    static bool deviceConnected;
    static bool oldDeviceConnected;
    static void setupBLE();
    static void setupServiceAndCharacteristics();
    static void startAdvertising();
};

#endif // BLEMANAGER_H
