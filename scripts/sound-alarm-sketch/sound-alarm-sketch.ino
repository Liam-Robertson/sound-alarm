
#include <Arduino.h>
#include <SPIFFS.h>
#include <FS.h>
#include "AudioFileSourceSPIFFS.h"
#include "AudioGeneratorMP3.h"
#include "AudioOutputI2S.h"
#include <WiFi.h>
const char *ssid = "VM6293918";     // Replace with your WiFi network name
const char *password = "k4bHjBb8ymcf"; // Replace with your WiFi network password
WiFiServer server(80);
AudioGeneratorMP3 *mp3;
AudioFileSourceSPIFFS *file;
AudioOutputI2S *out;
void setup() {
  Serial.begin(115200);
  // Connect to WiFi network
  Serial.println("Connecting to WiFi...");
  WiFi.begin(ssid, password);
  // Wait for connection
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("");
  Serial.println("WiFi connected");
  Serial.print("IP Address: ");
  Serial.println(WiFi.localIP());
  // Start the server
  server.begin();
  // Initialize SPIFFS
  if (!SPIFFS.begin()) {
    Serial.println("Failed to mount file system");
    return;
  }

  // List all files in SPIFFS
  Serial.println("Listing files in SPIFFS:");
  File dir = SPIFFS.open("/");
  if (!dir) {
    Serial.println("Failed to open directory");
    return;
  }
  if (!dir.isDirectory()) {
    Serial.println("Not a directory");
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

  file = new AudioFileSourceSPIFFS("/alarm.mp3");
  out = new AudioOutputI2S();
  out->SetPinout(27, 26, 25);
  mp3 = new AudioGeneratorMP3();
}


void loop() {
  // Check for new clients
  WiFiClient client = server.available();
  if (client) {
    Serial.println("New Client.");
    String currentLine = "";
    boolean isBody = false;
    String body = "";
    boolean bodyStarted = false;
    // Read the incoming data from the client
    while (client.connected() && client.available()) {
      char c = client.read();
      Serial.write(c);
      if (isBody) {
        if (c == '\n' && bodyStarted) {
          break; // End of the body, exit the loop
        }
        if (c != '\r' && c != '\n') {
          body += c;
          bodyStarted = true;
        }
      } else {
        if (c == '\n') {
          if (currentLine.length() == 0) {
            isBody = true; // Headers are done, body is next
          } else {
            currentLine = "";
          }
        } else if (c != '\r') {
          currentLine += c;
        }
      }
    }
    Serial.println("Received body: " + body);
    // Check if the body contains "startAlarm"
    if (body.indexOf("startAlarm") >= 0) {
      Serial.println("Start Alarm command received.");
      if (!mp3->isRunning()) {
        Serial.println("Starting MP3 playback...");
        file->seek(0, SeekSet);
        if (mp3->begin(file, out)) {
          Serial.println("Playback started successfully.");
        } else {
          Serial.println("Failed to start playback.");
        }
      } else {
        Serial.println("MP3 is already running.");
      }
    }
    // Send a response to the client
    delay(100); // Delay to ensure the request is fully received
    if (client.connected()) {
      Serial.println("Sending response...");
      client.println("HTTP/1.1 200 OK");
      client.println("Content-Type: text/plain");
      client.println("Connection: close");
      client.println();
      client.println("Command received");
      delay(500); // Longer delay to ensure the response is processed
    }
    client.stop();
    Serial.println("Client Disconnected.");
  }
  // Handle MP3 playback
  if (mp3->isRunning()) {
    if (!mp3->loop()) {
      mp3->stop();
      Serial.println("Playback finished");
    }
  }
}
