package com.ahmadmol.enmath.core.data

import java.util.UUID

/** No Firebase configuration/Auth provider/rules deployment is present in this workspace. */
enum class CloudSyncStatus { LOCAL_ONLY, QUEUED, SYNCED, RETRY_REQUIRED, CONFLICT, OWNER_CHANGED, CONFIGURATION_REQUIRED }
enum class EnergyMode { UNLIMITED }
data class StudentEnergy(val mode: EnergyMode = EnergyMode.UNLIMITED)

data class CloudIdentity(val uid: String, val projectId: String, val authenticationSession: String)
data class CloudBinding(val localOwnerId: String, val identity: CloudIdentity) {
    init { UUID.fromString(localOwnerId); require(identity.uid.isNotBlank() && identity.projectId.isNotBlank() && identity.authenticationSession.isNotBlank()) }
}
data class CloudLearningEvent(val id: String, val at: Long, val contentRef: String, val payloadHash: String)
/** Intentionally contains no Gems balance or ledger mutation. Cloud events cannot award currency. */
data class StudentCloudDocument(val version: Long, val avatarId: String?, val preferences: Map<String, String>,
    val learningEvents: Map<String, CloudLearningEvent>, val plan: String, val savedReferences: Set<String>)
data class PendingStudentSync(val id: String, val binding: CloudBinding, val expectedVersion: Long, val document: StudentCloudDocument)
sealed interface CloudWriteResult {
    data object Acknowledged : CloudWriteResult
    data object NetworkFailure : CloudWriteResult
    data class VersionConflict(val remote: StudentCloudDocument) : CloudWriteResult
}
interface VerifiedCloudAuthentication { fun currentIdentity(): CloudIdentity? }
interface StudentCloudTransport {
    /** Implement with authenticated UID paths and a transaction checking version/event receipts. */
    fun compareAndSet(identity: CloudIdentity, event: PendingStudentSync): CloudWriteResult
}
interface StudentSyncQueue {
    /** A future production implementation must durably commit before returning true. */
    fun put(event: PendingStudentSync): Boolean
    fun pending(binding: CloudBinding): List<PendingStudentSync>
    fun acknowledge(binding: CloudBinding, id: String): Boolean
}

/** Production stays local. These contracts are tested with fakes; no transport is installed. */
object LocalOnlyCloudSync {
    val status = CloudSyncStatus.LOCAL_ONLY
    val connected = false
}

/** Opt-in integration seam: no constructor is used by the application until Firebase is verified. */
class StudentCloudSyncCoordinator(private val verifiedProjectId: String?,
    private val authentication: VerifiedCloudAuthentication, private val activeLocalOwner: () -> String?, private val queue: StudentSyncQueue,
    private val transport: StudentCloudTransport) {
    private fun authorized(binding: CloudBinding) = verifiedProjectId != null &&
        binding.identity.projectId == verifiedProjectId && authentication.currentIdentity() == binding.identity && activeLocalOwner() == binding.localOwnerId
    fun enqueue(event: PendingStudentSync): CloudSyncStatus {
        if (verifiedProjectId == null) return CloudSyncStatus.CONFIGURATION_REQUIRED
        if (!authorized(event.binding)) return CloudSyncStatus.OWNER_CHANGED
        if (event.id.isBlank() || event.expectedVersion < 0 || event.document.version != event.expectedVersion + 1 ||
            (event.document.avatarId != null && event.document.avatarId !in StudentAvatars.ids) ||
            event.document.learningEvents.any { (key, value) -> key != value.id || value.at < 0 || value.payloadHash.isBlank() }) return CloudSyncStatus.CONFLICT
        return if (queue.put(event)) CloudSyncStatus.QUEUED else CloudSyncStatus.RETRY_REQUIRED
    }
    fun flush(binding: CloudBinding): CloudSyncStatus {
        if (verifiedProjectId == null) return CloudSyncStatus.CONFIGURATION_REQUIRED
        if (!authorized(binding)) return CloudSyncStatus.OWNER_CHANGED
        for (event in queue.pending(binding)) {
            if (!authorized(binding) || event.binding != binding) return CloudSyncStatus.OWNER_CHANGED
            val result = runCatching { transport.compareAndSet(binding.identity, event) }.getOrElse { CloudWriteResult.NetworkFailure }
            // Authentication may change while a request is in flight. Never acknowledge another session.
            if (!authorized(binding)) return CloudSyncStatus.OWNER_CHANGED
            when (result) {
                CloudWriteResult.NetworkFailure -> return CloudSyncStatus.RETRY_REQUIRED
                is CloudWriteResult.VersionConflict -> return CloudSyncStatus.CONFLICT
                CloudWriteResult.Acknowledged -> if (!queue.acknowledge(binding, event.id)) return CloudSyncStatus.RETRY_REQUIRED
            }
        }
        return CloudSyncStatus.SYNCED
    }
    companion object {
        /** Same immutable event ID with a changed payload is a conflict, never last-write-wins. */
        fun mergeEvents(local: Map<String, CloudLearningEvent>, remote: Map<String, CloudLearningEvent>): Map<String, CloudLearningEvent>? {
            if (local.any { (id, event) -> id != event.id || (remote[id] != null && remote[id] != event) } || remote.any { it.key != it.value.id }) return null
            return remote + local
        }
    }
}
