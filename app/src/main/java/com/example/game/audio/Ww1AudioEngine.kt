package com.example.game.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.tanh
import kotlin.random.Random

enum class MusicMode {
    MENU,
    COMBAT,
    VICTORY,
    SILENT
}

enum class SfxType {
    RIFLE_SHOT,
    SMG_SHOT,
    LMG_SHOT,
    SNIPER_SHOT,
    REVOLVER_SHOT,
    BOLT_CYCLE,
    RELOAD,
    ARTILLERY_BOOM,
    GRENADE_BLAST,
    WHISTLE,
    HIT_MARKER,
    HURT,
    UI_CLICK
}

private class ActiveSfx(
    val type: SfxType,
    val totalSamples: Int,
    var pos: Int = 0
)

class Ww1AudioEngine {
    companion object {
        private const val SAMPLE_RATE = 22050
        private const val BUFFER_FRAMES = 1024
    }

    @Volatile
    var musicMode: MusicMode = MusicMode.MENU

    @Volatile
    var musicVolume: Float = 0.72f

    @Volatile
    var sfxVolume: Float = 0.85f

    private val running = AtomicBoolean(false)
    private val paused = AtomicBoolean(false)
    private val activeSfx = ConcurrentLinkedQueue<ActiveSfx>()
    private var audioThread: Thread? = null

    fun start() {
        if (running.getAndSet(true)) return
        audioThread = Thread({
            runAudioLoop()
        }, "Ww1AudioSynth").apply {
            priority = Thread.MAX_PRIORITY
            start()
        }
    }

    fun pause() {
        paused.set(true)
    }

    fun resume() {
        paused.set(false)
    }

    fun stop() {
        running.set(false)
        audioThread?.join(400)
        audioThread = null
    }

    fun playSfx(type: SfxType) {
        if (sfxVolume <= 0.01f) return
        val durationSec = when (type) {
            SfxType.RIFLE_SHOT -> 0.42f
            SfxType.SNIPER_SHOT -> 0.55f
            SfxType.SMG_SHOT -> 0.16f
            SfxType.LMG_SHOT -> 0.22f
            SfxType.REVOLVER_SHOT -> 0.28f
            SfxType.BOLT_CYCLE -> 0.22f
            SfxType.RELOAD -> 0.48f
            SfxType.ARTILLERY_BOOM -> 1.8f
            SfxType.GRENADE_BLAST -> 1.15f
            SfxType.WHISTLE -> 0.95f
            SfxType.HIT_MARKER -> 0.09f
            SfxType.HURT -> 0.28f
            SfxType.UI_CLICK -> 0.06f
        }
        if (activeSfx.size > 16) {
            activeSfx.poll()
        }
        activeSfx.add(ActiveSfx(type, (durationSec * SAMPLE_RATE).toInt()))
    }

    private fun runAudioLoop() {
        val minBuf = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(BUFFER_FRAMES * 2)

        val track = try {
            AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setTransferMode(AudioTrack.MODE_STREAM)
                .setBufferSizeInBytes(minBuf)
                .build()
        } catch (_: Throwable) {
            running.set(false)
            return
        }

        val pcmBuffer = ShortArray(BUFFER_FRAMES)
        var sampleIndex = 0L
        var lpFilterState = 0f
        var sfxLpState = 0f
        val rng = Random(1916)

        try {
            track.play()
            while (running.get()) {
                if (paused.get()) {
                    Thread.sleep(40)
                    continue
                }

                val mVol = musicVolume
                val sVol = sfxVolume
                val mode = musicMode

                for (i in 0 until BUFFER_FRAMES) {
                    val t = sampleIndex.toDouble() / SAMPLE_RATE.toDouble()
                    var musicSample = if (mVol > 0.01f && mode != MusicMode.SILENT) {
                        synthesizeOrchestralScore(t, mode)
                    } else {
                        0f
                    }
                    // Warm orchestral low-pass smoothing
                    lpFilterState += 0.28f * (musicSample - lpFilterState)
                    musicSample = lpFilterState * mVol

                    var sfxSample = 0f
                    if (sVol > 0.01f && activeSfx.isNotEmpty()) {
                        val iter = activeSfx.iterator()
                        while (iter.hasNext()) {
                            val sfx = iter.next()
                            if (sfx.pos >= sfx.totalSamples) {
                                iter.remove()
                                continue
                            }
                            sfxSample += synthesizeSfxSample(sfx, rng)
                            sfx.pos++
                        }
                        sfxLpState += 0.55f * (sfxSample - sfxLpState)
                        sfxSample = sfxLpState * sVol
                    }

                    val mixed = tanh((musicSample + sfxSample).toDouble()).toFloat()
                    pcmBuffer[i] = (mixed * 29000f).toInt().coerceIn(-32767, 32767).toShort()
                    sampleIndex++
                }

                track.write(pcmBuffer, 0, pcmBuffer.size)
            }
        } catch (_: Throwable) {
            // Audio device released cleanly
        } finally {
            try {
                track.stop()
                track.release()
            } catch (_: Throwable) {
            }
        }
    }

