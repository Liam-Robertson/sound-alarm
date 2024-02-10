#include <vector>
#include <ArduinoJson.h>

struct Alarm {
    int id;
    String time;  // Format HH:MM for simplicity
    std::vector<String> daysActive;  // Using vector for simplicity, ensure C++ STL is enabled
    int volume;
    bool isActive;
    int userId;
};

// Define a global list (vector) of alarms
std::vector<Alarm> alarms;
