package com.mohammed.notes.feature.privacy.presentation

import kotlinx.serialization.Serializable

/**
 * Route contracts for the privacy area. The screens themselves live behind one
 * `HiddenNotesRoute` / `ChangePinRoute` entry each (setup → verify → list are gate
 * states, not destinations), so only these two objects cross the nav boundary.
 */
@Serializable
data object HiddenNotes

@Serializable
data object ChangePin
