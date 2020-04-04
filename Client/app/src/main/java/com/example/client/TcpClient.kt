package com.example.client

import java.io.*
import java.net.*

class TcpClient(private var strSend: String) {

    var serverResponse: String

    @Throws(Exception::class)
    private fun writeToAndReadFromSocket(
        socket: Socket,
        writeTo: String
    ): String {
        return try {
            // write text to the socket
            val bufferedWriter =
                BufferedWriter(OutputStreamWriter(socket.getOutputStream()))
            bufferedWriter.write(writeTo)
            bufferedWriter.flush()

            // read text from the socket

            val bufferedReader = BufferedReader(InputStreamReader(socket.getInputStream()))
            val sb = StringBuilder()
            var str: String
            while (bufferedReader.readLine().also { str = it } != null) {
                sb.append(
                    """
                        $str
                        
                        """.trimIndent()
                )
               break
            }

            // close the reader, and return the results as a String
            bufferedReader.close()
            sb.toString()
        } catch (e: IOException) {
            e.printStackTrace()
            throw e
        }
    }

    /**
     * Open a socket connection to the given server on the given port.
     * This method currently sets the socket timeout value to 10 seconds.
     * (A second version of this method could allow the user to specify this timeout.)
     */
    @Throws(Exception::class)
    private fun openSocket(server: String, port: Int): Socket {
        val socket: Socket

        // create a socket with a timeout
        return try {
            val inetAddress = InetAddress.getByName(server)
            val socketAddress: SocketAddress =
                InetSocketAddress(inetAddress, port)

            // create a socket
            socket = Socket()

            // this method will block no more than timeout ms.
            val timeoutInMs = 10 * 1000 // 10 seconds
            socket.connect(socketAddress, timeoutInMs)
            socket
        } catch (ste: SocketTimeoutException) {
            System.err.println("Timed out waiting for the socket.")
            ste.printStackTrace()
            throw ste
        }
    }


    init {
        val testServerName = "161.35.10.234"
        val port = 6500
        try {
            // open a socket
            val socket = openSocket(testServerName, port)
            // write-to, and read-from the socket.

            serverResponse = writeToAndReadFromSocket(socket, strSend)

            // close the socket, and we're done
            socket.close()
        } catch (e: Exception) {
            serverResponse = e.toString()
            e.printStackTrace()
        }
    }
}