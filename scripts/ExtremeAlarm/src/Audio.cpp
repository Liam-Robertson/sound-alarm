#include "Audio.h"

Audio::Audio() : initialized(false), mp3(nullptr), out(nullptr), file(nullptr) {}

void Audio::init() {
    if (!initialized) {
        Serial.println("Initializing I2S...");
        // I2S configuration setup...

        // Initialize the MP3 player
        mp3 = new AudioGeneratorMP3();
        if (mp3 == nullptr) {
            Serial.println("Failed to create MP3 player instance.");
            return;
        }

        // Initialize the I2S output
        out = new AudioOutputI2S();
        if (out == nullptr) {
            Serial.println("Failed to create I2S output instance.");
            delete mp3; // Clean up the mp3 object if out failed
            mp3 = nullptr;
            return;
        }

        // If you have any additional setup for mp3 or out, do it here

        initialized = true;
        Serial.println("MP3 player and I2S output initialized successfully.");

        // Configure the I2S interface
        i2s_config_t i2s_config = {
            .mode = static_cast<i2s_mode_t>(I2S_MODE_MASTER | I2S_MODE_TX),
            .sample_rate = 44100,
            .bits_per_sample = I2S_BITS_PER_SAMPLE_16BIT,
            .channel_format = I2S_CHANNEL_FMT_RIGHT_LEFT,
            .communication_format = static_cast<i2s_comm_format_t>(I2S_COMM_FORMAT_I2S | I2S_COMM_FORMAT_I2S_MSB),
            .intr_alloc_flags = 0,
            .dma_buf_count = 8,
            .dma_buf_len = 64,
            .use_apll = false,
            .tx_desc_auto_clear = true,
            .fixed_mclk = 0
        };

        // Pin configuration matching the provided wiring diagram
        i2s_pin_config_t pin_config = {
            .bck_io_num = 27, // BCLK connected to GPIO27
            .ws_io_num = 26,  // LRC connected to GPIO26
            .data_out_num = 25, // DIN connected to GPIO25
            .data_in_num = I2S_PIN_NO_CHANGE // Not used
        };

        // Install and start the I2S driver
        esp_err_t err = i2s_driver_install(I2S_NUM_0, &i2s_config, 0, NULL);
        if (err != ESP_OK) {
            Serial.printf("Failed to install I2S driver, error code: %d\n", err);
            return; // Stop initialization if there's an error
        }

        // Set the I2S pins
        err = i2s_set_pin(I2S_NUM_0, &pin_config);
        if (err != ESP_OK) {
            Serial.printf("Failed to set I2S pins, error code: %d\n", err);
            return; // Stop initialization if there's an error
        }

        // Set the I2S clock
        err = i2s_set_clk(I2S_NUM_0, 44100, I2S_BITS_PER_SAMPLE_16BIT, I2S_CHANNEL_STEREO);
        if (err != ESP_OK) {
            Serial.printf("Failed to set I2S clock, error code: %d\n", err);
            return; // Stop initialization if there's an error
        }

        // Initialize the MP3 decoder and output interface here, if applicable
        // ...

        initialized = true;
        Serial.println("I2S initialized successfully.");
    }
}


void Audio::handleMP3Playback(volatile bool &alarmState) {
    if (!mp3 || !out) {
        Serial.println("MP3 or output interface not initialized.");
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
                        Serial.println("Started MP3 playback.");
                    } else {
                        Serial.println("Failed to start MP3 playback.");
                        delete file;
                        file = nullptr;
                    }
                } else {
                    Serial.println("Failed to open alarm.mp3 file.");
                }
            } else {
                Serial.println("/alarm.mp3 does not exist on SPIFFS.");
            }
        } else {
            if (!mp3->loop()) {
                mp3->stop();
                Serial.println("MP3 playback stopped.");
                delete file;
                file = nullptr;
            }
        }
    } else {
        if (mp3->isRunning()) {
            mp3->stop();
            Serial.println("MP3 playback manually stopped.");
            delete file;
            file = nullptr;
        }
    }
}
