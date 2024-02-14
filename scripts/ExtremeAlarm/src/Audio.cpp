#include "Audio.h"

// Define the I2S pins in the CPP file
#define I2S_DIN_PIN 25
#define I2S_BCLK_PIN 27
#define I2S_LRC_PIN 26

Audio::Audio() : initialized(false), mp3(nullptr), out(nullptr), file(nullptr) {}

void Audio::init() {
    file = new AudioFileSourceSPIFFS("/alarm.mp3");
    out = new AudioOutputI2S();
    out->SetPinout(27, 26, 25);
    mp3 = new AudioGeneratorMP3();
}

void Audio::handleMP3Playback(bool alarmState) {
    if (alarmState) {
        if (!mp3->isRunning()) {
            if (file) {
                delete file;
            }
            file = new AudioFileSourceSPIFFS("/alarm.mp3");
            mp3->begin(file, out);
        } else {
            if (!mp3->loop()) {
                mp3->stop();
                delete file;
                file = new AudioFileSourceSPIFFS("/alarm.mp3");
                mp3->begin(file, out);
            }
        }
    } else if (mp3->isRunning()) {
        mp3->stop();
        delete file;
        file = nullptr;
    }
}

