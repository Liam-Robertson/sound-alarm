#include <AudioManager.h>
#include <SPIFFSSetup.h>
#include "Audio.h"

AudioGeneratorMP3 *mp3;
AudioFileSourceSPIFFS *file;
AudioOutputI2S *out;

void AudioManager::init() {
    Serial.println("[AudioManager] Initialization started.");
    file = new AudioFileSourceSPIFFS("/alarm.mp3");
    out = new AudioOutputI2S();
    out->SetPinout(27, 26, 25);
    mp3 = new AudioGeneratorMP3();
    Serial.println("[AudioManager] Initialization completed.");
}

void AudioManager::loop() {
    if (mp3->isRunning()) {
        if (!mp3->loop()) {
            mp3->stop();
        }
    }
}

void AudioManager::playAlarm() {
    Serial.println("[AudioManager] Attempting to play alarm.");
    if (!mp3->isRunning()) {
        Serial.println("[AudioManager] Alarm is not running, starting now.");
        mp3->begin(file, out);
    } else {
        Serial.println("[AudioManager] Alarm is already running.");
    }
}

void AudioManager::stopAlarm() {
    Serial.println("[AudioManager] Attempting to stop alarm.");
    if (mp3->isRunning()) {
        Serial.println("[AudioManager] Alarm is running, stopping now.");
        mp3->stop();
    } else {
        Serial.println("[AudioManager] Alarm is not running.");
    }
}