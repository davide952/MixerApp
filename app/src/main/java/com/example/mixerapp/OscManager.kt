/*
package com.example.mixerapp

import android.util.Log
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class MidiUdpSender(private val ipAddress: String, private val port: Int) {

    private val socket: DatagramSocket = DatagramSocket()

    // Invia un messaggio testuale via UDP
    fun sendTextMessage(message: String) {
        try {
            val data = message.toByteArray()
            val address = InetAddress.getByName(ipAddress)
            val packet = DatagramPacket(data, data.size, address, port)
            socket.send(packet)
            Log.d("UDP", "Messaggio inviato: $message")
        } catch (e: Exception) {
            Log.e("UDP", "Errore nell'invio del messaggio: ${e.toString()}")
        }
    }

    // Invia un messaggio MIDI Control Change via UDP
    // Per esempio: channel 0 (MIDI canale 1), controller 7 per il volume, value da 0 a 127.
    fun sendControlChange(channel: Int, controller: Int, value: Int) {
        try {
            val statusByte = 0xB0 or (channel and 0x0F) // Messaggio Control Change per il canale specificato
            val data = byteArrayOf(statusByte.toByte(), controller.toByte(), value.toByte())
            val address = InetAddress.getByName(ipAddress)
            val packet = DatagramPacket(data, data.size, address, port)
            socket.send(packet)
            Log.d("UDP", "Control Change inviato: channel=$channel, controller=$controller, value=$value")
        } catch (e: Exception) {
            Log.e("UDP", "Errore nell'invio del Control Change: ${e.toString()}")
        }
    }

    fun close() {
        socket.close()
    }
}
*/

package com.example.mixerapp

import android.util.Log
import java.io.IOException
import java.net.*

class MidiRtpSender(private val remoteIp: String, private val remotePort: Int) {
    private var socket: DatagramSocket? = null
    private var sequenceNumber = 0 // Contatore per i pacchetti RTP

    init {
        try {
            socket = DatagramSocket()
            socket?.soTimeout = 1000
        } catch (e: SocketException) {
            Log.e("RTP", "Errore inizializzazione socket: ${e.message}")
        }
    }

    // Invia un messaggio MIDI formattato come RTP-MIDI
    fun sendMidiMessage(command: Byte, data1: Byte, data2: Byte) {
        try {
            // Costruisci il payload MIDI (1-3 byte)
            val midiData = byteArrayOf(command, data1, data2)

            // Costruisci l'header RTP (RFC 3550 + MIDI specifiche)
            val rtpHeader = ByteArray(12)
            rtpHeader[0] = 0x80.toByte()  // Versione RTP (2)
            rtpHeader[1] = 0x61.toByte()  // Payload type per MIDI (97)
            rtpHeader[2] = (sequenceNumber shr 8).toByte()  // Sequence number (high byte)
            rtpHeader[3] = (sequenceNumber and 0xFF).toByte()  // Sequence number (low byte)
            rtpHeader[4] = 0  // Timestamp (32 bit, gestito opzionalmente)
            rtpHeader[5] = 0
            rtpHeader[6] = 0
            rtpHeader[7] = 0
            rtpHeader[8] = 0  // SSRC (identificativo sorgente)
            rtpHeader[9] = 0
            rtpHeader[10] = 0
            rtpHeader[11] = 0

            // Unisci header RTP e payload MIDI
            val packetData = rtpHeader + midiData

            // Invia il pacchetto
            val address = InetAddress.getByName(remoteIp)
            val packet = DatagramPacket(packetData, packetData.size, address, remotePort)
            socket?.send(packet)
            sequenceNumber++

            Log.d("RTP", "MIDI inviato: ${command.toHex()} ${data1.toHex()} ${data2.toHex()}")
        } catch (e: IOException) {
            Log.e("RTP", "Errore invio: ${e.message}")
        }
    }

    fun close() {
        socket?.close()
    }

    // Utility per convertire byte in esadecimale
    private fun Byte.toHex(): String = String.format("%02X", this)
}