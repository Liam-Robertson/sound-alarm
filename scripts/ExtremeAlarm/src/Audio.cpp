#include "Audio.h"

Audio::Audio() : initialized(false) {}

void Audio::init() {
    if (!initialized) {
        Serial.println("Audio constructor");
        out = new AudioOutputI2S();
        Serial.println("AudioOutputI2S initialized");
        mp3 = new AudioGeneratorMP3();
        Serial.println("AudioGeneratorMP3 initialized");
        file = nullptr; 
        Serial.println("File set to nullptr");
        initialized = true; // Mark as initialized
    }
}

void Audio::handleMP3Playback(volatile bool &alarmState) {
    if (!mp3 || !out) {
        return;
    }
    if (alarmState) {
        if (!mp3->isRunning()) {
            if (file) {
                delete file;
                file = nullptr;
            }
            if (SPIFFS.exists("/alarm.mp3")) {
                file = new AudioFileSourceSPIFFS("/alarm.mp3");
                if (file) {
                    if (mp3->begin(file, out)) {
                    } else {
                        delete file;
                        file = nullptr;
                    }
                }
            } 
        } else {
            if (!mp3->loop()) {
                mp3->stop();
                delete file;
                file = nullptr;
            }
        }
    } else {
        if (mp3->isRunning()) {
            mp3->stop();
            delete file;
            file = nullptr;
        }
    }
}