    /**
     * Synthesizes a multi-voice orchestral WW1 score in D minor:
     * - String/cello warm harmonic pad
     * - Solo horn/violin cantabile melody with vibrato
     * - Menu: gentle harp/piano arpeggio
     * - Combat: driving timpani & military snare cadence
     */
    private fun synthesizeOrchestralScore(t: Double, mode: MusicMode): Float {
        // 8-bar progression in D minor (each chord = 3.0s in menu, 2.0s in combat)
        val chordDur = when (mode) {
            MusicMode.MENU -> 3.0
            MusicMode.COMBAT -> 2.0
            MusicMode.VICTORY -> 2.2
            MusicMode.SILENT -> 3.0
        }
        val totalCycle = chordDur * 8.0
        val cycleT = t % totalCycle
        val barIdx = (cycleT / chordDur).toInt().coerceIn(0, 7)
        val barLocalT = cycleT - barIdx * chordDur
        val barProgress = barLocalT / chordDur

        // Chord frequencies (Root, Third, Fifth)
        val chordsMenu = arrayOf(
            doubleArrayOf(146.83, 174.61, 220.00), // Dm
            doubleArrayOf(116.54, 146.83, 174.61), // Bb
            doubleArrayOf(196.00, 233.08, 293.66), // Gm
            doubleArrayOf(110.00, 138.59, 164.81), // A
            doubleArrayOf(146.83, 174.61, 220.00), // Dm
            doubleArrayOf(174.61, 220.00, 261.63), // F
            doubleArrayOf(130.81, 164.81, 196.00), // C
            doubleArrayOf(110.00, 164.81, 220.00)  // A sus/5
        )
        val chordsVictory = arrayOf(
            doubleArrayOf(146.83, 185.00, 220.00), // D major
            doubleArrayOf(196.00, 246.94, 293.66), // G major
            doubleArrayOf(110.00, 138.59, 164.81), // A major
            doubleArrayOf(146.83, 185.00, 220.00), // D major
            doubleArrayOf(123.47, 146.83, 185.00), // Bm
            doubleArrayOf(196.00, 246.94, 293.66), // G major
            doubleArrayOf(110.00, 138.59, 164.81), // A major
            doubleArrayOf(146.83, 185.00, 220.00)  // D major
        )
        val chord = if (mode == MusicMode.VICTORY) chordsVictory[barIdx] else chordsMenu[barIdx]

        // 1. Warm Cello & Contrabass String Pad (gentle swell envelope per bar)
        val padEnv = (sin(barProgress * PI).coerceAtLeast(0.15)).toFloat()
        val bassFreq = chord[0] * 0.5
        val bass = (sin(2.0 * PI * bassFreq * t) * 0.55 +
            sin(2.0 * PI * bassFreq * 2.0 * t) * 0.25 +
            sin(2.0 * PI * bassFreq * 3.0 * t) * 0.08).toFloat()

        var pad = 0f
        for (f in chord) {
            // Subtle chorus detune for orchestral string section richness
            val s1 = sin(2.0 * PI * f * 0.997 * t)
            val s2 = sin(2.0 * PI * f * 1.003 * t)
            val s3 = sin(2.0 * PI * f * 2.0 * t) * 0.22
            pad += ((s1 + s2) * 0.5 + s3).toFloat()
        }
        pad = (pad / 3.0f) * (0.45f + 0.35f * padEnv)

        // 2. Expressive Solo Horn / Violin Melody (2 notes per bar)
        val melodyDm = doubleArrayOf(
            293.66, 349.23, // D4 -> F4
            329.63, 293.66, // E4 -> D4
            233.08, 261.63, // Bb3 -> C4
            220.00, 261.63, // A3 -> C4
            293.66, 440.00, // D4 -> A4
            392.00, 349.23, // G4 -> F4
            329.63, 293.66, // E4 -> D4
            261.63, 220.00  // C4 -> A3
        )
        val melodyVictory = doubleArrayOf(
            293.66, 369.99,
            440.00, 392.00,
            440.00, 493.88,
            587.33, 440.00,
            493.88, 440.00,
            392.00, 369.99,
            329.63, 440.00,
            587.33, 587.33
        )
        val melodyArr = if (mode == MusicMode.VICTORY) melodyVictory else melodyDm
        val halfDur = chordDur * 0.5
        val noteIdx = (barIdx * 2 + if (barLocalT >= halfDur) 1 else 0).coerceIn(0, 15)
        val noteLocalT = if (barLocalT >= halfDur) barLocalT - halfDur else barLocalT
        val noteNorm = (noteLocalT / halfDur).coerceIn(0.0, 1.0)
        val noteEnv = ((1.0 - exp(-noteLocalT * 10.0)) * exp(-noteNorm * 1.15)).toFloat()
        val vibrato = 1.0 + 0.0038 * sin(2.0 * PI * 5.2 * t)
        val mf = melodyArr[noteIdx] * vibrato
        // Horn/violin timbre: fundamental + 2nd + 3rd harmonic
        val melody = ((sin(2.0 * PI * mf * t) * 0.62 +
            sin(2.0 * PI * mf * 2.0 * t) * 0.28 +
            sin(2.0 * PI * mf * 3.0 * t) * 0.12) * noteEnv).toFloat()

        // 3. Mode-specific rhythmic/harmonic layer
        val layer = when (mode) {
            MusicMode.MENU -> {
                // Soft piano/harp arpeggio (4 notes per bar)
                val stepDur = chordDur / 4.0
                val stepIdx = (barLocalT / stepDur).toInt().coerceIn(0, 3)
                val stepT = barLocalT - stepIdx * stepDur
                val arpFreq = when (stepIdx) {
                    0 -> chord[0] * 2.0
                    1 -> chord[1] * 2.0
                    2 -> chord[2] * 2.0
                    else -> chord[1] * 2.0
                }
                val arpEnv = ((1.0 - exp(-stepT * 45.0)) * exp(-stepT * 3.6)).toFloat()
                (sin(2.0 * PI * arpFreq * t) * 0.65 + sin(2.0 * PI * arpFreq * 2.0 * t) * 0.18).toFloat() * arpEnv * 0.25f
            }
            MusicMode.COMBAT -> {
                // Driving military timpani & marching pulse (4 beats per bar)
                val beatDur = chordDur / 4.0
                val beatIdx = (barLocalT / beatDur).toInt().coerceIn(0, 3)
                val beatT = barLocalT - beatIdx * beatDur
                val timpaniEnv = exp(-beatT * 9.5).toFloat()
                val timpaniFreq = bassFreq * (1.0 + 0.45 * exp(-beatT * 28.0))
                val timpani = sin(2.0 * PI * timpaniFreq * beatT).toFloat() * timpaniEnv *
                    (if (beatIdx == 0 || beatIdx == 2) 0.48f else 0.28f)

                // Sub-beat brass staccato ostinato
                val eighthDur = beatDur * 0.5
                val eighthT = barLocalT % eighthDur
                val ostEnv = exp(-eighthT * 14.0).toFloat()
                val ost = sin(2.0 * PI * chord[0] * t).toFloat() * ostEnv * 0.18f
                timpani + ost
            }
            MusicMode.VICTORY -> {
                val beatDur = chordDur / 2.0
                val beatT = barLocalT % beatDur
                val brassEnv = exp(-beatT * 4.5).toFloat()
                (sin(2.0 * PI * chord[2] * 2.0 * t) * brassEnv * 0.22).toFloat()
            }
            MusicMode.SILENT -> 0f
        }

        return (bass * 0.28f + pad * 0.26f + melody * 0.24f + layer) * 0.9f
    }

