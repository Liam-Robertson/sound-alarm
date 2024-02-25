#ifndef AudioManager_h
#define AudioManager_h

#include <Arduino.h>

class AudioManager {
private:
    static bool alarmManuallyStopped; 

public:
    static void init();
    static void loop();
    static void playAlarm();
    static void resetAudioResources();
    static void stopAlarm();
};

#endif
