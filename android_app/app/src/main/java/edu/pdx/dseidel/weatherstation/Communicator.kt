/**
 * Communicator.kt
 * @author Drew Seidel (dseidel@pdx.edu)
 * 
 * provides interface for Communicator 
 * two override functions utilized in MainActivity.kt
 * to switch between fragments
 */

package edu.pdx.dseidel.weatherstation

//Communicator interface to be utilized as override functions
//in main activity.
interface Communicator {
    fun viewData(connected: Boolean)
    fun disconnect()
}