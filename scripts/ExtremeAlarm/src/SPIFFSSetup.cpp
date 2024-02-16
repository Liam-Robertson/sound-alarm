#include "SPIFFSSetup.h"
#include <SPIFFS.h>
#include <FS.h>
#include <Arduino.h>

void SPIFFSSetup::setupSPIFFS() {
    Serial.println("[SPIFFSSetup] Mounting file system.");
    if (!SPIFFS.begin(true)) {
        Serial.println("[SPIFFSSetup] Failed to mount file system");
        return;
    }
    Serial.println("[SPIFFSSetup] Listing files in SPIFFS:");
    File dir = SPIFFS.open("/");
    if (!dir || !dir.isDirectory()) {
        Serial.println("[SPIFFSSetup] Failed to open directory or not a directory");
        return;
    }
    File entry = dir.openNextFile();
    while (entry) {
        if (entry.isDirectory()) {
            Serial.print("[SPIFFSSetup] DIR : ");
        } else {
            Serial.print("[SPIFFSSetup] FILE: ");
        }
        Serial.print(entry.name());
        Serial.print(" SIZE: ");
        Serial.println(entry.size());
        entry = dir.openNextFile();
    }
}
