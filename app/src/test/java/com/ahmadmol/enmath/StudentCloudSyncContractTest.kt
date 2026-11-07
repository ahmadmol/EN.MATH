package com.ahmadmol.enmath

import com.ahmadmol.enmath.core.data.*
import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

/** Contract fakes only. These tests do not connect to or validate deployed Firebase rules. */
class StudentCloudSyncContractTest {
    private class Queue : StudentSyncQueue {
        val events=mutableListOf<PendingStudentSync>()
        override fun put(event: PendingStudentSync): Boolean {
            val old=events.find { it.binding==event.binding && it.id==event.id }
            if(old!=null) return old==event
            events+=event; return true
        }
        override fun pending(binding: CloudBinding)=events.filter { it.binding==binding }
        override fun acknowledge(binding: CloudBinding,id:String)=events.removeAll { it.binding==binding && it.id==id }
    }
    private class Auth(var value: CloudIdentity?) : VerifiedCloudAuthentication { override fun currentIdentity()=value }
    private fun identity(uid:String="uid-a",project:String="enmath-test-only")=CloudIdentity(uid,project,"verified-session")
    private fun binding(identity:CloudIdentity)=CloudBinding(UUID.randomUUID().toString(),identity)
    private fun event(binding:CloudBinding)=PendingStudentSync("event-1",binding,0,
        StudentCloudDocument(1,"muadh",mapOf("language" to "ar"),emptyMap(),"{}",emptySet()))
    @Test fun productionRemainsLocalAndMissingConfigurationCannotEnqueueOrSend() {
        assertFalse(LocalOnlyCloudSync.connected); assertEquals(CloudSyncStatus.LOCAL_ONLY,LocalOnlyCloudSync.status)
        val a=identity(); val b=binding(a); var sends=0
        val sync=StudentCloudSyncCoordinator(null,Auth(a),{b.localOwnerId},Queue(),object:StudentCloudTransport {
            override fun compareAndSet(identity:CloudIdentity,event:PendingStudentSync):CloudWriteResult { sends++; return CloudWriteResult.Acknowledged }
        })
        assertEquals(CloudSyncStatus.CONFIGURATION_REQUIRED,sync.enqueue(event(b)))
        assertEquals(CloudSyncStatus.CONFIGURATION_REQUIRED,sync.flush(b)); assertEquals(0,sends)
    }
    @Test fun unrelatedProjectAndWrongUidAreRejected() {
        val a=identity(); val auth=Auth(a); val b=binding(a); val q=Queue()
        val sync=StudentCloudSyncCoordinator("enmath-test-only",auth,{b.localOwnerId},q,object:StudentCloudTransport { override fun compareAndSet(identity:CloudIdentity,event:PendingStudentSync)=CloudWriteResult.Acknowledged })
        assertEquals(CloudSyncStatus.OWNER_CHANGED,sync.enqueue(event(binding(identity(project="school-project")))))
        assertEquals(CloudSyncStatus.OWNER_CHANGED,sync.enqueue(event(binding(identity(uid="uid-b")))))
        assertTrue(q.events.isEmpty()); assertEquals(CloudSyncStatus.QUEUED,sync.enqueue(event(b)))
    }
    @Test fun offlineQueueKeepsEventAndRetriesTheSameIdempotentCommand() {
        val a=identity(); val b=binding(a); val q=Queue(); var online=false; val ids=mutableListOf<String>()
        val sync=StudentCloudSyncCoordinator(a.projectId,Auth(a),{b.localOwnerId},q,object:StudentCloudTransport {
            override fun compareAndSet(identity:CloudIdentity,event:PendingStudentSync):CloudWriteResult {
                ids+=event.id; return if(online) CloudWriteResult.Acknowledged else CloudWriteResult.NetworkFailure
            }
        })
        assertEquals(CloudSyncStatus.QUEUED,sync.enqueue(event(b))); sync.enqueue(event(b)); assertEquals(1,q.events.size)
        assertEquals(CloudSyncStatus.RETRY_REQUIRED,sync.flush(b)); assertEquals(1,q.events.size)
        online=true; assertEquals(CloudSyncStatus.SYNCED,sync.flush(b)); assertTrue(q.events.isEmpty()); assertEquals(listOf("event-1","event-1"),ids)
    }
    @Test fun conflictRetainsTheQueueInsteadOfOverwritingRemoteProgress() {
        val a=identity(); val b=binding(a); val q=Queue(); val e=event(b)
        val sync=StudentCloudSyncCoordinator(a.projectId,Auth(a),{b.localOwnerId},q,object:StudentCloudTransport {
            override fun compareAndSet(identity:CloudIdentity,event:PendingStudentSync)=CloudWriteResult.VersionConflict(e.document.copy(version=9))
        })
        sync.enqueue(e); assertEquals(CloudSyncStatus.CONFLICT,sync.flush(b)); assertEquals(listOf(e),q.events)
    }
    @Test fun logoutDuringRequestCannotAcknowledgeAnotherSession() {
        val a=identity(); val b=binding(a); val auth=Auth(a); val q=Queue()
        val sync=StudentCloudSyncCoordinator(a.projectId,auth,{b.localOwnerId},q,object:StudentCloudTransport {
            override fun compareAndSet(identity:CloudIdentity,event:PendingStudentSync):CloudWriteResult { auth.value=null; return CloudWriteResult.Acknowledged }
        })
        sync.enqueue(event(b)); assertEquals(CloudSyncStatus.OWNER_CHANGED,sync.flush(b)); assertEquals(1,q.events.size)
    }
    @Test fun switchingAccountsDoesNotFlushAnotherUidsQueue() {
        val a=identity(); val b=binding(a); val second=identity("uid-b"); val auth=Auth(a); val q=Queue(); var sends=0
        val sync=StudentCloudSyncCoordinator(a.projectId,auth,{b.localOwnerId},q,object:StudentCloudTransport {
            override fun compareAndSet(identity:CloudIdentity,event:PendingStudentSync):CloudWriteResult { sends++; assertEquals(second,identity); return CloudWriteResult.Acknowledged }
        })
        sync.enqueue(event(b)); auth.value=second; assertEquals(CloudSyncStatus.OWNER_CHANGED,sync.flush(b))
        assertEquals(CloudSyncStatus.OWNER_CHANGED,sync.flush(binding(second))); assertEquals(0,sends); assertEquals(1,q.events.size)
    }
    @Test fun immutableEventUnionDeduplicatesAndDetectsPayloadChanges() {
        val e=CloudLearningEvent("event",100,"content","sha256:one")
        assertEquals(mapOf("event" to e),StudentCloudSyncCoordinator.mergeEvents(mapOf("event" to e),mapOf("event" to e)))
        assertNull(StudentCloudSyncCoordinator.mergeEvents(mapOf("event" to e),mapOf("event" to e.copy(payloadHash="changed"))))
        assertNull(StudentCloudSyncCoordinator.mergeEvents(mapOf("wrong-id" to e),emptyMap()))
    }
    @Test fun invalidDocumentVersionsCannotBeQueued() {
        val a=identity(); val b=binding(a); val q=Queue()
        val sync=StudentCloudSyncCoordinator(a.projectId,Auth(a),{b.localOwnerId},q,object:StudentCloudTransport { override fun compareAndSet(identity:CloudIdentity,event:PendingStudentSync)=CloudWriteResult.Acknowledged })
        val e=event(b); assertEquals(CloudSyncStatus.CONFLICT,sync.enqueue(e.copy(document=e.document.copy(version=3))))
        assertTrue(q.events.isEmpty())
    }
}
