#ifndef ALARM_MANAGER_H
#define ALARM_MANAGER_H

#include <Arduino.h>
#include <vector>
#include <TimeLib.h>

extern volatile bool alarmState; // Declare it as extern to access it globally

struct Alarm {
    int id;
    int hour;
    int minute;
    std::vector<String> daysActive;
    int volume;
    bool isActive;
    int userId;
};

class AlarmManager {
public:
    static void loadAlarms();
    static void checkAndTriggerAlarms();
    static void printAlarms(); // Function to print the current list of alarms
    static std::vector<Alarm> alarms;
};


#endif // ALARM_MANAGER_H
