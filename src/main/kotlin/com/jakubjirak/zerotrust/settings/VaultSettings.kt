package com.jakubjirak.zerotrust.settings

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.credentialStore.generateServiceName
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project

@State(
    name = "ZeroTrustVaultSettings",
    storages = [Storage("zeroTrustVault.xml")]
)
class VaultSettings : PersistentStateComponent<VaultSettings.State> {

    private var state = State()

    class State {
        var vaultProvider: String = "Infisical"
        var vaultProject: String = ""
        var environment: String = "dev"
        var vaultUrl: String = ""
    }

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    var vaultUrl: String
        get() = state.vaultUrl
        set(value) {
            state.vaultUrl = value
        }

    var vaultProvider: String
        get() = state.vaultProvider
        set(value) {
            state.vaultProvider = value
        }

    var vaultProject: String
        get() = state.vaultProject
        set(value) {
            state.vaultProject = value
        }

    var environment: String
        get() = state.environment
        set(value) {
            state.environment = value
        }

    private fun getCredentialAttributes(): CredentialAttributes {
        return CredentialAttributes(generateServiceName("ZeroTrustSecretGuardian", "vault-token"))
    }

    fun setToken(token: String?) {
        val attributes = getCredentialAttributes()
        if (token.isNullOrEmpty()) {
            PasswordSafe.instance.set(attributes, null)
        } else {
            PasswordSafe.instance.set(attributes, Credentials("vault-token", token))
        }
    }

    fun getToken(): String? {
        val attributes = getCredentialAttributes()
        val credentials = PasswordSafe.instance.get(attributes)
        return credentials?.getPasswordAsString()
    }

    companion object {
        fun getInstance(project: Project): VaultSettings {
            return project.getService(VaultSettings::class.java)
        }
    }
}
