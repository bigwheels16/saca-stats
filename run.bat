REM the service ID that was registered with the API
SET SERVICE_ID=example

REM The characters to listen to (in this case LoveThatLiberator, LoveTheirLiberator, and LoveThisLiberator)
SET SUBSCRIBE_CHARACTER_IDS=5428381682173447713,5428690458390388065,5428569415103706577

REM The events to subscribe to (note Death also includes Kills, as does VehicleDestroy)
SET SUBSCRIBE_EVENTS=VehicleDestroy,Death,PlayerLogin,PlayerLogout

lein run