    private fun synthesizeSfxSample(sfx: ActiveSfx, rng: Random): Float {
        val t = sfx.pos.toFloat() / SAMPLE_RATE.toFloat()
        val norm = sfx.pos.toFloat() / sfx.totalSamples.toFloat()
        val noise = rng.nextFloat() * 2f - 1f

        return when (sfx.type) {
            SfxType.RIFLE_SHOT -> {
                val crack = noise * exp(-t * 28f) * 0.85f
                val thump = sin(2.0 * PI * (150.0 * exp(-t * 18.0) + 48.0) * t).toFloat() * exp(-t * 14f) * 0.65f
                val tail = noise * exp(-t * 7f) * 0.22f
                crack + thump + tail
            }
            SfxType.SNIPER_SHOT -> {
                val crack = noise * exp(-t * 22f) * 0.95f
                val boom = sin(2.0 * PI * (180.0 * exp(-t * 14.0) + 42.0) * t).toFloat() * exp(-t * 9f) * 0.75f
                val echo = noise * exp(-t * 4.5f) * 0.25f
                crack + boom + echo
            }
            SfxType.SMG_SHOT -> {
                val snap = noise * exp(-t * 42f) * 0.65f
                val punch = sin(2.0 * PI * (190.0 * exp(-t * 26.0) + 75.0) * t).toFloat() * exp(-t * 26f) * 0.5f
                snap + punch
            }
            SfxType.LMG_SHOT -> {
                val bark = noise * exp(-t * 32f) * 0.72f
                val body = sin(2.0 * PI * (145.0 * exp(-t * 22.0) + 60.0) * t).toFloat() * exp(-t * 18f) * 0.58f
                bark + body
            }
            SfxType.REVOLVER_SHOT -> {
                val pop = noise * exp(-t * 34f) * 0.78f
                val ring = sin(2.0 * PI * (240.0 * exp(-t * 20.0) + 68.0) * t).toFloat() * exp(-t * 16f) * 0.52f
                pop + ring
            }
            SfxType.BOLT_CYCLE -> {
                // Two crisp metallic clicks at t=0.01 and t=0.11
                val c1 = if (t < 0.08f) sin(2.0 * PI * 1950.0 * t).toFloat() * exp(-t * 95f) * 0.45f else 0f
                val t2 = t - 0.10f
                val c2 = if (t2 > 0f) sin(2.0 * PI * 2400.0 * t2).toFloat() * exp(-t2 * 95f) * 0.48f else 0f
                c1 + c2
            }
            SfxType.RELOAD -> {
                val c1 = if (t < 0.18f) (noise * 0.25f + sin(2.0 * PI * 1200.0 * t).toFloat() * 0.3f) * exp(-t * 35f) else 0f
                val t2 = t - 0.24f
                val c2 = if (t2 > 0f) (noise * 0.2f + sin(2.0 * PI * 1650.0 * t2).toFloat() * 0.4f) * exp(-t2 * 40f) else 0f
                c1 + c2
            }
            SfxType.ARTILLERY_BOOM -> {
                if (t < 0.35f) {
                    // Incoming shell whistle descending
                    val whistleFreq = 1400.0 - t * 2100.0
                    sin(2.0 * PI * whistleFreq * t).toFloat() * (t / 0.35f) * 0.28f
                } else {
                    val bt = t - 0.35f
                    val sub = sin(2.0 * PI * (68.0 * exp(-bt * 4.0) + 28.0) * bt).toFloat() * exp(-bt * 2.2f) * 0.95f
                    val rumble = noise * exp(-bt * 2.8f) * 0.72f
                    sub + rumble
                }
            }
            SfxType.GRENADE_BLAST -> {
                val blast = sin(2.0 * PI * (85.0 * exp(-t * 6.0) + 34.0) * t).toFloat() * exp(-t * 3.8f) * 0.85f
                val dirt = noise * exp(-t * 4.2f) * 0.68f
                blast + dirt
            }
            SfxType.WHISTLE -> {
                // Authentic brass trench pea-whistle with trill modulation
                val trill = 1.0 + 0.06 * sin(2.0 * PI * 36.0 * t)
                val env = (1f - abs(norm * 2f - 1f)).coerceIn(0f, 1f)
                (sin(2.0 * PI * 2780.0 * trill * t) * 0.45 +
                    sin(2.0 * PI * 2960.0 * trill * t) * 0.35).toFloat() * env * 0.5f
            }
            SfxType.HIT_MARKER -> {
                sin(2.0 * PI * 1850.0 * t).toFloat() * exp(-t * 65f) * 0.45f
            }
            SfxType.HURT -> {
                sin(2.0 * PI * 75.0 * t).toFloat() * exp(-t * 14f) * 0.65f +
                    noise * exp(-t * 24f) * 0.25f
            }
            SfxType.UI_CLICK -> {
                sin(2.0 * PI * 980.0 * t).toFloat() * exp(-t * 85f) * 0.3f
            }
        }
    }
}
