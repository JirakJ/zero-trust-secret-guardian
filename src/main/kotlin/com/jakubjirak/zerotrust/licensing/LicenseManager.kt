package com.jakubjirak.zerotrust.licensing

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
class LicenseManager {

    // Simple mock for licensing phase. In reality, we'd use JetBrains Licensing API.
    private var isLicensed = true

    fun isEnterprise(): Boolean {
        return isLicensed
    }

    fun setLicensed(licensed: Boolean) {
        this.isLicensed = licensed
    }

    companion object {
        fun getInstance(project: Project): LicenseManager {
            return project.getService(LicenseManager::class.java)
        }
    }
}
