#ifndef BLEManager_h
#define BLEManager_h

#include <Arduino.h>
#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>
#include <AudioManager.h>

class BLEManager {
public:
    static void init();
    static void startAdvertising();
};

#endif
