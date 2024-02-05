#include <Arduino.h>
#include <SPIFFS.h>
#include <FS.h>
#include "AudioFileSourceSPIFFS.h"
#include "AudioGeneratorMP3.h"
#include "AudioOutputI2S.h"
#include <WiFi.h>

const char *ssid = "VM6293918";     // Replace with your WiFi network name
const char *password = "k4bHjBb8ymcf"; // Replace with your WiFi network password

// Function prototypes
void setupWiFi();
void setupSPIFFS();
void startServer();
void handleClient();
void handleMP3Playback();

WiFiServer server(80);
AudioGeneratorMP3 *mp3 = nullptr;
AudioFileSourceSPIFFS *file = nullptr;
AudioOutputI2S *out = nullptr;
volatile bool alarmState = false; // True when the alarm should be playing

void setup() {
    Serial.begin(9600);

    setupWiFi();
    setupSPIFFS();

    file = new AudioFileSourceSPIFFS("/alarm.mp3");
    out = new AudioOutputI2S();
    out->SetPinout(27, 26, 25);
    mp3 = new AudioGeneratorMP3();
}

void loop() {
    handleClient();
    handleMP3Playback();
}

void setupWiFi() {
    Serial.println("Connecting to WiFi...");
    WiFi.begin(ssid, password);

    while (WiFi.status() != WL_CONNECTED) {
        delay(500);
        Serial.print(".");
    }

    Serial.println("");
    Serial.println("WiFi connected");
    Serial.print("IP Address: ");
    Serial.println(WiFi.localIP());
    server.begin();
}

void setupSPIFFS() {
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

void handleClient() {
    WiFiClient client = server.available();
    if (client) {
        Serial.println("New Client.");
        String currentLine = "";
        boolean currentLineIsBlank = true;
        boolean headersEnded = false;
        String body = "";

        while (client.connected()) {
            if (client.available()) {
                char c = client.read();
                Serial.write(c);

                if (headersEnded) {
                    body += c;
                    if (body.indexOf("startAlarm") >= 0) {
                        alarmState = true;
                        break;
                    }
                    if (body.indexOf("stopAlarm") >= 0) {
                        alarmState = false;
                        break;
                    }
                } else {
                    if (c == '\n' && currentLineIsBlank) {
                        headersEnded = true;
                    }
                    if (c == '\n') {
                        currentLineIsBlank = true;
                    } else if (c != '\r') {
                        currentLine += c;
                        currentLineIsBlank = false;
                    }
                }
            }
        }

        client.println("HTTP/1.1 200 OK");
        client.println("Content-type:text/plain");
        client.println("Connection: close");
        client.println();
        client.stop();
        Serial.println("Client Disconnected.");
    }
}

void handleMP3Playback() {
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
