package com.example.iqromandarin

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.iqromandarin.database.AppDatabase
import com.example.iqromandarin.model.Item
import com.example.iqromandarin.viewmodel.MainViewModel
import com.example.iqromandarin.viewmodel.MainViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

// ---------- Add Custom Item Dialog ----------
class AddItemDialog : DialogFragment() {

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(AppDatabase.getDatabase(requireContext()))
    }

    private var halamanId: Int = 1
    private var jilidId: Int = 1

    companion object {
        fun newInstance(halamanId: Int, jilidId: Int) = AddItemDialog().apply {
            arguments = Bundle().apply {
                putInt("halaman_id", halamanId)
                putInt("jilid_id", jilidId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        halamanId = arguments?.getInt("halaman_id") ?: 1
        jilidId = arguments?.getInt("jilid_id") ?: 1
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_item, null)

        val etPinyin = view.findViewById<EditText>(R.id.et_pinyin)
        val etHanzi = view.findViewById<EditText>(R.id.et_hanzi)
        val etIndoPron = view.findViewById<EditText>(R.id.et_indo_pron)
        val etArti = view.findViewById<EditText>(R.id.et_arti)

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("➕ Tambah Item Sendiri")
            .setView(view)
            .setPositiveButton("Simpan") { _, _ ->
                val pinyin = etPinyin.text.toString().trim()
                val hanzi = etHanzi.text.toString().trim()
                val indoPron = etIndoPron.text.toString().trim()
                val arti = etArti.text.toString().trim()

                if (pinyin.isEmpty() || arti.isEmpty()) {
                    Toast.makeText(requireContext(), "Pinyin & Arti wajib diisi!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                viewModel.addCustomItem(
                    Item(
                        id = 0, // auto-generate
                        pinyin = pinyin,
                        hanzi = hanzi.ifEmpty { null },
                        indoPron = indoPron,
                        arti = arti,
                        halamanId = halamanId,
                        jilidId = jilidId,
                        kategori = "CUSTOM",
                        isCustom = true
                    )
                )
                Toast.makeText(requireContext(), "✅ Item ditambahkan!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .create()
    }
}

// ---------- Item Detail BottomSheet ----------
class ItemDetailBottomSheet : BottomSheetDialogFragment() {

    private var item: Item? = null

    companion object {
        fun newInstance(item: Item) = ItemDetailBottomSheet().apply {
            arguments = Bundle().apply {
                putParcelable("item", item)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        @Suppress("DEPRECATION")
        item = arguments?.getParcelable("item")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_item_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentItem = item ?: return

        view.findViewById<TextView>(R.id.tv_detail_hanzi).text = currentItem.hanzi ?: "-"
        view.findViewById<TextView>(R.id.tv_detail_pinyin).text = currentItem.pinyin
        view.findViewById<TextView>(R.id.tv_detail_indo).text = currentItem.indoPron
        view.findViewById<TextView>(R.id.tv_detail_arti).text = currentItem.arti
        view.findViewById<TextView>(R.id.tv_detail_contoh).text = currentItem.contoh ?: "-"

        // Color tone on pinyin
        val pinyin = currentItem.pinyin
        val spannable = SpannableStringHelper.colorTones(pinyin, view.context)
        view.findViewById<TextView>(R.id.tv_detail_pinyin).text = spannable
    }
}

// ---------- Review Fragment ----------
class ReviewFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(AppDatabase.getDatabase(requireContext()))
    }

    private lateinit var itemAdapter: ItemAdapter
    private lateinit var audioHandler: AudioHandler

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_review, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        audioHandler = AudioHandler(requireContext())

        val rvReview = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rv_review)
        val tvEmpty = view.findViewById<TextView>(R.id.tv_review_empty)

        itemAdapter = ItemAdapter(
            onPlayClick = { item -> audioHandler.speakChinese(item.hanzi ?: item.pinyin) },
            onItemLongClick = {}
        )
        rvReview.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        rvReview.adapter = itemAdapter

        viewModel.getDueReviewItems().observe(viewLifecycleOwner) { items ->
            if (items.isEmpty()) {
                rvReview.visibility = View.GONE
                tvEmpty.visibility = View.VISIBLE
            } else {
                rvReview.visibility = View.VISIBLE
                tvEmpty.visibility = View.GONE
                itemAdapter.submitList(items)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        audioHandler.release()
    }
}
