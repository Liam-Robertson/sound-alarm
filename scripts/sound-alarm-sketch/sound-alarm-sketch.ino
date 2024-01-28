#include <Arduino.h>
#include <SPIFFS.h>
#include <FS.h>
#include "AudioFileSourceSPIFFS.h"
#include "AudioGeneratorMP3.h"
#include "AudioOutputI2S.h"
#include <WiFi.h>

const char *ssid = "VM6293918";     // Replace with your WiFi network name
const char *password = "k4bHjBb8ymcf"; // Replace with your WiFi network password
WiFiServer server(80);

AudioGeneratorMP3 *mp3;
AudioFileSourceSPIFFS *file;
AudioOutputI2S *out;

volatile bool startAlarm = false;
volatile bool stopAlarm = false;

void setup() {
    Serial.begin(115200);
    // Connect to WiFi network
    Serial.println("Connecting to WiFi...");
    WiFi.begin(ssid, password);
    // Wait for connection
    while (WiFi.status() != WL_CONNECTED) {
        delay(500);
        Serial.print(".");
    }
    Serial.println("");
    Serial.println("WiFi connected");
    Serial.print("IP Address: ");
    Serial.println(WiFi.localIP());
    // Start the server
    server.begin();
    // Initialize SPIFFS
    if (!SPIFFS.begin()) {
        Serial.println("Failed to mount file system");
        return;
    }
    // List all files in SPIFFS
    Serial.println("Listing files in SPIFFS:");
    File dir = SPIFFS.open("/");
    if (!dir) {
        Serial.println("Failed to open directory");
        return;
    }
    if (!dir.isDirectory()) {
        Serial.println("Not a directory");
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
    file = new AudioFileSourceSPIFFS("/alarm.mp3");
    out = new AudioOutputI2S();
    out->SetPinout(27, 26, 25);
    mp3 = new AudioGeneratorMP3();
}

void loop() {
    WiFiClient client = server.available();   // Listen for incoming clients
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
                        startAlarm = true;
                        stopAlarm = false;
                        // Reinitialize the audio file source and mp3 player
                        if (file) {
                            delete file;
                            file = new AudioFileSourceSPIFFS("/alarm.mp3");
                        }
                        if (mp3->isRunning()) {
                            mp3->stop();
                            delete mp3;
                            mp3 = new AudioGeneratorMP3();
                        }
                        break;
                    }
                    if (body.indexOf("stopAlarm") >= 0) {
                        stopAlarm = true;
                        startAlarm = false;
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
        // Close the client connection
        client.println("HTTP/1.1 200 OK");
        client.println("Content-type:text/plain");
        client.println("Connection: close");
        client.println();
        client.stop();
        Serial.println("Client Disconnected.");
    }

    // Control MP3 playback
    if (startAlarm && !stopAlarm) {
        if (!mp3->isRunning()) {
            mp3->begin(file, out);
        }
    } else if (stopAlarm) {
        if (mp3->isRunning()) {
            mp3->stop();
        }
    }

    // Keep the MP3 playing
    if (mp3->isRunning()) {
        if (!mp3->loop()) {
            mp3->stop();
        }
    }
}
