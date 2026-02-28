package com.jakubjirak.zerotrust.execution

import com.intellij.execution.ExecutionException
import com.intellij.execution.RunConfigurationExtension
import com.intellij.execution.configurations.JavaParameters
import com.intellij.execution.configurations.RunConfigurationBase
import com.intellij.execution.configurations.RunnerSettings
import com.jakubjirak.zerotrust.util.SecretFetcher

class ZeroTrustRunConfigurationExtension : RunConfigurationExtension() {

    override fun isApplicableFor(configuration: RunConfigurationBase<*>): Boolean {
        return true
    }

    override fun <T : RunConfigurationBase<*>> updateJavaParameters(
        configuration: T,
        params: JavaParameters,
        runnerSettings: RunnerSettings?
    ) {
        val project = configuration.project
        
        val secretsFetched = SecretFetcher.fetchSecretsSynchronously(project)

        if (secretsFetched.isNotEmpty()) {
            secretsFetched.forEach { (key, value) ->
                params.env[key] = value
            }
            ZeroTrustSecretCache.getInstance(project).setSecrets(secretsFetched)
        }
    }
}
