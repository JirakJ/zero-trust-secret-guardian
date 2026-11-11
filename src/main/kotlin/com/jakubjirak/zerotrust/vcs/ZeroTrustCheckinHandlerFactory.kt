package com.jakubjirak.zerotrust.vcs

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vcs.CheckinProjectPanel
import com.intellij.openapi.vcs.changes.CommitContext
import com.intellij.openapi.vcs.checkin.CheckinHandler
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.readText
import com.jakubjirak.zerotrust.api.VaultClientFactory
import com.jakubjirak.zerotrust.execution.SecretMasker
import com.jakubjirak.zerotrust.execution.ZeroTrustSecretCache
import com.jakubjirak.zerotrust.settings.VaultSettings
import kotlinx.coroutines.runBlocking
import java.io.IOException

class ZeroTrustCheckinHandlerFactory : CheckinHandlerFactory() {
    override fun createHandler(panel: CheckinProjectPanel, commitContext: CommitContext): CheckinHandler {
        return ZeroTrustCheckinHandler(panel.project, panel)
    }
}

/** Blocks (after confirmation) commits whose files contain a value known to be a vault secret. */
class ZeroTrustCheckinHandler(
    private val project: Project,
    private val panel: CheckinProjectPanel
) : CheckinHandler() {

    override fun beforeCheckin(): ReturnResult {
        val secrets = knownSecrets()
        if (secrets.isEmpty()) return ReturnResult.COMMIT

        val contents = panel.virtualFiles
            .filter { CommitLeakReport.isScannable(it.name, it.isDirectory) }
            .mapNotNull { file -> readOrNull(file)?.let { file.name to it } }
            .toMap()
        val report = CommitLeakReport.build(contents, secrets)
        if (report.isEmpty()) return ReturnResult.COMMIT

        val result = Messages.showYesNoDialog(
            project,
            CommitLeakReport.message(report),
            "Zero-Trust Secret Guardian",
            "Review Commit",
            "Commit Anyway (Unsafe)",
            Messages.getWarningIcon()
        )
        return if (result == Messages.YES) ReturnResult.CANCEL else ReturnResult.COMMIT
    }

    /** Secrets from the last run; if none are cached, fetch them (with a cancellable progress dialog). */
    private fun knownSecrets(): Map<String, String> {
        val cached = ZeroTrustSecretCache.getInstance(project).getSecrets()
        if (cached.isNotEmpty()) return cached
        val settings = VaultSettings.getInstance(project)
        val token = settings.getToken()
        if (token.isNullOrBlank()) return emptyMap()
        return try {
            ProgressManager.getInstance().runProcessWithProgressSynchronously<Map<String, String>, IOException>(
                {
                    runBlocking {
                        VaultClientFactory.getClient(settings.vaultProvider)
                            .getSecrets(settings.vaultProject, settings.environment, token, settings.vaultUrl)
                    }
                },
                "Checking Commit for Vault Secrets",
                true,
                project
            )
        } catch (e: IOException) {
            LOG.info("Pre-commit secret scan skipped: ${e.message}")
            emptyMap()
        } catch (e: IllegalArgumentException) {
            LOG.info("Pre-commit secret scan skipped: ${e.message}")
            emptyMap()
        }
    }

    private fun readOrNull(file: VirtualFile): String? = try {
        file.readText()
    } catch (e: IOException) {
        LOG.debug("Cannot read ${file.path}", e)
        null
    }

    private companion object {
        val LOG = Logger.getInstance(ZeroTrustCheckinHandler::class.java)
    }
}

/** Pure part of the pre-commit scan, unit-tested. */
object CommitLeakReport {
    private val BINARY_EXTENSIONS = setOf("jar", "class", "png", "jpg", "jpeg", "gif", "ico", "zip", "gz", "pdf")

    fun isScannable(fileName: String, isDirectory: Boolean): Boolean =
        !isDirectory && fileName.substringAfterLast('.', "").lowercase() !in BINARY_EXTENSIONS

    /** File name → number of distinct known secrets it contains (only files with at least one). */
    fun build(contents: Map<String, String>, secrets: Map<String, String>): Map<String, Int> =
        contents.mapValues { (_, text) -> SecretMasker.findLeaks(text, secrets).size }
            .filterValues { it > 0 }
            .toSortedMap()

    fun message(report: Map<String, Int>): String = buildString {
        appendLine("Security warning: you are about to commit values that are stored in your vault!")
        appendLine()
        report.forEach { (file, count) -> appendLine("File: $file contains $count known vault secret(s)") }
        appendLine()
        append("Please remove these secrets before committing.")
    }
}
