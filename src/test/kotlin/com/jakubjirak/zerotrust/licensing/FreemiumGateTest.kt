/*
 * Copyright (c) 2026 Jakub Jirák. All rights reserved.
 * Licensed under the terms of the JetBrains Marketplace EULA.
 */
package com.jakubjirak.zerotrust.licensing

import com.intellij.openapi.application.ApplicationManager
import com.intellij.ui.LicensingFacade
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.MockedStatic
import org.mockito.Mockito

class FreemiumGateTest {

    private lateinit var gate: FreemiumGate
    private lateinit var facadeMock: MockedStatic<LicensingFacade>
    private lateinit var appMock: MockedStatic<ApplicationManager>
    private lateinit var licensingFacadeInstance: LicensingFacade

    @BeforeEach
    fun setUp() {
        gate = FreemiumGate()
        licensingFacadeInstance = Mockito.mock(LicensingFacade::class.java)
        facadeMock = Mockito.mockStatic(LicensingFacade::class.java)
    }

    @AfterEach
    fun tearDown() {
        facadeMock.close()
        if (::appMock.isInitialized) {
            appMock.close()
        }
    }

    // ---- getLicenseStatus() ----

    @Test
    fun `getLicenseStatus returns FREE when facade is null`() {
        facadeMock.`when`<LicensingFacade?> { LicensingFacade.getInstance() }.thenReturn(null)
        assertEquals(FreemiumGate.LicenseStatus.FREE, gate.getLicenseStatus())
    }

    @Test
    fun `getLicenseStatus returns FREE when confirmation stamp is null`() {
        facadeMock.`when`<LicensingFacade?> { LicensingFacade.getInstance() }.thenReturn(licensingFacadeInstance)
        Mockito.`when`(licensingFacadeInstance.getConfirmationStamp(FreemiumGate.PRODUCT_CODE)).thenReturn(null)
        assertEquals(FreemiumGate.LicenseStatus.FREE, gate.getLicenseStatus())
    }

    @Test
    fun `getLicenseStatus returns TRIAL when stamp starts with eval`() {
        facadeMock.`when`<LicensingFacade?> { LicensingFacade.getInstance() }.thenReturn(licensingFacadeInstance)
        Mockito.`when`(licensingFacadeInstance.getConfirmationStamp(FreemiumGate.PRODUCT_CODE)).thenReturn("eval:12345")
        assertEquals(FreemiumGate.LicenseStatus.TRIAL, gate.getLicenseStatus())
    }

    @Test
    fun `getLicenseStatus returns PAID when stamp does not start with eval`() {
        facadeMock.`when`<LicensingFacade?> { LicensingFacade.getInstance() }.thenReturn(licensingFacadeInstance)
        Mockito.`when`(licensingFacadeInstance.getConfirmationStamp(FreemiumGate.PRODUCT_CODE)).thenReturn("key:ABCDEF-12345")
        assertEquals(FreemiumGate.LicenseStatus.PAID, gate.getLicenseStatus())
    }

    // ---- isPaidFeatureAvailable() ----

    @Test
    fun `isPaidFeatureAvailable returns true for non-premium feature regardless of license`() {
        facadeMock.`when`<LicensingFacade?> { LicensingFacade.getInstance() }.thenReturn(null)
        assertTrue(gate.isPaidFeatureAvailable("basic_scanning"))
    }

    @Test
    fun `isPaidFeatureAvailable returns false for premium feature when FREE`() {
        facadeMock.`when`<LicensingFacade?> { LicensingFacade.getInstance() }.thenReturn(null)
        assertFalse(gate.isPaidFeatureAvailable(FreemiumGate.FEATURE_ADVANCED_SCANNING))
    }

    @Test
    fun `isPaidFeatureAvailable returns true for premium feature when TRIAL`() {
        facadeMock.`when`<LicensingFacade?> { LicensingFacade.getInstance() }.thenReturn(licensingFacadeInstance)
        Mockito.`when`(licensingFacadeInstance.getConfirmationStamp(FreemiumGate.PRODUCT_CODE)).thenReturn("eval:12345")
        assertTrue(gate.isPaidFeatureAvailable(FreemiumGate.FEATURE_ADVANCED_SCANNING))
    }

