#ifndef WiFiSetup_h
#define WiFiSetup_h

#include <WiFi.h>

class WiFiSetup {
public:
    static void setupWiFi(const char *ssid, const char *password);
};

#endif
