""""
import machine

led = machine.Pin("LED", machine.Pin.OUT)
timer = machine.Timer()

def blink(timer):
    print("hello world"); 
    led.toggle()
    
timer.init(freq=2.5, mode=machine.Timer.PERIODIC, callback=blink)
"""

import network
import time
from machine import Pin, I2C
from umqtt.simple import MQTTClient
#import adafruit_bme680
from bme680 import *
import broker_secrets
import json 

i2c = I2C(id=1,scl=Pin(15),sda=Pin(14),freq=400000)

bme680 = BME680_I2C(i2c)


wlan = network.WLAN(network.STA_IF)
wlan.active(True)
wlan.connect(broker_secrets.SSID,broker_secrets.PASSWORD)
time.sleep(5)
print(wlan.isconnected())

#sensor = Pin(16, Pin.IN)

mqtt_broker = broker_secrets.BROKER
client_id = broker_secrets.CLIENT_ID
topic_pub = b'drew/weather_station'


def weather_update():     
    topic_msg = {
             f"Temperature = {bme680.temperature}",
             f"Gas = {bme680.gas}",
             f"Humidity = {bme680.humidity}",
             f"Pressure = {bme680.pressure}",
             f"Altitude = {bme680.altitude}"
             }
    return topic_msg

def mqtt_connect():
    client = MQTTClient(client_id, mqtt_broker, keepalive=3600)
    client.connect()
    print('Connected to %s MQTT Broker'%mqtt_broker)
    return client

def reconnect():
    print('Failed to connect to the MQTT Broker. Reconnecting...')
    time.sleep(5)
    machine.reset()

try:
    client = mqtt_connect()
except OSError as e:
    reconnect()
while True:
    print("\nTemperature: %0.1f C" % bme680.temperature )
    print("Gas: %d ohm" % bme680.gas)
    print("Humidity: %0.1f %%" % bme680.humidity)
    print("Pressure: %0.3f hPa" % bme680.pressure)
    print("Altitude = %0.2f meters" % bme680.altitude)
    topic_msg = weather_update()
    client.publish(topic_pub, json.dumps(topic_msg))
    time.sleep(1)