    @Test
    fun `isPaidFeatureAvailable returns true for premium feature when PAID`() {
        facadeMock.`when`<LicensingFacade?> { LicensingFacade.getInstance() }.thenReturn(licensingFacadeInstance)
        Mockito.`when`(licensingFacadeInstance.getConfirmationStamp(FreemiumGate.PRODUCT_CODE)).thenReturn("key:ABCDEF-12345")
        assertTrue(gate.isPaidFeatureAvailable(FreemiumGate.FEATURE_ADVANCED_SCANNING))
    }

    @Test
    fun `isPaidFeatureAvailable checks all premium features when FREE`() {
        facadeMock.`when`<LicensingFacade?> { LicensingFacade.getInstance() }.thenReturn(null)
        val premiumFeatures = listOf(
            FreemiumGate.FEATURE_ADVANCED_SCANNING,
            FreemiumGate.FEATURE_MULTI_VAULT,
            FreemiumGate.FEATURE_AUDIT_LOGGING,
            FreemiumGate.FEATURE_SECRET_ROTATION,
            FreemiumGate.FEATURE_TEAM_COLLABORATION,
            FreemiumGate.FEATURE_ADVANCED_MASKING,
            FreemiumGate.FEATURE_CICD_INTEGRATION,
            FreemiumGate.FEATURE_COMPLIANCE_REPORTING,
            FreemiumGate.FEATURE_APPROVAL_WORKFLOWS
        )
        for (feature in premiumFeatures) {
            assertFalse(gate.isPaidFeatureAvailable(feature), "Expected $feature to be unavailable for FREE users")
        }
    }

    @Test
    fun `isPaidFeatureAvailable checks all premium features when PAID`() {
        facadeMock.`when`<LicensingFacade?> { LicensingFacade.getInstance() }.thenReturn(licensingFacadeInstance)
        Mockito.`when`(licensingFacadeInstance.getConfirmationStamp(FreemiumGate.PRODUCT_CODE)).thenReturn("key:ABCDEF-12345")
        val premiumFeatures = listOf(
            FreemiumGate.FEATURE_ADVANCED_SCANNING,
            FreemiumGate.FEATURE_MULTI_VAULT,
            FreemiumGate.FEATURE_AUDIT_LOGGING,
            FreemiumGate.FEATURE_SECRET_ROTATION,
            FreemiumGate.FEATURE_TEAM_COLLABORATION,
            FreemiumGate.FEATURE_ADVANCED_MASKING,
            FreemiumGate.FEATURE_CICD_INTEGRATION,
            FreemiumGate.FEATURE_COMPLIANCE_REPORTING,
            FreemiumGate.FEATURE_APPROVAL_WORKFLOWS
        )
        for (feature in premiumFeatures) {
            assertTrue(gate.isPaidFeatureAvailable(feature), "Expected $feature to be available for PAID users")
        }
    }

    // ---- isPremiumUser() ----

    @Test
    fun `isPremiumUser returns false when FREE`() {
        facadeMock.`when`<LicensingFacade?> { LicensingFacade.getInstance() }.thenReturn(null)
        assertFalse(gate.isPremiumUser())
    }

    @Test
    fun `isPremiumUser returns true when TRIAL`() {
        facadeMock.`when`<LicensingFacade?> { LicensingFacade.getInstance() }.thenReturn(licensingFacadeInstance)
        Mockito.`when`(licensingFacadeInstance.getConfirmationStamp(FreemiumGate.PRODUCT_CODE)).thenReturn("eval:12345")
        assertTrue(gate.isPremiumUser())
    }

    @Test
    fun `isPremiumUser returns true when PAID`() {
        facadeMock.`when`<LicensingFacade?> { LicensingFacade.getInstance() }.thenReturn(licensingFacadeInstance)
        Mockito.`when`(licensingFacadeInstance.getConfirmationStamp(FreemiumGate.PRODUCT_CODE)).thenReturn("key:ABCDEF-12345")
        assertTrue(gate.isPremiumUser())
    }

    // ---- Constants ----

    @Test
    fun `PRODUCT_CODE is correct`() {
        assertEquals("PZEROTRUST", FreemiumGate.PRODUCT_CODE)
    }

    @Test
    fun `LicenseStatus enum has correct values`() {
        val values = FreemiumGate.LicenseStatus.values()
        assertEquals(3, values.size)
        assertTrue(values.contains(FreemiumGate.LicenseStatus.FREE))
        assertTrue(values.contains(FreemiumGate.LicenseStatus.TRIAL))
        assertTrue(values.contains(FreemiumGate.LicenseStatus.PAID))
    }
}
