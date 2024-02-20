#include "AudioManager.h"
#include <SPIFFSSetup.h>
#include "Audio.h"

AudioGeneratorMP3 *mp3 = nullptr;
AudioFileSourceSPIFFS *file = nullptr;
AudioOutputI2S *out = nullptr;
bool AudioManager::alarmManuallyStopped = false;

void AudioManager::init() {
    Serial.println("[AudioManager] Initialization started.");
    // Initialize file source for the MP3 file
    if (!file) {
        file = new AudioFileSourceSPIFFS("/alarm.mp3");
    } else {
        Serial.println("[AudioManager] Warning: file object was already initialized.");
    }
    // Initialize the I2S output
    if (!out) {
        out = new AudioOutputI2S();
        out->SetPinout(27, 26, 25);
    } else {
        Serial.println("[AudioManager] Warning: out object was already initialized.");
    }
    // Initialize the MP3 decoder
    if (!mp3) {
        mp3 = new AudioGeneratorMP3();
    } else {
        Serial.println("[AudioManager] Warning: mp3 object was already initialized.");
    }
    Serial.println("[AudioManager] Initialization completed.");
}

void AudioManager::playAlarm() {
    Serial.println("[AudioManager] Attempting to play alarm.");
    alarmManuallyStopped = false;
    if (!mp3) {
        mp3 = new AudioGeneratorMP3();
    }
    // Check if the alarm is not already running
    if (!mp3->isRunning()) {
        if (file) {
            file->close(); // Make sure to close the file before trying to open it again
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
            file->close();
        }
        Serial.println("[AudioManager] Alarm stopped and file closed.");
        alarmManuallyStopped = true; // Set the flag when the alarm is manually stopped
    } else {
        Serial.println("[AudioManager] Alarm is not running or MP3 object not initialized.");
    }
}

void AudioManager::loop() {
    if (mp3 && mp3->isRunning()) {
        if (!mp3->loop() && !alarmManuallyStopped) { // Check the flag here
            Serial.println("[AudioManager] MP3 loop has ended, stopping...");
            stopAlarm();
        }
    }
}
