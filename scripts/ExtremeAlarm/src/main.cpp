#include <Arduino.h>
#include <SPIFFS.h>
#include <WiFi.h>
#include <ESPAsyncWebServer.h>
#include <ArduinoJson.h>
#include "Audio.h"

const char* ssid = "VM6293918";
const char* password = "k4bHjBb8ymcf";

AsyncWebServer server(80);
Audio audio;

volatile bool alarmState = false;

void setup() {
    Serial.begin(9600);
    while(!Serial) { }
    audio.init();
    WiFi.begin(ssid, password);
    while (WiFi.status() != WL_CONNECTED) {
        delay(500);
    }
    if (!SPIFFS.begin()) {
        Serial.println("SPIFFS Mount Failed");
        return;
    }
    server.on("/", HTTP_GET, [](AsyncWebServerRequest* request) {
        request->send(200, "text/plain", "Hello, world");
    });
    server.on("/toggleAlarm", HTTP_POST, [](AsyncWebServerRequest *request) {}, NULL, [](AsyncWebServerRequest *request, uint8_t *data, size_t len, size_t index, size_t total) {
        DynamicJsonDocument doc(1024);
        deserializeJson(doc, (const char*)data);
        String status = doc["status"].as<String>();

        Serial.print("Received toggleAlarm request with status: ");
        Serial.println(status);

        if (status == "on") {
            alarmState = true;
            Serial.println("Alarm state set to ON.");
            request->send(200, "application/json", "{\"message\":\"Alarm turned on\"}");
        } else if (status == "off") {
            alarmState = false;
            Serial.println("Alarm state set to OFF.");
            request->send(200, "application/json", "{\"message\":\"Alarm turned off\"}");
        } else {
            Serial.println("Invalid status value received.");
            request->send(400, "application/json", "{\"message\":\"Invalid status value\"}");
        }
    });
    server.begin();
}

void loop() {
    audio.handleMP3Playback(alarmState);
}
