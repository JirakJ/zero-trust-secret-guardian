package com.jakubjirak.zerotrust.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.components.JBPasswordField
import com.intellij.util.ui.AsyncProcessIcon
import com.intellij.util.ui.JBUI
import com.jakubjirak.zerotrust.settings.VaultSettings
import com.jakubjirak.zerotrust.api.VaultClientFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.BorderLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.FlowLayout
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JPanel

class ZeroTrustToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val toolWindowContent = ZeroTrustToolWindowContent(project)
        val content = ContentFactory.getInstance().createContent(toolWindowContent.contentPanel, "", false)
        toolWindow.contentManager.addContent(content)
    }
}

class ZeroTrustToolWindowContent(private val project: Project) {
    val contentPanel = JPanel(BorderLayout())
    private val scope = CoroutineScope(Dispatchers.Main)

    init {
        val settingsPanel = JPanel(GridBagLayout())
        val gbc = GridBagConstraints()
        gbc.fill = GridBagConstraints.HORIZONTAL
        gbc.insets = JBUI.insets(5)
        gbc.weightx = 1.0

        val vaultTypeLabel = JBLabel("Vault Provider:")
        val vaultTypeCombo = JComboBox(arrayOf("Infisical", "Doppler"))
        
        val urlLabel = JBLabel("Custom Base URL (Optional):")
        val urlField = JBTextField()
        urlField.emptyText.text = "e.g., https://vault.mycompany.local"

        val tokenLabel = JBLabel("Access Token:")
        val tokenField = JBPasswordField()
        
        val projectLabel = JBLabel("Project ID/Config:")
        val projectField = JBTextField()
        
        val envLabel = JBLabel("Environment:")
        val envCombo = JComboBox(arrayOf("dev", "staging", "prod"))

        val saveButton = JButton("Save & Test Connection")
        val statusLabel = JBLabel("")
        val spinner = AsyncProcessIcon("TestConnectionSpinner")
        spinner.isVisible = false
        
        val statusPanel = JPanel(FlowLayout(FlowLayout.LEFT, 5, 0))
        statusPanel.add(spinner)
        statusPanel.add(statusLabel)

        // Load existing settings
        val settings = VaultSettings.getInstance(project)
        vaultTypeCombo.selectedItem = settings.vaultProvider
        urlField.text = settings.vaultUrl
        projectField.text = settings.vaultProject
        envCombo.selectedItem = settings.environment
        
        val token = settings.getToken()
        if (token != null) {
            tokenField.text = token
        }

        saveButton.addActionListener {
            val selectedProvider = vaultTypeCombo.selectedItem as String
            val selectedUrl = urlField.text
            val selectedProject = projectField.text
            val selectedEnv = envCombo.selectedItem as String
            val enteredToken = String(tokenField.password)

            settings.vaultProvider = selectedProvider
            settings.vaultUrl = selectedUrl
            settings.vaultProject = selectedProject
            settings.environment = selectedEnv
            settings.setToken(enteredToken)

            statusLabel.text = "Testing connection..."
            statusLabel.foreground = com.intellij.ui.JBColor.GRAY
            spinner.isVisible = true
            spinner.resume()
            saveButton.isEnabled = false

            scope.launch {
                try {
                    val client = VaultClientFactory.getClient(selectedProvider)
                    val secrets = withContext(Dispatchers.IO) {
                        client.getSecrets(selectedProject, selectedEnv, enteredToken, selectedUrl)
                    }
                    if (secrets.isNotEmpty()) {
                        statusLabel.text = "Connected! Found \${secrets.size} secrets."
                        statusLabel.foreground = com.intellij.ui.JBColor.GREEN
                    } else {
                        statusLabel.text = "Connected, but no secrets found."
                        statusLabel.foreground = com.intellij.ui.JBColor.ORANGE
                    }
                } catch (e: Exception) {
                    statusLabel.text = "Error: \${e.message}"
                    statusLabel.foreground = com.intellij.ui.JBColor.RED
                } finally {
                    spinner.suspend()
                    spinner.isVisible = false
                    saveButton.isEnabled = true
                }
            }
        }

        var y = 0
        gbc.gridy = y; gbc.gridx = 0; settingsPanel.add(vaultTypeLabel, gbc)
        gbc.gridx = 1; settingsPanel.add(vaultTypeCombo, gbc)
        
        y++
        gbc.gridy = y; gbc.gridx = 0; settingsPanel.add(urlLabel, gbc)
        gbc.gridx = 1; settingsPanel.add(urlField, gbc)

        y++
        gbc.gridy = y; gbc.gridx = 0; settingsPanel.add(tokenLabel, gbc)
        gbc.gridx = 1; settingsPanel.add(tokenField, gbc)
        
        y++
        gbc.gridy = y; gbc.gridx = 0; settingsPanel.add(projectLabel, gbc)
        gbc.gridx = 1; settingsPanel.add(projectField, gbc)
        
        y++
        gbc.gridy = y; gbc.gridx = 0; settingsPanel.add(envLabel, gbc)
        gbc.gridx = 1; settingsPanel.add(envCombo, gbc)
        
        y++
        gbc.gridy = y; gbc.gridx = 0; gbc.gridwidth = 2; settingsPanel.add(saveButton, gbc)
        
        y++
        gbc.gridy = y; gbc.gridx = 0; gbc.gridwidth = 2; settingsPanel.add(statusPanel, gbc)

        contentPanel.add(settingsPanel, BorderLayout.NORTH)
    }
}
