package com.example.iqromandarin

import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.iqromandarin.adapter.ItemAdapter
import com.example.iqromandarin.audio.AudioHandler
import com.example.iqromandarin.database.AppDatabase
import com.example.iqromandarin.databinding.FragmentHalamanBinding
import com.example.iqromandarin.model.Item
import com.example.iqromandarin.viewmodel.MainViewModel
import com.example.iqromandarin.viewmodel.MainViewModelFactory
import java.io.File

class HalamanFragment : Fragment() {

    private var _binding: FragmentHalamanBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(AppDatabase.getDatabase(requireContext()))
    }

    private lateinit var itemAdapter: ItemAdapter
    private lateinit var audioHandler: AudioHandler
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private var tempAudioFile: File? = null
    private var readCount = 0

    private var jilidId: Int = 1
    private var halamanId: Int = 1
    private var halamanIndex: Int = 0
    private var totalHalaman: Int = 1
    private var isUnlocked: Boolean = false

    companion object {
        private const val ARG_JILID_ID = "jilid_id"
        private const val ARG_HALAMAN_ID = "halaman_id"
        private const val ARG_HALAMAN_INDEX = "halaman_index"
        private const val ARG_TOTAL_HALAMAN = "total_halaman"
        private const val ARG_IS_UNLOCKED = "is_unlocked"
        private const val READ_TARGET = 5

        fun newInstance(
            jilidId: Int,
            halamanId: Int,
            halamanIndex: Int,
            totalHalaman: Int,
            isUnlocked: Boolean
        ): HalamanFragment {
            return HalamanFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_JILID_ID, jilidId)
                    putInt(ARG_HALAMAN_ID, halamanId)
                    putInt(ARG_HALAMAN_INDEX, halamanIndex)
                    putInt(ARG_TOTAL_HALAMAN, totalHalaman)
                    putBoolean(ARG_IS_UNLOCKED, isUnlocked)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        jilidId = arguments?.getInt(ARG_JILID_ID) ?: 1
        halamanId = arguments?.getInt(ARG_HALAMAN_ID) ?: 1
        halamanIndex = arguments?.getInt(ARG_HALAMAN_INDEX) ?: 0
        totalHalaman = arguments?.getInt(ARG_TOTAL_HALAMAN) ?: 1
        isUnlocked = arguments?.getBoolean(ARG_IS_UNLOCKED) ?: false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHalamanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        audioHandler = AudioHandler(requireContext())

        setupUI()
        loadItems()
        setupClickListeners()
    }

    private fun setupUI() {
        // Page counter
        binding.tvHalamanCounter.text = "Halaman ${halamanIndex + 1} / $totalHalaman"

        // Lock/unlock UI
        if (!isUnlocked && halamanIndex > 0) {
            binding.lockOverlay.visibility = View.VISIBLE
            binding.btnLancar.isEnabled = false
        } else {
            binding.lockOverlay.visibility = View.GONE
            binding.btnLancar.isEnabled = true
        }

        // Read progress
        updateReadProgress()
    }

    private fun loadItems() {
        itemAdapter = ItemAdapter(
            onPlayClick = { item -> playItemAudio(item) },
            onItemLongClick = { item -> showItemDetail(item) }
        )

        binding.rvItems.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = itemAdapter
        }

        viewModel.getItemsByHalaman(halamanId).observe(viewLifecycleOwner) { items ->
            itemAdapter.submitList(items)
            binding.tvItemCount.text = "${items.size} item"
        }
    }

    private fun setupClickListeners() {
        // Play all TTS
        binding.btnPlayAll.setOnClickListener {
            itemAdapter.currentList.forEach { item ->
                audioHandler.speakChinese(item.pinyin)
            }
            incrementReadCount()
        }

        // Record audio
        binding.btnRecord.setOnClickListener {
            if (!isRecording) {
                startRecording()
            } else {
                stopRecording()
            }
        }

        // Playback recorded audio
        binding.btnPlayback.setOnClickListener {
            playRecordedAudio()
        }

        // Mark as Lancar (Mastered) — unlock next halaman
        binding.btnLancar.setOnClickListener {
            if (readCount < READ_TARGET) {
                Toast.makeText(requireContext(),
                    "Baca keras minimal $READ_TARGET kali dulu! ($readCount/$READ_TARGET)",
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.unlockNextHalaman(jilidId, halamanId, halamanIndex)
            viewModel.markHalamanSelesai(halamanId)

            // Mark all items as mastered
            itemAdapter.currentList.forEach { item ->
                viewModel.updateItemProgres(item.id, isKuasai = true,
                    srsBox = 1, nextReviewDays = 1)
            }

            Toast.makeText(requireContext(), "🎉 Lancar! Halaman berikutnya terbuka.", Toast.LENGTH_LONG).show()

            // Auto-swipe to next page after delay
            view?.postDelayed({
                parentFragment?.let { parent ->
                    if (parent is JilidFragment) {
                        val vp = (parent._binding)?.viewPager
                        vp?.currentItem = halamanIndex + 1
                    }
                }
            }, 1000)
        }

        // Add custom item
        binding.btnTambah.setOnClickListener {
            showAddItemDialog()
        }
    }

    private fun incrementReadCount() {
        readCount++
        updateReadProgress()
    }

    private fun updateReadProgress() {
        binding.tvReadCount.text = "Dibaca: $readCount / $READ_TARGET kali"
        binding.progressRead.progress = (readCount.toFloat() / READ_TARGET * 100).toInt()
        if (readCount >= READ_TARGET) {
            binding.tvReadCount.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.green_primary)
            )
            binding.btnLancar.isEnabled = true
            binding.btnLancar.alpha = 1.0f
        }
    }

    private fun playItemAudio(item: Item) {
        // Try raw resource first, fallback to TTS
        if (item.audioResName != null) {
            val resId = resources.getIdentifier(item.audioResName, "raw", requireContext().packageName)
            if (resId != 0) {
                audioHandler.playRawAudio(resId)
                return
            }
        }
        // Fallback: TTS
        audioHandler.speakChinese(item.hanzi ?: item.pinyin)
        incrementReadCount()
    }

    private fun showItemDetail(item: Item) {
        val dialog = ItemDetailBottomSheet.newInstance(item)
        dialog.show(parentFragmentManager, "ItemDetail")
    }

    // ---- Recording ----

    private fun startRecording() {
        try {
            tempAudioFile = File(requireContext().cacheDir, "record_temp.3gp")
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(tempAudioFile!!.absolutePath)
                prepare()
                start()
            }
            isRecording = true
            binding.btnRecord.text = "⏹ Stop Rekam"
            binding.btnRecord.setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.red_tone)
            )
            Toast.makeText(requireContext(), "🎙 Merekam... Baca pinyin yang ada!", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Gagal merekam: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        isRecording = false
        binding.btnRecord.text = "🎙 Rekam Suara"
        binding.btnRecord.setBackgroundColor(
            ContextCompat.getColor(requireContext(), R.color.purple_tone)
        )
        binding.btnPlayback.visibility = View.VISIBLE
        Toast.makeText(requireContext(), "✅ Rekaman selesai! Tekan Playback.", Toast.LENGTH_SHORT).show()
        incrementReadCount()
    }

    private fun playRecordedAudio() {
        tempAudioFile?.let { file ->
            if (file.exists()) {
                try {
                    val player = MediaPlayer()
                    player.setDataSource(file.absolutePath)
                    player.prepare()
                    player.start()
                    Toast.makeText(requireContext(), "▶️ Memutar rekaman kamu...", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Gagal putar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showAddItemDialog() {
        val dialog = AddItemDialog.newInstance(halamanId, jilidId)
        dialog.show(parentFragmentManager, "AddItem")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        audioHandler.release()
        mediaRecorder?.release()
        mediaRecorder = null
        _binding = null
    }
}
