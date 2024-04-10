// BLEManager.h
#ifndef BLEMANAGER_H
#define BLEMANAGER_H

#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>
#include <BLE2902.h>

class BLEManager {
public:
    static void init();
private:
    static void onConnect(BLEServer* pServer);
    static void onDisconnect(BLEServer* pServer);
    class ServerCallbacks: public BLEServerCallbacks {
        void onConnect(BLEServer* pServer) override;
        void onDisconnect(BLEServer* pServer) override;
    };
    class CharacteristicCallbacks: public BLECharacteristicCallbacks {
        void onWrite(BLECharacteristic* pCharacteristic) override;
    };
};

#endif // BLEMANAGER_H
