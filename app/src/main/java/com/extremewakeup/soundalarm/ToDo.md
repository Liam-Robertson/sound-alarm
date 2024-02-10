To do list:
- Create delete alarm button
- Make it a https request instead of http
- Delete the security config you made to bypass https and remove this line from the manifest @xml/network_security_config
- If you fail to scan the correct qr code it shouldn't navigate away from the qr code scanner, it should stay on the qr code scanner and give an error message

Medium term to do:
- Create a timeout so that the alarm won't go infinitely if the user loses the barcode
- Let the user configure this timeout (but don't let them change it during the alarm time)
- If you accidentally exit the alarm screen there needs to be a way to re-enter it. Otherwise you can't turn off alarm.
- Ask for camera permissions at the start of the app, not when the bardcode scanner is used
- Give an app notification saying that the alarm is going off

Long term to do:
- Create a default alarm which is quiet wake up noises getting gradually louder then 30 seconds in it turns into a claxon
- Create a setup test where you force the user to test the alarm with a barcode
     - This ensures that the user has the barcode printed out and knows how to use it
- Create a popup that says something like "If I lose this barcode I will not be able to turn off this alarm" with a checkbox saying "I understand and agree"
- Right now the firebase server is only in Europe, create an american one and get American devices to route to that 
- Start and stop the alarm using wifi instead of bluetooth? 

Bugs:
- If you exit the barcode scanner, then you won't be able to reschedule any alarms

