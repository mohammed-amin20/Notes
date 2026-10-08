package com.mohammed.notes.feature.core.security

/**
 * The escalating cooldown schedule for wrong PIN entries. Pure so every branch is
 * unit-testable on the host; [PrivacyStore] persists the counters beside the verifier.
 *
 * Nothing auto-deletes or self-destructs: a locked account stays locked (and only a
 * successful entry — or an explicit, data-losing reset — clears the throttle).
 */
object PinThrottle {

    /** Failures below this are free-form; the fifth wrong PIN arms the first block. */
    const val ARM_AT_FAILURES = 5

    fun cooldownFor(failCount: Int): Long = when {
        failCount < ARM_AT_FAILURES -> 0L
        failCount == 5 -> 30_000L
        failCount == 6 -> 60_000L
        failCount == 7 -> 5 * 60_000L
        failCount == 8 -> 15 * 60_000L
        failCount == 9 -> 30 * 60_000L
        else -> 60 * 60_000L
    }
}