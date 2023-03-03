package edu.pdx.dseidel.weatherstation

//Communicator interface to be utilized as override functions
//in main activity.
interface Communicator {
    fun viewData(connected: Boolean)

    fun disconnect()
}