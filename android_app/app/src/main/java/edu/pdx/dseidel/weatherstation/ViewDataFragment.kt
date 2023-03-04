/**
 * ViewDataFragment.kt
 * @author Drew Seidel (dseidel@pdx.edu)
 * This file implements the view data fragment for viewing pressure, altitude, humidity, gas, and temperature
 * that is published via MQTT on the Pico W. It subscribes to the topic 'drew/weather_station' that has this information
 * as a JSON object, and parses through to display the data on the screen
 *
 * This file publishes the topic 'drew/led_status_update' packaged as a JSON string, which carries information for
 * weather the board LED is on or not (specified by a switch button in the fragment), or whether an external LED is on or not,
 * specified by weather the temperature is over 70% or not.
 *
 */

package edu.pdx.dseidel.weatherstation

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import edu.pdx.dseidel.weatherstation.databinding.ViewDataFragmentBinding
import org.eclipse.paho.client.mqttv3.*
import org.json.JSONException
import org.json.JSONObject


@Suppress("PrivatePropertyName")
class ViewDataFragment : Fragment() {

    private val TAG = MainActivity::class.java.simpleName


    private lateinit var mqttClient : MQTTClient
    private lateinit var mqttClientID: String
    private lateinit var communicator: Communicator //used to communicate to first fragment on disconnect

    private var highTemp : Boolean = false  //true if over 70 f, false otherwise
    private var boardLedOn : Boolean = false //true if led switch turned on, false otherwise, initialized to false
    private lateinit var led_STATUS : String //led string to be JSON formatted to send to Raspberry Pico W, containing status of LEDS


    private var _binding: ViewDataFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
           super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = ViewDataFragmentBinding.inflate(inflater, container, false)
        return binding.root //inflater.inflate(R.layout.view_data_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /* On successful view creation, do the following */

        // open mQTT Broker communication
        mqttClientID = MqttClient.generateClientId()
        mqttClient = MQTTClient(activity, MQTT_SERVER_URI, mqttClientID)
        connectMQTT()

        //initialize communicator
        communicator = activity as Communicator

        //set callback method for disconnect button
        binding.disconnect.setOnClickListener { disconnectButtonClicked() }

        //create method for handling the switch for the onboard Pico W LED
        binding.LEDSwitch.setOnCheckedChangeListener { _, isChecked ->
            boardLedOn = isChecked
            ledHandler(LED_STATUS_UPDATE)
        }

    }


    private fun connectMQTT(){
        mqttClient.connect(
            MQTT_USERNAME,
            MQTT_PWD,
            object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Connection success")

                    val successMsg = "MQTT Connection to $MQTT_SERVER_URI Established"
                    Toast.makeText(activity, successMsg, Toast.LENGTH_LONG).show()

                    // subscribe to the status topics
                    subscribeToStatus(WEATHER_UPDATE)
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Connection failure: ${exception.toString()}")
                    val failureMsg =
                        "MQTT Connection to $MQTT_SERVER_URI failed: ${exception?.toString()}"
                    Toast.makeText(activity, failureMsg, Toast.LENGTH_LONG).show()
                    exception?.printStackTrace()
                }
            },
            //MqttCallback object provides the main brains for the frontend UI
            //upon MQTT message arrival, we parse the JSON formatted string,
            //update the display, check the temperature, and if there was a
            //change from the previous state, update highTemp, and call the ledHandler
            object : MqttCallback {
                @SuppressLint("SetTextI18n")
                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    val msg = "Received message: ${message.toString()} from topic: $topic"
                    Log.d(TAG, msg)

                    // since a message arrived I'm assuming that the topic string is not null
                    // update all weather data from JSON formatted incoming string
                    if (topic!! == WEATHER_UPDATE) {
                        val weatherData = JSONObject(message.toString())
                        binding.updateTemp.text = weatherData.getString("Temperature") + " ºF"
                        binding.updateGas.text = weatherData.getString("Gas") + " Ω"
                        binding.humidityUpdate.text = weatherData.getString("Humidity") + " %"
                        binding.pressureUpdate.text = weatherData.getString("Pressure") + " hPA"
                        binding.altitudeUpdate.text = weatherData.getString("Altitude") + " M"

                       val tempString = weatherData.getString("Temperature")
                       val temp : Double = tempString.toDouble()

                        if ((temp >= 70) && !highTemp) {
                            highTemp = true
                            ledHandler(LED_STATUS_UPDATE)
                        } else if ((temp < 70) && highTemp) {
                            highTemp = false
                            ledHandler(LED_STATUS_UPDATE)
                        }
                    }
                }

                override fun connectionLost(cause: Throwable?) {
                    Log.d(TAG, "Connection lost ${cause.toString()}")
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                    Log.d(TAG, "Delivery complete")
                }
            })
    }

    //handle LED and send publish topic as JSON formatted string
    private fun ledHandler(topic: String){

        val boardLEDStatusString = if (boardLedOn) "ON" else "OFF"
        val tempLEDStatusString = if (highTemp) "ON" else "OFF"

        if (mqttClient.isConnected()) {
            val messageJSON = JSONObject()
            messageJSON.put("BOARD_LED", boardLEDStatusString)
            messageJSON.put("TEMP_LED", tempLEDStatusString)
            val message = messageJSON.toString()
            mqttClient.publish(
                topic,
                message,
                1,
                false,
                object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        val msg = "Successfully published topic: $topic"
                        Log.d(TAG, msg)
                    }

                    override fun onFailure(
                        asyncActionToken: IMqttToken?,
                        exception: Throwable?
                    ) {
                        val msg =
                            "Failed to publish: to topic: $topic exception: ${exception.toString()}"
                        Log.d(TAG, msg)
                    }
                })
        } else {
            Log.d(TAG, "Impossible to publish, no server connected")
        }
    }
    private fun disconnectButtonClicked() {
        mqttClient.disconnect() //disconnect from MQTT
        communicator.disconnect() //use the communicator to call the disconnect method in Main Activity
    }

    private fun subscribeToStatus(subscribeTopic: String) {
        // subscribe to status topic only if connected to broker
        if (mqttClient.isConnected()) {
            mqttClient.subscribe(
                topic = subscribeTopic,
                qos = 1,
                object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        val msg = "Subscribed to: $subscribeTopic"
                        Log.d(TAG, msg)
                        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
                    }

                    override fun onFailure(
                        asyncActionToken: IMqttToken?,
                        exception: Throwable?
                    ) {
                        Log.d(
                            TAG, "Failed to subscribe: $WEATHER_UPDATE exception: ${exception.toString()}"
                        )
                    }
                })
        } else {
            Log.d(TAG, "Cannot subscribe to $WEATHER_UPDATE: Not connected to server")
        }
    }
}