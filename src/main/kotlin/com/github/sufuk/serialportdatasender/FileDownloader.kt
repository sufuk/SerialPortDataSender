package com.github.sufuk.serialportdatasender

import com.fazecast.jSerialComm.SerialPort
import java.io.File

class FileDownloader {
    enum class Result {
        Success,
        FileNotFound,
        FileEmpty,
        PortNotFound
    }

    companion object {
        @OptIn(ExperimentalStdlibApi::class)
        public fun downloadFile(portName: String?, fileName: String): Result {

            if (!File(fileName).exists())
                return Result.FileNotFound

            val fileContent = File(fileName).readBytes()

            if (fileContent.isEmpty())
                return Result.FileEmpty

            val port = SerialPort.getCommPort(portName)
            port.openPort()

            if (!port.isOpen)
                return Result.PortNotFound


            //Send the size of the file
            val fileSizeBuffer = fileContent.size.toString().toByteArray()
            var bytesWritten = port.writeBytes(
                fileSizeBuffer,
                fileSizeBuffer.size.toLong()
            )
            println("Bytes written for file size: $bytesWritten")
//            println("fileSizeBuffer : " + fileSizeBuffer.toHexString())

            //Then send the file content
            bytesWritten = port.writeBytes(fileContent, fileContent.size.toLong())
            println("Bytes written for file content: $bytesWritten")
//            println("fileContent : " + fileContent.toHexString())

            port.closePort()
            return Result.Success
        }
    }
}