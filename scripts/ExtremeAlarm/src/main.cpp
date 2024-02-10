#include <Arduino.h>
#include <SPIFFS.h>
#include <WiFi.h>
#include <ESPAsyncWebServer.h>
#include <ArduinoJson.h>
#include "Audio.h"          // Make sure this header is properly defined
#include "AlarmManager.h"   // Ensure this file is correctly implemented

const char* ssid = "VM6293918";
const char* password = "k4bHjBb8ymcf";

AsyncWebServer server(80);
Audio audio;

volatile bool alarmState = false;

void setup() {
    Serial.begin(9600);
    while (!Serial) { /* wait for serial port to connect */ }

    audio.init();
    WiFi.begin(ssid, password);
    while (WiFi.status() != WL_CONNECTED) {
        delay(500);
    }
    Serial.println("WiFi Connected");

    if (!SPIFFS.begin()) {
        Serial.println("SPIFFS Mount Failed");
        return;
    }
    
    // Load alarms from file
    AlarmManager::loadAlarms();
    AlarmManager::printAlarms();

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

    server.on("/addAlarm", HTTP_POST, [](AsyncWebServerRequest *request){}, NULL, [](AsyncWebServerRequest *request, uint8_t *data, size_t len, size_t index, size_t total) {
        DynamicJsonDocument doc(1024);
        DeserializationError error = deserializeJson(doc, (const char*)data);
        if (error) {
            Serial.println("Failed to deserialize JSON");
            request->send(400, "application/json", "{\"message\":\"Invalid JSON\"}");
            return;
        }

        File file = SPIFFS.open("/alarms.json", FILE_APPEND);
        if (!file) {
            Serial.println("Failed to open file for appending");
            request->send(500, "application/json", "{\"message\":\"Failed to open file\"}");
            return;
        }

        if (serializeJson(doc, file) == 0) {
            Serial.println("Failed to write alarm to file");
            request->send(500, "application/json", "{\"message\":\"Failed to write alarm\"}");
        } else {
            Serial.println("Alarm added");
            request->send(200, "application/json", "{\"message\":\"Alarm added successfully\"}");
        }
        file.close();
    });

    server.begin();
}

void loop() {
    if (WiFi.status() == WL_CONNECTED) {
        // Optionally, synchronize time with NTP here
    }

    AlarmManager::checkAndTriggerAlarms(); // Updated call
    audio.handleMP3Playback(alarmState);
    delay(1000); // Adjust delay as needed
}