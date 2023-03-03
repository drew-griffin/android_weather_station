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

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ConnectFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@Suppress("PrivatePropertyName")
class ConnectFragment : Fragment() {

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val TAG = MainActivity::class.java.simpleName
    private lateinit var communicator: Communicator

    private var _binding: ConnectFragmentBinding? = null
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
        _binding = ConnectFragmentBinding.inflate(inflater, container, false)
        return binding.root
        //return inflater.inflate(R.layout.connect_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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

        communicator = activity as Communicator
        binding.connectButton.setOnClickListener { connectButtonClicked() }

    }

    // go to view data to change fragments
    private fun connectButtonClicked() {
        communicator.viewData(true)
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ConnectFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ConnectFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
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

