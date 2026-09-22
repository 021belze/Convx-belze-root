/**
 * Music Recognition Feature
 * 
 * This feature is based on the original MusicRecognizer project by Aleksey Saenko.
 * Original project: https://github.com/aleksey-saenko/MusicRecognizer
 * 
 * Special thanks to Aleksey Saenko for the music recognition implementation.
 */

package com.convx.music.recognition

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import com.music.shazamkit.Shazam
import com.music.shazamkit.models.RecognitionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.nio.ByteOrder

/**
 * Service for recognizing music using audio fingerprinting.
 * Records audio from the microphone, generates a Shazam-compatible fingerprint,
 * and sends it to the Shazam API for recognition.
 */
object MusicRecognitionService {
    
    // Recording parameters
    private const val RECORDING_SAMPLE_RATE = 16000
    private const val FALLBACK_SAMPLE_RATE = 44100
    private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
    private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    private const val RECORDING_DURATION_MS = 10000L
    
    private val _recognitionStatus = MutableStateFlow<RecognitionStatus>(RecognitionStatus.Ready)
    val recognitionStatus: StateFlow<RecognitionStatus> = _recognitionStatus.asStateFlow()

    private var activeJob: kotlinx.coroutines.Job? = null
    
    fun hasRecordPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context, 
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Start the music recognition process.
     * Records audio, generates fingerprint, and queries Shazam API.
     */
    @SuppressLint("MissingPermission")
    suspend fun recognize(context: Context): RecognitionStatus = withContext(Dispatchers.IO) {
        if (!hasRecordPermission(context)) {
            val error = RecognitionStatus.Error("Microphone permission not granted")
            _recognitionStatus.value = error
            return@withContext error
        }
        
        _recognitionStatus.value = RecognitionStatus.Listening
        
        try {
            // Step 1: Record audio (prefer 16kHz directly to eliminate resampling distortion)
            var actualSampleRate = RECORDING_SAMPLE_RATE
            val audioData = try {
                recordAudio(RECORDING_SAMPLE_RATE)
            } catch (e: Exception) {
                actualSampleRate = FALLBACK_SAMPLE_RATE
                recordAudio(FALLBACK_SAMPLE_RATE)
            }

            if (!isActive) return@withContext _recognitionStatus.value

            if (audioData.isEmpty()) {
                val error = RecognitionStatus.Error("No audio recorded from microphone")
                _recognitionStatus.value = error
                return@withContext error
            }
            
            _recognitionStatus.value = RecognitionStatus.Processing
            
            // Step 2: Ensure 16kHz PCM
            val pcmData = if (actualSampleRate == VibraSignature.REQUIRED_SAMPLE_RATE) {
                audioData
            } else {
                val decodedAudio = DecodedAudio(
                    data = audioData,
                    channelCount = 1,
                    sampleRate = actualSampleRate,
                    pcmEncoding = AUDIO_FORMAT
                )
                val resampled = AudioResampler.resample(
                    decodedAudio, 
                    VibraSignature.REQUIRED_SAMPLE_RATE
                ).getOrElse { error ->
                    val err = RecognitionStatus.Error("Failed to resample audio: ${error.message}")
                    _recognitionStatus.value = err
                    return@withContext err
                }
                resampled.data
            }
            
            // Verify format
            require(
                pcmData.isNotEmpty() && 
                pcmData.size % 2 == 0
            ) { "Invalid audio format for fingerprint generation" }
            
            // Step 3: Generate fingerprint using pure Kotlin signature generator
            val signature = try {
                VibraSignature.fromI16(pcmData)
            } catch (e: Exception) {
                val err = RecognitionStatus.Error("Failed to generate fingerprint: ${e.message}")
                _recognitionStatus.value = err
                return@withContext err
            }

            if (!isActive) return@withContext _recognitionStatus.value
            
            // Step 4: Send to Shazam API
            val sampleDurationMs = (pcmData.size / 2) * 1000L / VibraSignature.REQUIRED_SAMPLE_RATE
            
            val result = Shazam.recognize(signature, sampleDurationMs)
            
            result.fold(
                onSuccess = { recognitionResult ->
                    _recognitionStatus.value = RecognitionStatus.Success(recognitionResult)
                },
                onFailure = { error ->
                    val message = error.message ?: "Unknown error"
                    _recognitionStatus.value = if (message.contains("No match", ignoreCase = true)) {
                        RecognitionStatus.NoMatch("No matches found. Try again with clearer audio.")
                    } else {
                        RecognitionStatus.Error(message)
                    }
                }
            )
            
            _recognitionStatus.value
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) {
                _recognitionStatus.value = RecognitionStatus.Ready
                throw e
            }
            val err = RecognitionStatus.Error(e.message ?: "Recognition failed")
            _recognitionStatus.value = err
            err
        }
    }
    
    @SuppressLint("MissingPermission")
    private suspend fun recordAudio(sampleRate: Int): ByteArray = withContext(Dispatchers.IO) {
        val minBufferSize = AudioRecord.getMinBufferSize(
            sampleRate, 
            CHANNEL_CONFIG, 
            AUDIO_FORMAT
        )
        require(minBufferSize > 0) { "Unsupported sample rate or buffer size: $sampleRate" }
        val bufferSize = maxOf(minBufferSize * 2, 4096)
        
        val audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            CHANNEL_CONFIG,
            AUDIO_FORMAT,
            bufferSize
        )

        check(audioRecord.state == AudioRecord.STATE_INITIALIZED) {
            try { audioRecord.release() } catch (_: Exception) {}
            "AudioRecord not initialized for $sampleRate Hz"
        }
        
        val outputStream = ByteArrayOutputStream()
        val buffer = ByteArray(bufferSize)
        val startTime = System.currentTimeMillis()
        
        try {
            audioRecord.startRecording()
            
            while (System.currentTimeMillis() - startTime < RECORDING_DURATION_MS && isActive) {
                val bytesRead = audioRecord.read(buffer, 0, buffer.size)
                if (bytesRead > 0) {
                    outputStream.write(buffer, 0, bytesRead)
                }
            }
        } finally {
            try { audioRecord.stop() } catch (_: Exception) {}
            try { audioRecord.release() } catch (_: Exception) {}
        }
        
        outputStream.toByteArray()
    }
    
    fun reset() {
        activeJob?.cancel()
        activeJob = null
        _recognitionStatus.value = RecognitionStatus.Ready
    }
}
