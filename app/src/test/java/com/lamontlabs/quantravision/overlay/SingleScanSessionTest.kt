package com.lamontlabs.quantravision.overlay

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import org.junit.Assert.*
import org.junit.Test

class SingleScanSessionTest {
    @Test fun cancellationKeepsOwnershipUntilCleanupFinishes() = runBlocking {
        val entered = CompletableDeferred<Unit>()
        val cleaning = CompletableDeferred<Unit>()
        val finishCleanup = CompletableDeferred<Unit>()
        val session = SingleScanSession(this)
        var secondRuns = 0
        assertTrue(session.start {
            try { entered.complete(Unit); awaitCancellation() }
            finally { withContext(NonCancellable) { cleaning.complete(Unit); finishCleanup.await() } }
        })
        entered.await()
        session.cancel()
        cleaning.await()
        try {
            assertFalse(session.start { secondRuns++ })
            assertEquals(0, secondRuns)
        } finally { finishCleanup.complete(Unit) }
        yield()
        assertTrue(session.start { secondRuns++ })
        yield()
        assertEquals(1, secondRuns)
    }

    @Test fun closeWaitsForCanceledWorkBeforeReleasingNativeResources() = runBlocking {
        val entered = CompletableDeferred<Unit>()
        val cleaning = CompletableDeferred<Unit>()
        val finishCleanup = CompletableDeferred<Unit>()
        val session = SingleScanSession(this)
        var releases = 0
        session.start {
            try { entered.complete(Unit); awaitCancellation() }
            finally { withContext(NonCancellable) { cleaning.complete(Unit); finishCleanup.await() } }
        }
        entered.await()
        session.close { releases++ }
        cleaning.await()
        try {
            assertEquals(0, releases)
            assertFalse(session.start { error("A closed scanner must not restart") })
            session.close { releases++ }
        } finally { finishCleanup.complete(Unit) }
        yield()
        assertEquals(1, releases)
    }

    @Test fun idleCloseReleasesOnceAndBlocksFurtherScans() = runBlocking {
        val session = SingleScanSession(this)
        var releases = 0
        session.close { releases++ }
        session.close { releases++ }
        assertEquals(1, releases)
        assertFalse(session.start { error("A closed scanner must not restart") })
    }
}
