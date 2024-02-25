#include "AudioManager.h"
#include <SPIFFSSetup.h>
#include <SPIFFS.h>
#include <Arduino.h>
#include <memory>
#include "AudioFileSourceSPIFFS.h"
#include "AudioGeneratorMP3.h"
#include "AudioOutputI2S.h"

std::unique_ptr<AudioGeneratorMP3> mp3;
std::unique_ptr<AudioFileSourceSPIFFS> file;
std::unique_ptr<AudioOutputI2S> out;
bool AudioManager::alarmManuallyStopped = false;

void AudioManager::init() {
    resetAudioResources();
    Serial.println("[AudioManager] Audio system initialized.");
}

void AudioManager::playAlarm() {
    Serial.println("[AudioManager] Attempting to play alarm.");
    alarmManuallyStopped = false;
    if (mp3 && !mp3->isRunning()) {
        mp3->begin(file.get(), out.get());
        Serial.println("[AudioManager] Alarm is now playing.");
    } else {
        Serial.println("[AudioManager] Alarm is already running or resources not ready.");
    }
}

void AudioManager::stopAlarm() {
    if (mp3 && mp3->isRunning()) {
        Serial.println("[AudioManager] Stopping alarm.");
        mp3->stop();
        delay(1000);
        resetAudioResources();
        alarmManuallyStopped = true;
        Serial.println("[AudioManager] Alarm stopped.");
    } else {
        Serial.println("[AudioManager] No alarm to stop or resources not initialized.");
    }
}

void AudioManager::resetAudioResources() {
    file.reset(new AudioFileSourceSPIFFS("/alarm.mp3"));
    mp3.reset(new AudioGeneratorMP3());
    out.reset(new AudioOutputI2S());
    out->SetPinout(27, 26, 25);
}

void AudioManager::loop() {
    if (mp3 && mp3->isRunning() && !mp3->loop() && !alarmManuallyStopped) {
        Serial.println("[AudioManager] MP3 playback ended. Stopping...");
        delay(1000);
        stopAlarm();
    }
}
