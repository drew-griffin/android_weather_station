# Weather Station 
## Drew Seidel (dseidel@pdx.edu)

Weather Station. Data displayed on custom Android App using and MQTT broker and data collected on a Raspberry Pi Pico W

# Hardware Setup 
- Raspberry Pi Pico W
- bme680 (scl=GP15, sda=GP14)
- external LED with current limiting resistor (GP16)
- firmware located in firmware. Download all files to Pico W and run main.py

# App Setup
- App is located in android_app directory. Open is directory in Android Studio and run and debug as usual

# Note 
broker_secrets.py in the .gitignore for obvious reasons but to recreate the project, create broker_secrets.py in the firmware directory of this repository and fill in the following with your information:

```python 
# Wifi
SSID = ""
PASSWORD = ""

# MQTT
CLIENT_ID = b""  #can be anything
BROKER = b""  #for HiveMQ use broker.hivemq.com
