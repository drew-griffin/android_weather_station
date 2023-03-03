/**
 * MainActivity.kt
 * @author Drew Seidel (dseidel@pdx.edu)
 * This file is the main activity that keeps track of, and hosts two fragments that take up the entire screen
 * The first is the connect screen, the user can click connect to attempt to connect to MQTT
 *
 * The second, upon successful connection, will host the weather station app
 *
 */

package edu.pdx.dseidel.weatherstation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import edu.pdx.dseidel.weatherstation.databinding.ActivityMainBinding

var connected = false

class MainActivity : AppCompatActivity(), Communicator{
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //open first connect fragment on start
        if(!connected) {
            val connectFragment = ConnectFragment()
            supportFragmentManager.beginTransaction().replace(R.id.view_manager, connectFragment)
                .commit()
        }
    }

    //upon connect button clicked on main screen, go to second fragment for weather station
    //interaction
    override fun viewData(connected: Boolean)
    {
        if (connected){
            val transaction = this.supportFragmentManager.beginTransaction()
            val viewDataFragment = ViewDataFragment()
            transaction.replace(R.id.view_manager, viewDataFragment)
            transaction.addToBackStack(null)
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            transaction.commit()
        }
    }

    //upon disconnect button pressed in second fragment, go back to the home screen
    override fun disconnect() {
        val transaction = this.supportFragmentManager.beginTransaction()
        val connectFragment = ConnectFragment()
        transaction.replace(R.id.view_manager, connectFragment)
        transaction.addToBackStack(null)
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction.commit()
    }

    override fun onBackPressed()
    {
        //do not do anything upon back button press as
        //the connect and disconnect buttons are for that
    }
}