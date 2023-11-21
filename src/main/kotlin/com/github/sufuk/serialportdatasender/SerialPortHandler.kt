package com.github.sufuk.serialportdatasender

import com.fazecast.jSerialComm.SerialPort
import io.ktor.utils.io.core.*
import java.io.File
import kotlin.text.toByteArray

class SerialPortHandler {
    enum class Result {
        Success,
        FileNotFound,
        FileEmpty,
        PortNotFound
    }

    companion object {
        public fun downloadFile(portName: String?, fileName: String, sendSize: Boolean): Result {

            if (!File(fileName).exists())
                return Result.FileNotFound

            val fileContent = File(fileName).readBytes()

            if (fileContent.isEmpty())
                return Result.FileEmpty

            val port = SerialPort.getCommPort(portName)
            port.openPort()

            if (!port.isOpen)
                return Result.PortNotFound

            if (sendSize){
                //Send the size of the file
                val fileSizeBuffer = fileContent.size.toString().toByteArray()
                val bytesWrittenSize = port.writeBytes(
                    fileSizeBuffer,
                    fileSizeBuffer.size.toLong()
                )
                println("Bytes written for file size: $bytesWrittenSize")

            }
//            println("fileSizeBuffer : " + fileSizeBuffer.toHexString())

            //Then send the file content
            val bytesWritten = port.writeBytes(fileContent, fileContent.size.toLong())
            println("Bytes written for file content: $bytesWritten")
//            println("fileContent : " + fileContent.toHexString())

            port.closePort()
            return Result.Success
        }

        public fun sendData(portName: String?, data: String, sendSize: Boolean): Result {

            val port = SerialPort.getCommPort(portName)
            port.openPort()

            if (!port.isOpen)
                return Result.PortNotFound


            if (sendSize){
                //Send the size of the file
                val sizeBuffer = data.toByteArray().size.toLong().toString().toByteArray()
                val bytesWrittenSize = port.writeBytes(
                    sizeBuffer,
                    sizeBuffer.size.toLong()
                )
                println("Bytes written for size: $bytesWrittenSize")

            }

            //Then send data
            val bytesWritten = port.writeBytes(data.toByteArray(), data.toByteArray().size.toLong())
            println("Bytes written for data: $bytesWritten")

            port.closePort()
            return Result.Success
        }
    }
}