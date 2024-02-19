Note: 
- You don't want to store alarm timings in the esp32 because then the esp32 alarm could trigger when the phone was out of battery
- This means there would be no way to turn off the alarm
- The only thing you might ever want to store in the esp32 is timings for when wifi should go on or off

- You could either have this alarm be started and stopped using bluetooth or wifi 
- Wifi is better since it has better range and is more stable but bluetooth consumes less power and has better built in functionality 
- In order to use wifi, you'd have to find ways to consistently turn the wifi connection on and off to conserve power while also still be able to receive messages
- Right now I'm leaning towards using approach 1 in the short term and moving to approach 3 if I have range concerns
- Approach 2 seems like it wouldn't work 

Approach 1 - Using bluetooth:
- The simplest one - bluetooth has lots of functionality designed for this 
- Just always keep the device bluetooth connected so it can always send and receive messages
- This consumes relatively little power 
- The downside is that bluetooth is low range, especially within a house. If you went out of range you couldn't turn the alarm off

Approach 2 - Using wifi only and disconnecting regularly: 
- What if I just kept the wifi capabilities on all the time but didn't keep it constantly connected? Would that still drain battery? 
- I feel like that's what I did before though right and it still drained a ton of battery 
- I don't think that's what you did, I think 
- How would this work? How would the esp32 know when to connect to the wifi network? 
- You would want it to only connect to the wifi when the wifi is ready but the esp32 has no idea when that is 

Approach 3 - Mix of bluetooth and wifi: 
- There would always be a bluetooth signal to allow the user to create alarms and have them stored in the esp32
- However the alarm being triggered on and off would be controlled by wifi 
- When the alarm is stored in the esp32, it would also stored instructions to connect to wifi when the alarm timing is reached
- When the alarm was turned off it would disconnect from wifi
- The upsides of this is that it allows you to use wifi to turn the alarms on and off with low power use and no mqtt server
- The downsides of this is that it might be confusing for the user 
  - For the alarm to work, there has to be both a stable bluetooth connection and the ability to connect to the wifi 
  - If any of those fail the alarm wouldn't work
  - You would also need to get the user to set up both wifi and bluetooth and find clean ways of telling them if either were broken
  - This is complicated from both a code perspective and a user experience perspective
- If you managed to do this well though it would let you have the advantages of both bluetooth and wifi while also having low power consumption

Approach 4 - Using wifi with an mqtt server: 
- If you were using wifi you would need the wifi to be off most of the time to conserve power
  - Having a constant wifi connection is power intensive
- However you also need there to be an active wifi connection to send and receive messages
- To do this, you would disconnect the wifi most of the time (and probably sleep the esp32) and only reconnect once every 60 seconds
- However if the android app send a message when the esp32 was disconnected from wifi, the esp32 would miss the message 
- Because of this, you'd need to host an mqtt broker on a cloud server 
  - Alternatively you could just create your own server and do this using http requests rather than mqtt
- When the esp32 connects to wifi every 60 seconds, it would check the mqtt server to see if any new alarms had arrived 
- If they had, the esp32 would store those alarms and program itself to wake up and connect to wifi when those alarm timings were reached 
  - i.e. it would tell itself to connect to wifi whenever it knew an active alarm timing was coming up 
- The downsides of this approach is that it's complicated, it involved an external server with more security
- The upsides is that it uses wifi so it has longer range than bluetooth and is more stable so you can turn the alarm off anywhere in the house 