package com.github.sufuk.serialportdatasender.toolWindow

import com.fazecast.jSerialComm.SerialPort
import com.github.sufuk.serialportdatasender.FileDownloader
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import com.github.sufuk.serialportdatasender.MyBundle
import com.github.sufuk.serialportdatasender.services.MyProjectService
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import java.awt.BorderLayout
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
        }

        private fun downloadFile() {
            val result = FileDownloader.downloadFile(portComboBox.selectedItem?.toString(), fileTextField.text.trim())
            when (result) {
                FileDownloader.Result.FileNotFound -> {
                    Messages.showMessageDialog(
                        "File not found",
                        "Error",
                        Messages.getErrorIcon()
                    )
                }
                FileDownloader.Result.FileEmpty -> {
                    Messages.showMessageDialog(
                        "File is empty",
                        "Error",
                        Messages.getErrorIcon()
                    )
                }
                FileDownloader.Result.PortNotFound -> {
                    Messages.showMessageDialog(
                        "Port is not open",
                        "Error",
                        Messages.getErrorIcon()
                    )
                }
                FileDownloader.Result.Success -> {
                    Messages.showMessageDialog(
                        "File sent successfully",
                        "Information",
                        Messages.getInformationIcon()
                    )
                }
            }
        }
        fun getContent(): JBPanel<JBPanel<*>> {
            val topPanel = JBPanel<JBPanel<*>>().apply {
                layout = BorderLayout()
                add(portComboBox, BorderLayout.WEST)
                add(JButton("Refresh").apply {
                    addActionListener {
                        refreshPortList()
                    }
                }, BorderLayout.CENTER)
                add(JButton("Reset").apply {
                    addActionListener {
                        clearEverything()
                    }
                }, BorderLayout.EAST)
            }

            val bottomPanel = JBPanel<JBPanel<*>>().apply {
                layout = BorderLayout()
                add(fileTextField, BorderLayout.NORTH)
                add(JButton("Download").apply {
                    addActionListener {
                        downloadFile()
                    }
                }, BorderLayout.CENTER)
            }

            val mainPanel = JBPanel<JBPanel<*>>().apply {
                layout = BorderLayout()
                add(topPanel, BorderLayout.NORTH)
                add(bottomPanel, BorderLayout.CENTER)
            }

            val panel = JBPanel<JBPanel<*>>().apply {
                layout = BorderLayout()
                add(mainPanel, BorderLayout.CENTER)
            }

            return panel
        }

        private fun getListOfPorts(): List<String> {
            val ports = SerialPort.getCommPorts()
            return ports.map { it.systemPortName }
        }
    }
}
