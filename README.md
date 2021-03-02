# PS2 Real Time Stats by SACA

This app connects to the PS2 real time web socket API and prints events as they are received. This app can be used as a basis for other projects but likely has limited use on its own in its current state.

## Run with Leiningen (Windows)

Update `run.bat` file with appropriate values and then run.

## Resources on the PS2 API
* http://census.daybreakgames.com/#what-is-websocket - information about the streaming API as well as a list of events that can be subscribed to
* http://census.daybreakgames.com/ps2-websocket.html - page for testing streaming API

## How to get a Service ID
You will need a service ID in order to use the streaming API. Getting a service ID is free and extremely easy and should only take a few seconds.  Fill out the form here: http://census.daybreakgames.com/#service-id

## How to get an Outfit ID
http://census.daybreakgames.com/get/ps2:v2/outfit?alias_lower=saca