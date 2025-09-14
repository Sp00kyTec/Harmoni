// app/src/main/java/com/harmoni/fragment/EqualizerFragment.kt
package com.harmoni.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.harmoni.AudioService
import com.harmoni.MainActivity
import com.harmoni.R
import java.util.*

class EqualizerFragment : Fragment() {

    private lateinit var containerBands: LinearLayout
    private lateinit var spinnerPresets: Spinner
    private var service: AudioService? = null
    private val filters = mutableListOf<BandFilter>()

    private val presetNames = listOf("Flat", "Classical", "Pop", "Rock", "Jazz", "Bass Booster")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_equalizer, container, false)
        containerBands = view.findViewById(R.id.container_bands)
        spinnerPresets = view.findViewById(R.id.spinner_presets)
        return view
    }

    override fun onResume() {
        super.onResume()
        bindToService()
    }

    private fun bindToService() {
        service = (activity as? MainActivity)?.audioService
        val eq = service?.equalizer ?: run {
            Toast.makeText(context, "Equalizer not supported on this device", Toast.LENGTH_SHORT).show()
            return
        }

        setupPresets(eq)
        setupBands(eq)
    }

    private fun setupPresets(equalizer: android.media.audiofx.Equalizer) {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, presetNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPresets.adapter = adapter

        spinnerPresets.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                applyPreset(equalizer, position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun applyPreset(equalizer: android.media.audiofx.Equalizer, index: Int) {
        val numBands = equalizer.numberOfBands
        val levelMax = equalizer.bandLevelRange[1]
        val levelMin = equalizer.bandLevelRange[0]

        when (index) {
            0 -> { /* Flat - do nothing */ }
            1 -> { // Classical
                for (i in 0 until numBands) {
                    val level = when (i) {
                        in 0..1 -> levelMin + (levelMax - levelMin) / 4
                        in 2..3 -> levelMin
                        else -> levelMin + (levelMax - levelMin) / 3
                    }
                    equalizer.setBandLevel(i, level.toShort())
                }
            }
            2 -> { // Pop
                for (i in 0 until numBands) {
                    val level = if (i <= 1 || i >= numBands - 2) {
                        levelMin + (levelMax - levelMin) * 3 / 5
                    } else {
                        levelMin + (levelMax - levelMin) / 4
                    }
                    equalizer.setBandLevel(i, level.toShort())
                }
            }
            3 -> { // Rock
                for (i in 0 until numBands) {
                    val level = if (i <= 2) {
                        levelMin + (levelMax - levelMin) * 2 / 3
                    } else {
                        levelMin + (levelMax - levelMin) / 4
                    }
                    equalizer.setBandLevel(i, level.toShort())
                }
            }
            4 -> { // Jazz
                for (i in 0 until numBands) {
                    val level = if (i == 0 || i >= numBands - 2) {
                        levelMin + (levelMax - levelMin) / 3
                    } else {
                        levelMin + (levelMax - levelMin) / 2
                    }
                    equalizer.setBandLevel(i, level.toShort())
                }
            }
            5 -> { // Bass Booster
                for (i in 0 until numBands) {
                    val level = if (i <= 2) {
                        levelMin + (levelMax - levelMin) * 4 / 5
                    } else {
                        levelMin + (levelMax - levelMin) / 4
                    }
                    equalizer.setBandLevel(i, level.toShort())
                }
            }
        }

        updateAllSeekBars(equalizer)
    }

    private fun setupBands(equalizer: android.media.audiofx.Equalizer) {
        containerBands.removeAllViews()
        filters.clear()

        val numBands = equalizer.numberOfBands
        val levelMax = equalizer.bandLevelRange[1]
        val levelMin = equalizer.bandLevelRange[0]

        for (band in 0 until numBands) {
            val centerFreq = equalizer.getCenterFreq(band.toShort())
            val kHz = centerFreq / 1000f

            val layout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.setMargins(0, 8, 0, 16) }
            }

            val label = TextView(context).apply {
                text = String.format(Locale.getDefault(), "%.1f kHz", kHz)
                setTextAppearance(android.R.style.TextAppearance_Material_Subhead)
            }

            val seekBar = SeekBar().apply {
                max = levelMax - levelMin
                progress = (equalizer.getBandLevel(band.toShort()) - levelMin).toInt()
            }

            val display = TextView(context).apply {
                text = "${seekBar.progress + levelMin} dB"
                gravity = Gravity.END
                setTextAppearance(android.R.style.TextAppearance_Material_Small)
            }

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seek: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (!fromUser) return
                    val level = (progress + levelMin).toShort()
                    equalizer.setBandLevel(band.toShort(), level)
                    display.text = "$level dB"
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })

            layout.addView(label)
            layout.addView(seekBar)
            layout.addView(display)
            containerBands.addView(layout)

            filters.add(BandFilter(band, seekBar, display))
        }
    }

    private fun updateAllSeekBars(equalizer: android.media.audiofx.Equalizer) {
        for (filter in filters) {
            val level = equalizer.getBandLevel(filter.band.toShort())
            val levelMin = equalizer.bandLevelRange[0]
            filter.seekBar.progress = (level - levelMin).toInt()
            filter.display.text = "$level dB"
        }
    }

    override fun onPause() {
        super.onPause()
        // Leave equalizer active — user expects persistence
    }

    data class BandFilter(val band: Short, val seekBar: SeekBar, val display: TextView)
}