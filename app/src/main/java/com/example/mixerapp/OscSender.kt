package com.example.mixerapp
import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import com.illposed.osc.OSCListener
import com.illposed.osc.OSCMessage
import com.illposed.osc.OSCPortIn
import com.illposed.osc.OSCPortOut
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress

class OscManager {

    private var oscPortOut: OSCPortOut? = null
    private var oscPortIn: OSCPortIn? = null
    //private val trackLevels = mutableStateMapOf<Int, Float>()


    // Connessione via OSC specificando IP e porta
    suspend fun connect(ip: String, port: Int) = withContext(Dispatchers.IO) {
        Log.d("OSC", "Tentativo di connessione a $ip:$port")
        try {
            val inetAddress = InetAddress.getByName(ip)
            oscPortOut = OSCPortOut(inetAddress, port)
            Log.d("OSC", "Connesso a $ip:$port")
        } catch (e: Exception) {
            Log.e("OSC", "Errore nella connessione: ${e.message}", e)
        }
    }

    /*
    // Invia un messaggio testuale OSC
    suspend fun sendTextMessage(text: String) = withContext(Dispatchers.IO) {
        Log.d("OSC", "Invio messaggio: $text")
        try {
            val message = OSCMessage("/text", listOf(text))
            oscPortOut?.send(message)
            Log.d("OSC", "Messaggio inviato: $text")
        } catch (e: Exception) {
            Log.e("OSC", "Errore invio messaggio: ${e.message}", e)
        }
    }*/

    // Invia il valore del fader del volume (canale 1)
    suspend fun sendVolumeFader(track: Int, value: Float) = withContext(Dispatchers.IO) {
        Log.d("OSC", "Invio volume: $value")
        try {
            val message = OSCMessage("/track/$track/volume", listOf(value))
            oscPortOut?.send(message)
            Log.d("OSC", "Volume inviato: $value")
        } catch (e: Exception) {
            Log.e("OSC", "Errore invio volume: ${e.message}", e)
        }
    }


    // Invia volume al master
    suspend fun sendMasterVolume(value: Float) = withContext(Dispatchers.IO) {
        Log.d("OSC", "Invio volume: $value")
        try {
            val message = OSCMessage("/master/volume", listOf(value))
            oscPortOut?.send(message)
            Log.d("OSC", "Volume inviato: $value")
        } catch (e: Exception) {
            Log.e("OSC", "Errore invio volume: ${e.message}", e)
        }
    }

    //Ascolto Master
    var trackMasterLevelsLeft:  Float = 0.0f
    var trackMasterLevelsRight:  Float = 0.0f

    fun masterListenerL(): Float{
        return trackMasterLevelsLeft
    }
    fun masterListenerR(): Float{
        return trackMasterLevelsRight
    }

    //Ascolto di tutti i canali
    private val trackLevelsLeft = mutableStateMapOf<Int, Float>()
    private val trackLevelsRight = mutableStateMapOf<Int, Float>()

    fun startStereoMeteringListener() {
        try {
            oscPortIn = OSCPortIn(9000)

            oscPortIn?.addListener("/track/*/vu/L", OSCListener { _, message ->
                val track = message.address.split("/")[2].toIntOrNull()
                val value = message.arguments.getOrNull(0) as? Float
                if (track != null && value != null) {
                    trackLevelsLeft[track] = value
                }
            })
            oscPortIn?.addListener("/track/*/vu/R", OSCListener { _, message ->
                val track = message.address.split("/")[2].toIntOrNull()
                val value = message.arguments.getOrNull(0) as? Float
                if (track != null && value != null) {
                    trackLevelsRight[track] = value
                }
            })


            oscPortIn?.addListener("/master/vu/L", OSCListener { _, message ->
                val value = message.arguments.getOrNull(0) as? Float
                if (value != null) {
                    trackMasterLevelsLeft = value
                }
            })
            oscPortIn?.addListener("/master/vu/R", OSCListener { _, message ->
                val value = message.arguments.getOrNull(0) as? Float
                if (value != null) {
                    trackMasterLevelsRight = value
                }
            })

            oscPortIn?.startListening()
        } catch (e: Exception) {
            Log.e("OSC", "Errore ricezione: ${e.message}")
        }
    }

    fun getMeterValueR(track: Int): Float {
        return trackLevelsRight[track] ?: 0f
    }
    fun getMeterValueL(track: Int): Float {
        return trackLevelsLeft[track] ?: 0f
    }


    //Mute Solo Panning
    suspend fun toggleMute(track: Int) = withContext(Dispatchers.IO){
        val message = OSCMessage("/track/$track/mute/toggle")
        oscPortOut?.send(message)
    }

    suspend fun toggleSolo(track: Int) = withContext(Dispatchers.IO){
        val message = OSCMessage("/track/$track/solo/toggle")
        oscPortOut?.send(message)
    }

    suspend fun sendPan(track: Int, panValue: Float) = withContext(Dispatchers.IO){
        val message = OSCMessage("/track/$track/pan", listOf(panValue))
        oscPortOut?.send(message)
    }



    // Disconnessione
    fun disconnect() {
        Log.d("OSC", "Disconnessione in corso...")
        try {
            oscPortOut?.close()
            oscPortOut = null
            Log.d("OSC", "Disconnesso")
        } catch (e: Exception) {
            Log.e("OSC", "Errore disconnessione: ${e.message}", e)
        }
    }
}



