package com.resonix.media.feature.settings.screens.audio

import com.resonix.media.core.data.repository.PreferencesRepository
import com.resonix.media.core.model.ApplicationPreferences
import com.resonix.media.core.model.PlayerPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AudioPreferencesEqualizerTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `moving a band stores millibels and drops the preset match`() = runTest(dispatcher.scheduler) {
        val repository = FakePreferencesRepository()
        val viewModel = viewModel(repository)

        viewModel.onAction(AudioPreferencesUiEvent.SelectEqualizerPreset(EqualizerPreset.ROCK))
        advanceUntilIdle()
        assertEquals(EqualizerPreset.ROCK, EqualizerPreset.match(repository.playerPreferences.value.equalizerBands))

        viewModel.onAction(AudioPreferencesUiEvent.UpdateEqualizerBand(0, 3.5f))
        advanceUntilIdle()

        assertEquals(350, repository.playerPreferences.value.equalizerBands.first())
        assertEquals(
            EqualizerPreset.CUSTOM,
            EqualizerPreset.match(repository.playerPreferences.value.equalizerBands),
        )
    }

    @Test
    fun `band updates never change the other bands`() = runTest(dispatcher.scheduler) {
        val repository = FakePreferencesRepository()
        val viewModel = viewModel(repository)

        viewModel.onAction(AudioPreferencesUiEvent.UpdateEqualizerBand(2, -4f))
        advanceUntilIdle()

        val bands = repository.playerPreferences.value.equalizerBands
        assertEquals(listOf(0, 0, -400, 0, 0), bands)
    }

    @Test
    fun `an out of range band index is ignored`() = runTest(dispatcher.scheduler) {
        val repository = FakePreferencesRepository()
        val viewModel = viewModel(repository)

        viewModel.onAction(AudioPreferencesUiEvent.UpdateEqualizerBand(9, 5f))
        advanceUntilIdle()

        assertEquals(PlayerPreferences.DEFAULT_EQUALIZER_BANDS, repository.playerPreferences.value.equalizerBands)
    }

    @Test
    fun `the preamp is stored separately from the bands`() = runTest(dispatcher.scheduler) {
        val repository = FakePreferencesRepository()
        val viewModel = viewModel(repository)

        viewModel.onAction(AudioPreferencesUiEvent.UpdateEqualizerPreamp(-2.5f))
        advanceUntilIdle()

        assertEquals(-250, repository.playerPreferences.value.equalizerGain)
        assertEquals(PlayerPreferences.DEFAULT_EQUALIZER_BANDS, repository.playerPreferences.value.equalizerBands)
    }

    @Test
    fun `choosing a preset closes the dialog`() = runTest(dispatcher.scheduler) {
        val viewModel = viewModel(FakePreferencesRepository())

        viewModel.onAction(AudioPreferencesUiEvent.ShowDialog(AudioPreferenceDialog.EqualizerPresetDialog))
        viewModel.onAction(AudioPreferencesUiEvent.SelectEqualizerPreset(EqualizerPreset.JAZZ))
        advanceUntilIdle()

        assertEquals(null, viewModel.state.value.showDialog)
    }

    @Test
    fun `the ui always offers one control per band`() {
        val state = AudioPreferencesUiState(
            preferences = PlayerPreferences(equalizerBands = listOf(100, 200)),
        )

        assertEquals(PlayerPreferences.EQUALIZER_BAND_COUNT, state.bands.size)
        assertEquals(100, state.bands.first())
        assertEquals(0, state.bands.last())
    }

    @Test
    fun `the preamp is exposed in decibels`() {
        assertEquals(-2.5f, AudioPreferencesUiState(preferences = PlayerPreferences(equalizerGain = -250)).preamp.toDecibels(), 0.001f)
    }

    private fun viewModel(repository: PreferencesRepository) = AudioPreferencesViewModel(
        preferencesRepository = repository,
        output = AudioPreferencesViewModel.Output(navigateUp = {}),
    )
}

private class FakePreferencesRepository : PreferencesRepository {
    private val applicationPreferencesInternal = MutableStateFlow(ApplicationPreferences())
    private val playerPreferencesInternal = MutableStateFlow(PlayerPreferences())

    override val applicationPreferences: StateFlow<ApplicationPreferences> = applicationPreferencesInternal
    override val playerPreferences: StateFlow<PlayerPreferences> = playerPreferencesInternal

    override suspend fun updateApplicationPreferences(
        transform: suspend (ApplicationPreferences) -> ApplicationPreferences,
    ) {
        applicationPreferencesInternal.value = transform(applicationPreferencesInternal.value)
    }

    override suspend fun updatePlayerPreferences(transform: suspend (PlayerPreferences) -> PlayerPreferences) {
        playerPreferencesInternal.value = transform(playerPreferencesInternal.value)
    }

    override suspend fun resetPreferences() {
        applicationPreferencesInternal.value = ApplicationPreferences()
        playerPreferencesInternal.value = PlayerPreferences()
    }
}