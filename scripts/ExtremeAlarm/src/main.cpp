#include <Arduino.h>
#include "SPIFFSSetup.h"
#include "BLEManager.h"
#include "AudioManager.h"

void setup() {
    Serial.begin(9600);
    Serial.println("Initializing system...");
    // SPIFFSSetup::setupSPIFFS();
    BLEManager::init();
    // AudioManager::init();
}

void loop() {
    // AudioManager::loop();
}
