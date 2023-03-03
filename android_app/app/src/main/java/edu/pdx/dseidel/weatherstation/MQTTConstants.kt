/**
 * MQTTConstants.kt
 * Provides the file for all of the MQTT constants
 */
package edu.pdx.dseidel.weatherstation

const val MQTT_SERVER_URI_KEY   = "MQTT_SERVER_URI"
const val MQTT_CLIENT_ID_KEY    = "MQTT_CLIENT_ID"
const val MQTT_USERNAME_KEY     = "MQTT_USERNAME"
const val MQTT_PWD_KEY          = "MQTT_PWD"

const val MQTT_SERVER_URI 		= "tcp://broker.hivemq.com"
const val MQTT_CLIENT_ID        = ""
const val MQTT_USERNAME         = ""
const val MQTT_PWD              = ""


//topics and messages for Pico W
const val WEATHER_UPDATE		= "drew/weather_station"
const val LED_STATUS_UPDATE     = "drew/led_status_update"
