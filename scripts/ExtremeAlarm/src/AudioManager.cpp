#include "AudioManager.h"
#include <SPIFFSSetup.h>
#include "Audio.h"

AudioGeneratorMP3 *mp3 = nullptr;
AudioFileSourceSPIFFS *file = nullptr;
AudioOutputI2S *out = nullptr;

void AudioManager::init() {
    Serial.println("[AudioManager] Initialization started.");
    // Ensure that previous resources are properly released before initialization
    if (!file) {
        file = new AudioFileSourceSPIFFS("/alarm.mp3");
    }
    if (!out) {
        out = new AudioOutputI2S();
        out->SetPinout(27, 26, 25);
    }
    if (!mp3) {
        mp3 = new AudioGeneratorMP3();
    }
    Serial.println("[AudioManager] Initialization completed.");
}

void AudioManager::loop() {
    if (mp3 && mp3->isRunning()) {
        if (!mp3->loop()) {
            Serial.println("[AudioManager] MP3 loop has ended, stopping...");
            stopAlarm(); // Ensure alarm is stopped if the loop has ended
        }
    }
}

void AudioManager::playAlarm() {
    Serial.println("[AudioManager] Attempting to play alarm.");
    if (!mp3) {
        Serial.println("[AudioManager] MP3 object not initialized.");
        return;
    }
    if (!mp3->isRunning()) {
        if (file) {
            file->close(); // Ensure the file is closed before reopening
            if (!file->open("/alarm.mp3")) {
                Serial.println("[AudioManager] Failed to open file for playback.");
                return;
            }
        } else {
            Serial.println("[AudioManager] File object not initialized.");
            return;
        }
        mp3->begin(file, out);
        Serial.println("[AudioManager] Alarm is now playing.");
    } else {
        Serial.println("[AudioManager] Alarm is already running.");
    }
}

void AudioManager::stopAlarm() {
    Serial.println("[AudioManager] Attempting to stop alarm.");
    if (mp3 && mp3->isRunning()) {
        mp3->stop();
        if (file) {
            file->close(); // Close the file after stopping the playback
        }
        Serial.println("[AudioManager] Alarm stopped and file closed.");
    } else {
        Serial.println("[AudioManager] Alarm is not running or MP3 object not initialized.");
    }
}
