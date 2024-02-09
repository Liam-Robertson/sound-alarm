#include "SPIFFSSetup.h"
#include <SPIFFS.h>
#include <FS.h>

void SPIFFSSetup::setupSPIFFS() {
    if (!SPIFFS.begin(true)) {
        Serial.println("Failed to mount file system");
        return;
    }
    Serial.println("Listing files in SPIFFS:");
    File dir = SPIFFS.open("/");
    if (!dir || !dir.isDirectory()) {
        Serial.println("Failed to open directory or not a directory");
        return;
    }
    File entry = dir.openNextFile();
    while (entry) {
        if (entry.isDirectory()) {
            Serial.print("  DIR : ");
            Serial.println(entry.name());
        } else {
            Serial.print("  FILE: ");
            Serial.print(entry.name());
            Serial.print("  SIZE: ");
            Serial.println(entry.size());
        }
        entry = dir.openNextFile();
    }
}