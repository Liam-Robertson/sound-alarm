#ifndef AudioManager_h
#define AudioManager_h

#include <Arduino.h>
#include <Audio.h>

class AudioManager {
public:
    static void init();
    static void loop();
    static void playAlarm();
    static void stopAlarm();
};

#endif
