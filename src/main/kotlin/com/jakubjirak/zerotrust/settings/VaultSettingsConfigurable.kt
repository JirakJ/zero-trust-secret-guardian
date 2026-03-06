/*
 * Copyright (c) 2024-2025 Jakub Jirak. All rights reserved.
 * Licensed under the terms of the project license.
 */
package com.jakubjirak.zerotrust.settings

import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * Project-level configurable for the Zero-Trust Secret Guardian plugin.
 * Provides a settings UI under Settings > Tools > Zero-Trust Secret Guardian
 * for configuring vault provider, URL, project, and environment.
 */
class VaultSettingsConfigurable(private val project: Project) : SearchableConfigurable {

    private lateinit var vaultProviderField: JBTextField
    private lateinit var vaultUrlField: JBTextField
    private lateinit var vaultProjectField: JBTextField
    private lateinit var environmentField: JBTextField
    private var myPanel: JPanel? = null

    override fun getId(): String = "zerotrust.secret.guardian.settings"

    override fun getDisplayName(): String = "Zero-Trust Secret Guardian"

    override fun createComponent(): JComponent {
        vaultProviderField = JBTextField()
        vaultUrlField = JBTextField()
        vaultProjectField = JBTextField()
        environmentField = JBTextField()

        val panel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("Vault provider (Infisical, Doppler):"), vaultProviderField, 1, false)
            .addLabeledComponent(JBLabel("Vault URL:"), vaultUrlField, 1, false)
            .addLabeledComponent(JBLabel("Vault project:"), vaultProjectField, 1, false)
            .addLabeledComponent(JBLabel("Environment (dev, staging, prod):"), environmentField, 1, false)
            .addComponentFillVertically(JPanel(), 0)
            .panel

        myPanel = panel
        reset()
        return panel
    }

    override fun isModified(): Boolean {
        val settings = VaultSettings.getInstance(project)
        return vaultProviderField.text != settings.vaultProvider ||
                vaultUrlField.text != settings.vaultUrl ||
                vaultProjectField.text != settings.vaultProject ||
                environmentField.text != settings.environment
    }

    override fun apply() {
        val settings = VaultSettings.getInstance(project)
        settings.vaultProvider = vaultProviderField.text
        settings.vaultUrl = vaultUrlField.text
        settings.vaultProject = vaultProjectField.text
        settings.environment = environmentField.text
    }

    override fun reset() {
        val settings = VaultSettings.getInstance(project)
        vaultProviderField.text = settings.vaultProvider
        vaultUrlField.text = settings.vaultUrl
        vaultProjectField.text = settings.vaultProject
        environmentField.text = settings.environment
    }
}
