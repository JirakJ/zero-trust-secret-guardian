/*
 * Copyright (c) 2026 Jakub Jirák. All rights reserved.
 * Licensed under the terms of the JetBrains Marketplace EULA.
 */
package com.jakubjirak.zerotrust.licensing

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.ui.LicensingFacade

@Service(Service.Level.APP)
class FreemiumGate {

    enum class LicenseStatus { FREE, TRIAL, PAID }

    companion object {
        const val PRODUCT_CODE = "PZEROTRUST"

        const val FEATURE_ADVANCED_SCANNING = "advanced_scanning"
        const val FEATURE_MULTI_VAULT = "multi_vault"
        const val FEATURE_AUDIT_LOGGING = "audit_logging"
        const val FEATURE_SECRET_ROTATION = "secret_rotation"
        const val FEATURE_TEAM_COLLABORATION = "team_collaboration"
        const val FEATURE_ADVANCED_MASKING = "advanced_masking"
        const val FEATURE_CICD_INTEGRATION = "cicd_integration"
        const val FEATURE_COMPLIANCE_REPORTING = "compliance_reporting"
        const val FEATURE_APPROVAL_WORKFLOWS = "approval_workflows"

        private val PREMIUM_FEATURES = setOf(
            FEATURE_ADVANCED_SCANNING, FEATURE_MULTI_VAULT, FEATURE_AUDIT_LOGGING,
            FEATURE_SECRET_ROTATION, FEATURE_TEAM_COLLABORATION, FEATURE_ADVANCED_MASKING,
            FEATURE_CICD_INTEGRATION, FEATURE_COMPLIANCE_REPORTING, FEATURE_APPROVAL_WORKFLOWS
        )

        fun getInstance(): FreemiumGate =
            ApplicationManager.getApplication().getService(FreemiumGate::class.java)
    }

    fun getLicenseStatus(): LicenseStatus {
        val facade = LicensingFacade.getInstance() ?: return LicenseStatus.FREE
        val stamp = facade.getConfirmationStamp(PRODUCT_CODE) ?: return LicenseStatus.FREE
        return if (stamp.startsWith("eval:")) LicenseStatus.TRIAL else LicenseStatus.PAID
    }

    fun isPaidFeatureAvailable(featureId: String): Boolean {
        if (featureId !in PREMIUM_FEATURES) return true
        return getLicenseStatus() != LicenseStatus.FREE
    }

    fun isPremiumUser(): Boolean = getLicenseStatus() != LicenseStatus.FREE
}
