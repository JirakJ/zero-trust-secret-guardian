package com.jakubjirak.zerotrust.execution

import com.intellij.execution.ExecutionListener
import com.intellij.execution.ExecutionManager
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.project.Project

class ZeroTrustExecutionListener(private val project: Project) : ExecutionListener {
    override fun processTerminated(executorId: String, env: ExecutionEnvironment, handler: ProcessHandler, exitCode: Int) {
        val hasRunningProcesses = ExecutionManager.getInstance(project).getRunningProcesses().isNotEmpty()
        if (!hasRunningProcesses) {
            ZeroTrustSecretCache.getInstance(project).clear()
        }
    }
}
