#ifndef Audio_h
#define Audio_h

#include <SPIFFS.h>
#include <AudioFileSourceSPIFFS.h>
#include <AudioGeneratorMP3.h>
#include <AudioOutputI2S.h>
#include <ArduinoJson.h>
#include <ESPAsyncWebServer.h>

class Audio {
public:
    Audio();
    void init();
    void handleMP3Playback(volatile bool &alarmState);

private:
    AudioGeneratorMP3 *mp3;
    AudioFileSourceSPIFFS *file;
    AudioOutputI2S *out;
    bool initialized; 
};

#endif
