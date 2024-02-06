#include <Arduino.h>
#include <SPIFFS.h>
#include <FS.h>
#include <AudioFileSourceSPIFFS.h>
#include <AudioGeneratorMP3.h>
#include <AudioOutputI2S.h>
#include <WiFi.h>
#include <ESPAsyncWebServer.h>
#include <ArduinoJson.h> 

const char *ssid = "VM6293918";
const char *password = "k4bHjBb8ymcf";

AsyncWebServer server(80);
AudioGeneratorMP3 *mp3 = nullptr;
AudioFileSourceSPIFFS *file = nullptr;
AudioOutputI2S *out = nullptr;
volatile bool alarmState = false;

void setupWiFi();
void setupSPIFFS();
void handleMP3Playback();

void setup() {
    Serial.begin(9600);
    setupWiFi();
    setupSPIFFS();
    file = new AudioFileSourceSPIFFS("/alarm.mp3");
    out = new AudioOutputI2S();
    out->SetPinout(27, 26, 25);
    mp3 = new AudioGeneratorMP3();

    server.on("/", HTTP_GET, [](AsyncWebServerRequest *request){
        request->send(200, "text/plain", "Hello, world");
    });

    server.on("/toggleAlarm", HTTP_POST, [](AsyncWebServerRequest *request) {},
      NULL,
      [](AsyncWebServerRequest *request, uint8_t *data, size_t len, size_t index, size_t total) {
        // Parse JSON object from request body
        DynamicJsonDocument jsonDocument(256);
        DeserializationError error = deserializeJson(jsonDocument, (const char*)data);
        if (!error) {
          JsonObject json = jsonDocument.as<JsonObject>();
          if (json.containsKey("status")) {
            String status = json["status"].as<String>();
            if (status == "on") {
              alarmState = true;
              request->send(200, "application/json", "{\"message\":\"Alarm started\"}");
            } else if (status == "off") {
              alarmState = false;
              request->send(200, "application/json", "{\"message\":\"Alarm stopped\"}");
            } else {
              request->send(400, "application/json", "{\"message\":\"Invalid status value\"}");
            }
          } else {
            request->send(400, "application/json", "{\"message\":\"Missing status key\"}");
          }
        } else {
          request->send(400, "application/json", "{\"message\":\"Invalid JSON data\"}");
        }
      });

    server.begin();
}

void loop() {
    handleMP3Playback();
}

void setupWiFi() {
    WiFi.begin(ssid, password);
    while (WiFi.status() != WL_CONNECTED) {
        delay(500);
    }
    Serial.println("");
    Serial.println("WiFi connected");
    Serial.print("IP Address: ");
    Serial.println(WiFi.localIP());
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
