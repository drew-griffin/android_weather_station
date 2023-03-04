""""
main.py

author: Drew Seidel (dseidel@pdx.edu)

brief: This script collects weather data (humidity, temperature, altitude, gas, and pressure data)
from the BME680 device, and publishes this using and MQTT broker (hive mq in this case)
as a single formated JSON string, that the android app parses, and displays
Furthermore, this devices receives a JSON string formatted object with information about two 
leds, the onboard LED, and an external LED mapped to GP16. The onboard LED is controlled 
by a switch on the Android App, and the external LED turns on if the temperature is above 70 degrees 
farenheit

reference to: 

https://github.com/robert-hh/BME680-Micropython for I2C MicroPython BME 680 driver modified from Adafruit CircuitPython
https://www.tomshardware.com/how-to/send-and-receive-data-raspberry-pi-pico-w-mqtt
project release  .../ece558w23_proj2_release/starters/aht20_picoW_example/* 


"""

import network
import time
from machine import Pin, I2C
from umqtt.simple import MQTTClient
from bme680 import *
import broker_secrets
import json 

#Global variables 
i2c = I2C(id=1,scl=Pin(15),sda=Pin(14),freq=400000)
bme680 = BME680_I2C(i2c)
board_led = machine.Pin("LED", machine.Pin.OUT)
temp_led = machine.Pin(16, machine.Pin.OUT)


wlan = network.WLAN(network.STA_IF)
wlan.active(True)
wlan.connect(broker_secrets.SSID,broker_secrets.PASSWORD)
time.sleep(5)
print(wlan.isconnected())

mqtt_broker = broker_secrets.BROKER
client_id = broker_secrets.CLIENT_ID
topic_pub = b'drew/weather_station'
led_topic_sub = b'drew/led_status_update'

#handle LEDs callback to subscription led_topic_sub
def handle_leds(topic, msg):
    if (topic == led_topic_sub):  #should always be true, but check in case
        message = json.loads(msg) #convert JSON object to Python Dictionary
        if (message["BOARD_LED"] == "ON"):
            board_led.on()
            print("User turning on board LED") 
        else:
            board_led.off()
            print("User turning off board LED") 
        if (message["TEMP_LED"] == "ON"):
            temp_led.on()
            print("Temperature above 70˚F. Turning on temp LED") 
        else:
            temp_led.off()
            print("Temperature dropped below 70˚F. Turning off temp LED") 


def weather_update():     
    topic_msg = {
             f"Temperature":  f"{((bme680.temperature - 5) * 9/5 + 32):.2f}",
             f"Gas":  f"{bme680.gas:.2f}",
             f"Humidity":  f"{bme680.humidity:.2f}",
             f"Pressure": f"{bme680.pressure:.2f}",
             f"Altitude": f"{bme680.altitude:.2f}"
             }
    return topic_msg

def mqtt_connect():
    client = MQTTClient(client_id, mqtt_broker, keepalive=3600)
    client.connect()
    print('Connected to %s MQTT Broker'%mqtt_broker)
    client.set_callback(handle_leds) #set callback handler to leds
    client.subscribe(led_topic_sub)  #subsribed to LED topic 
    return client

def reconnect():
    print('Failed to connect to the MQTT Broker. Reconnecting...')
    time.sleep(5)
    machine.reset()
    

try:
    client = mqtt_connect()
except OSError as e:
    reconnect()
# main loop 
while True:
    # display measurements on console. Offset by -5 degrees for PCB temp 
    print("\nTemperature: %0.1f F" % ((bme680.temperature - 5) * 9/5 + 32) )
    print("Gas: %d ohm" % bme680.gas)
    print("Humidity: %0.1f %%" % bme680.humidity)
    print("Pressure: %0.3f hPa" % bme680.pressure)
    print("Altitude:  %0.2f meters" % bme680.altitude)
    topic_msg = weather_update()                     #call weather update to format string
    client.publish(topic_pub, json.dumps(topic_msg)) #publish topic message as JSON
    client.check_msg()                               #check subscription message
    time.sleep(1)                                    #1 second
