#include "Audio.h"
#include <Arduino.h> // Ensure Arduino functions are accessible for logging

// Define the I2S pins in the CPP file
#define I2S_DIN_PIN 25
#define I2S_BCLK_PIN 27
#define I2S_LRC_PIN 26

Audio::Audio() : initialized(false), mp3(nullptr), out(nullptr), file(nullptr) {
    Serial.println("[Audio] Constructor called.");
}

void Audio::init() {
    Serial.println("[Audio] Initialization started.");
    file = new AudioFileSourceSPIFFS("/alarm.mp3");
    out = new AudioOutputI2S();
    out->SetPinout(I2S_BCLK_PIN, I2S_LRC_PIN, I2S_DIN_PIN);
    mp3 = new AudioGeneratorMP3();
    Serial.println("[Audio] Initialization completed.");
}

void Audio::handleMP3Playback(bool alarmState) {
    Serial.println("[Audio] Handling MP3 Playback.");
    if (alarmState) {
        Serial.println("[Audio] Alarm state is true.");
        if (!mp3->isRunning()) {
            Serial.println("[Audio] MP3 is not running, starting playback.");
            if (file) {
                delete file;
            }
            file = new AudioFileSourceSPIFFS("/alarm.mp3");
            mp3->begin(file, out);
        } else {
            Serial.println("[Audio] MP3 is already running.");
            if (!mp3->loop()) {
                Serial.println("[Audio] MP3 loop finished, restarting.");
                mp3->stop();
                delete file;
                file = new AudioFileSourceSPIFFS("/alarm.mp3");
                mp3->begin(file, out);
            }
        }
    } else if (mp3->isRunning()) {
        Serial.println("[Audio] Stopping MP3 playback.");
        mp3->stop();
        delete file;
        file = nullptr;
    }
}
