#include <AudioManager.h>
#include <SPIFFSSetup.h>
#include "Audio.h"

AudioGeneratorMP3 *mp3;
AudioFileSourceSPIFFS *file;
AudioOutputI2S *out;

void AudioManager::init() {
    file = new AudioFileSourceSPIFFS("/alarm.mp3");
    out = new AudioOutputI2S();
    out->SetPinout(27, 26, 25);
    mp3 = new AudioGeneratorMP3();
}

void AudioManager::loop() {
    if (mp3->isRunning()) {
        if (!mp3->loop()) mp3->stop();
    }
}

void AudioManager::playAlarm() {
    if (!mp3->isRunning()) {
        mp3->begin(file, out);
    }
}

void AudioManager::stopAlarm() {
    if (mp3->isRunning()) {
        mp3->stop();
    }
}
