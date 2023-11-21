package com.github.sufuk.serialportdatasender.toolWindow

import com.fazecast.jSerialComm.SerialPort
import com.github.sufuk.serialportdatasender.SerialPortHandler
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import java.awt.*
import javax.swing.JButton


class MyToolWindowFactory : ToolWindowFactory {

    init {
        thisLogger().warn("Don't forget to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.")
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = MyToolWindow(toolWindow)
        val content = ContentFactory.getInstance().createContent(myToolWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true

    class MyToolWindow(private val toolWindow: ToolWindow) {

        private val portComboBox = ComboBox<String>()
        private val fileTextField = TextFieldWithBrowseButton()
        private val textField = JBTextField()
        private val checkbox = JBCheckBox("Send size")
        init {
            setupFileSelection()
            refreshPortList()
        }

        private fun setupFileSelection() {
            val fileChooserDescriptor = FileChooserDescriptor(true, false, false, false, false, false)
            fileTextField.addBrowseFolderListener(
                "Select a File",
                null,
                null,
                fileChooserDescriptor
            )
        }

        private fun refreshPortList() {
            val portNames = getListOfPorts()
            portComboBox.removeAllItems()
            portNames.forEach { portComboBox.addItem(it) }
        }

        private fun clearEverything() {
            portComboBox.removeAllItems()
            fileTextField.text = ""
            textField.text = ""
        }

        private fun downloadFile(sendSize: Boolean) {
            val result = SerialPortHandler.downloadFile(portComboBox.selectedItem?.toString(), fileTextField.text.trim(), sendSize)
            when (result) {
                SerialPortHandler.Result.FileNotFound -> {
                    Messages.showMessageDialog(
                        "File not found",
                        "Error",
                        Messages.getErrorIcon()
                    )
                }
                SerialPortHandler.Result.FileEmpty -> {
                    Messages.showMessageDialog(
                        "File is empty",
                        "Error",
                        Messages.getErrorIcon()
                    )
                }
                SerialPortHandler.Result.PortNotFound -> {
                    Messages.showMessageDialog(
                        "Port is not open",
                        "Error",
                        Messages.getErrorIcon()
                    )
                }
                SerialPortHandler.Result.Success -> {
                    Messages.showMessageDialog(
                        "File sent successfully",
                        "Information",
                        Messages.getInformationIcon()
                    )
                }
            }
        }
        private fun sendData(sendSize: Boolean) {
            val result = SerialPortHandler.sendData(textField.text.toString(), fileTextField.text.trim(), sendSize)
            if (result == SerialPortHandler.Result.PortNotFound) {
                Messages.showMessageDialog(
                    "Port is not open",
                    "Error",
                    Messages.getErrorIcon()
                )
            } else if (result == SerialPortHandler.Result.Success) {
                Messages.showMessageDialog(
                    "File sent successfully",
                    "Information",
                    Messages.getInformationIcon()
                )
            }

        }
        fun getContent(): JBPanel<JBPanel<*>> {
            val topPanel = JBPanel<JBPanel<*>>().apply {
//                layout = BorderLayout()
//                add(mainPanel, BorderLayout.CENTER)
                layout = GridBagLayout()
                val comboConstraints = GridBagConstraints()
                comboConstraints.fill = GridBagConstraints.HORIZONTAL
                comboConstraints.gridx = 0
                comboConstraints.gridy = 0
                comboConstraints.weightx = 1.0
                add(portComboBox, comboConstraints)

                val refreshButtonConstraints = GridBagConstraints()
                refreshButtonConstraints.fill = GridBagConstraints.HORIZONTAL
                refreshButtonConstraints.gridx = 1
                refreshButtonConstraints.gridy = 0
                refreshButtonConstraints.weightx = 1.0
                val refreshButton = JButton("Refresh").apply {
                    addActionListener {
                        refreshPortList()
                    }
                }
                add(refreshButton, refreshButtonConstraints)

                val resetButtonConstraints = GridBagConstraints()
                resetButtonConstraints.fill = GridBagConstraints.HORIZONTAL
                resetButtonConstraints.gridx = 2
                resetButtonConstraints.gridy = 0
                resetButtonConstraints.weightx = 1.0
                val resetButton = JButton("Reset").apply {
                    addActionListener {
                        clearEverything()
                    }
                }
                add(resetButton, resetButtonConstraints)


                val fileLabelConstraints = GridBagConstraints()
                fileLabelConstraints.fill = GridBagConstraints.HORIZONTAL
                fileLabelConstraints.gridx = 0
                fileLabelConstraints.gridy = 1
                fileLabelConstraints.weightx = 1.0
                add(JBLabel("File path:"), fileLabelConstraints)

                val fileConstraints = GridBagConstraints()
                fileConstraints.fill = GridBagConstraints.HORIZONTAL
                fileConstraints.gridx = 1
                fileConstraints.gridy = 1
                fileConstraints.weightx = 1.0
                fileConstraints.gridwidth = 2
                add(fileTextField, fileConstraints)

                val textLabelConstraints = GridBagConstraints()
                textLabelConstraints.fill = GridBagConstraints.HORIZONTAL
                textLabelConstraints.gridx = 0
                textLabelConstraints.gridy = 2
                textLabelConstraints.weightx = 1.0
                add(JBLabel("Text:"), textLabelConstraints)

                val textConstraints = GridBagConstraints()
                textConstraints.fill = GridBagConstraints.HORIZONTAL
                textConstraints.gridx = 1
                textConstraints.gridy = 2
                textConstraints.weightx = 1.0
//                textConstraints.weighty = 0.0 // Prevent vertical growth
                textConstraints.gridwidth = 2
                add(textField, textConstraints)

                val downloadButtonConstraints = GridBagConstraints()
                downloadButtonConstraints.fill = GridBagConstraints.HORIZONTAL
                downloadButtonConstraints.gridx = 0
                downloadButtonConstraints.gridy = 3
                downloadButtonConstraints.weightx = 1.0
                downloadButtonConstraints.gridwidth = 2
                val downloadButton = JButton("Download").apply {
                    addActionListener {
                        if (fileTextField.text.isNotEmpty()){
                            textField.text = ""
                            println("checkbox.isSelected : " + checkbox.isSelected)
                            downloadFile(checkbox.isSelected)
                        }
                        else if (textField.text.isNotEmpty()){
                            sendData(checkbox.isSelected)
                        }
                        else{
                            Messages.showMessageDialog(
                                "Please select a file or enter text",
                                "Error",
                                Messages.getErrorIcon()
                            )
                        }


                    }
                }
                add(downloadButton, downloadButtonConstraints)

                val checkBoxConstraints = GridBagConstraints()
                checkBoxConstraints.fill = GridBagConstraints.HORIZONTAL
                checkBoxConstraints.gridx = 2
                checkBoxConstraints.gridy = 3
                checkBoxConstraints.weightx = 1.0
                checkBoxConstraints.gridwidth = 1
                add(checkbox, checkBoxConstraints)

            }
            val panel = JBPanel<JBPanel<*>>().apply {
                layout = BorderLayout()
                add(topPanel, BorderLayout.NORTH)
            }

            return panel
        }


        private fun getListOfPorts(): List<String> {
            val ports = SerialPort.getCommPorts()
            return ports.map { it.systemPortName }
        }
    }
}
