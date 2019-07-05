REM the service ID that was registered with the API
SET SERVICE_ID=example

REM The characters to listen to
SET SUBSCRIBE_CHARACTERS=LoveThatLiberator,LoveTheirLiberator,LoveThisLiberator

REM The events to subscribe to (note Death also includes Kills, as does VehicleDestroy)
SET SUBSCRIBE_EVENTS=VehicleDestroy,Death,PlayerLogin,PlayerLogout

REM The Discord webhook URL for posting messages to a Discord channel
SET DISCORD_WEBHOOK_URL=

lein run
