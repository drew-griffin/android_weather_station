/**
 * ConnectFragment.kt
 * @author Drew Seidel (dseidel@pdx.edu)
 *
 * This file implements the initial fragment for the app.
 * This fragment allows the enter to click connect to enter the main
 * weather station functionality
 *
 */

package edu.pdx.dseidel.weatherstation

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import edu.pdx.dseidel.weatherstation.databinding.ConnectFragmentBinding
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttClient

@Suppress("PrivatePropertyName")
class ConnectFragment : Fragment() {

    private val TAG = MainActivity::class.java.simpleName
    private lateinit var communicator: Communicator

    private var _binding: ConnectFragmentBinding? = null
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = ConnectFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /* On successful view creation, do the following */

        // Check if Internet connection is available
        // exit if it is not
        if (!isConnected()) {
            Log.d(TAG, "Internet connection NOT available")
            Toast.makeText(activity, "Internet connection NOT available", Toast.LENGTH_LONG).show()
            activity?.finish()
        } else {
            Log.d(TAG, "Connected to the Internet")
            Toast.makeText(activity, "Connected to the Internet", Toast.LENGTH_LONG).show()
        }

        communicator = activity as Communicator //initialize the communicator used to go between fragments

        binding.connectButton.setOnClickListener { connectButtonClicked() } //set callback method for when button is pressed

    }

    // go to view data method in Main Activity to change fragments
    private fun connectButtonClicked() {
        communicator.viewData(true)
    }

    // helper functions
    private fun isConnected(): Boolean {
        var result = false
        val cm = activity?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)
        if (capabilities != null) {
            result = when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> true
                else -> false
            }
        }
        return result
    }

}

