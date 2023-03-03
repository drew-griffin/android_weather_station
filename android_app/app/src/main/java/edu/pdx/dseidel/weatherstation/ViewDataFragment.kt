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

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private lateinit var communicator: Communicator

/**
 * A simple [Fragment] subclass.
 * Use the [ViewDataFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ViewDataFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val TAG = MainActivity::class.java.simpleName

    private lateinit var mqttClient : MQTTClient
    private lateinit var mqttClientID: String
    private lateinit var communicator: Communicator

    private var highTemp : Boolean = false
    private var boardLedOn : Boolean = false
    private lateinit var led_STATUS : String


    private var _binding: ViewDataFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
           super.onCreate(savedInstanceState)

        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }



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

        // open mQTT Broker communication
        mqttClientID = MqttClient.generateClientId()
        mqttClient = MQTTClient(activity, MQTT_SERVER_URI, mqttClientID)

        connectMQTT()
        communicator = activity as Communicator
        binding.disconnect.setOnClickListener { disconnectButtonClicked() }


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

            object : MqttCallback {
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
                       val temp : Double? = tempString.toDouble()

                        if (temp != null) {
                            if (temp >= 70 && !highTemp) {
                                highTemp = true
                                ledHandler(LED_STATUS_UPDATE)
                            } else if (temp < 70 && highTemp) {
                                highTemp = false
                                ledHandler(LED_STATUS_UPDATE)
                            }
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
        mqttClient.disconnect()
        communicator.disconnect()
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
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ViewData.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ViewDataFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}