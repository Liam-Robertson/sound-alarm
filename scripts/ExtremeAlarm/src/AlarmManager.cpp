#include "AlarmManager.h"
#include <FS.h>
#include <SPIFFS.h>
#include <ArduinoJson.h>

std::vector<Alarm> AlarmManager::alarms;

void AlarmManager::loadAlarms() {
    if (!SPIFFS.begin(true)) {
        Serial.println("SPIFFS Mount Failed");
        return;
    }

    File file = SPIFFS.open("/alarms.json", "r");
    if (!file) {
        Serial.println("Failed to open alarms file");
        return;
    }

    DynamicJsonDocument doc(4096); // Adjust size based on your needs
    DeserializationError error = deserializeJson(doc, file);
    if (error) {
        Serial.println("Failed to parse alarms file");
        file.close();
        return;
    }

    JsonArray arr = doc.as<JsonArray>();
    for (JsonObject obj : arr) {
        Alarm alarm;
        alarm.id = obj["id"];
        alarm.hour = obj["time"].as<String>().substring(0, 2).toInt(); // Assuming time format is "HH:MM"
        alarm.minute = obj["time"].as<String>().substring(3, 5).toInt();
        // Parse daysActive and other fields as needed
        alarm.isActive = obj["isActive"];
        AlarmManager::alarms.push_back(alarm);
    }
    file.close();
}

void AlarmManager::checkAndTriggerAlarms() {
    for (Alarm &alarm : AlarmManager::alarms) {
        if (!alarm.isActive) continue;

        time_t now = time(nullptr);
        tm *timeinfo = localtime(&now);

        if (timeinfo->tm_hour == alarm.hour && timeinfo->tm_min == alarm.minute) {
            alarmState = true; // Trigger alarm
            break;
        } else {
            alarmState = false;
        }
    }
}

void AlarmManager::printAlarms() {
    Serial.println("Current Alarm List:");
    for (const Alarm& alarm : alarms) {
        Serial.print("Alarm ID: ");
        Serial.print(alarm.id);
        Serial.print(", Time: ");
        Serial.print(alarm.hour);
        Serial.print(":");
        Serial.println(alarm.minute);
        Serial.print(", Days Active: ");
        for (const String& day : alarm.daysActive) {
            Serial.print(day + " ");
        }
        Serial.print(", Volume: ");
        Serial.print(alarm.volume);
        Serial.print(", Is Active: ");
        Serial.println(alarm.isActive ? "Yes" : "No");
        Serial.print(", User ID: ");
        Serial.println(alarm.userId);
        Serial.println("------");
    }
}
