package com.example.iqromandarin.adapter

import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.iqromandarin.R
import com.example.iqromandarin.databinding.ItemHalamanCardBinding
import com.example.iqromandarin.model.Item

class ItemAdapter(
    private val onPlayClick: (Item) -> Unit,
    private val onItemLongClick: (Item) -> Unit
) : ListAdapter<Item, ItemAdapter.ItemViewHolder>(ItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemHalamanCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ItemViewHolder(private val binding: ItemHalamanCardBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Item) {
            // Hanzi (large character)
            if (!item.hanzi.isNullOrEmpty()) {
                binding.tvHanzi.visibility = View.VISIBLE
                binding.tvHanzi.text = item.hanzi
            } else {
                binding.tvHanzi.visibility = View.GONE
            }

            // Pinyin with tone coloring
            val coloredPinyin = colorPinyin(item.pinyin, binding.root)
            binding.tvPinyin.text = coloredPinyin

            // Indo-friendly pronunciation
            if (item.indoPron.isNotEmpty()) {
                binding.tvIndoPron.visibility = View.VISIBLE
                binding.tvIndoPron.text = "🇮🇩 ${item.indoPron}"
            } else {
                binding.tvIndoPron.visibility = View.GONE
            }

            // Arti (meaning)
            binding.tvArti.text = item.arti

            // Contoh (example)
            if (!item.contoh.isNullOrEmpty()) {
                binding.tvContoh.visibility = View.VISIBLE
                binding.tvContoh.text = "📝 ${item.contoh}"
            } else {
                binding.tvContoh.visibility = View.GONE
            }

            // Kategori badge
            binding.tvKategori.text = item.kategori

            // Mastery indicator
            if (item.isKuasai) {
                binding.ivMastery.visibility = View.VISIBLE
                binding.cardRoot.setCardBackgroundColor(
                    ContextCompat.getColor(binding.root.context, R.color.green_light)
                )
            } else {
                binding.ivMastery.visibility = View.GONE
                binding.cardRoot.setCardBackgroundColor(
                    ContextCompat.getColor(binding.root.context, R.color.card_background)
                )
            }

            // SRS info
            if (item.srsBox > 0) {
                binding.tvSrsBox.visibility = View.VISIBLE
                binding.tvSrsBox.text = "Box ${item.srsBox}"
            } else {
                binding.tvSrsBox.visibility = View.GONE
            }

            // Click to play audio
            binding.btnPlay.setOnClickListener {
                onPlayClick(item)
                // Animate button
                it.animate().scaleX(0.85f).scaleY(0.85f).setDuration(100)
                    .withEndAction { it.animate().scaleX(1f).scaleY(1f).setDuration(100) }
            }

            // Long click for detail
            binding.root.setOnLongClickListener {
                onItemLongClick(item)
                true
            }
        }

        /**
         * Color pinyin based on tone number:
         * Tone 1 (ā ē ī ō ū) = Red
         * Tone 2 (á é í ó ú) = Green
         * Tone 3 (ǎ ě ǐ ǒ ǔ) = Blue
         * Tone 4 (à è ì ò ù) = Purple
         * Neutral (no mark) = Gray
         */
        private fun colorPinyin(pinyin: String, view: View): SpannableString {
            val spannable = SpannableString(pinyin)
            val ctx = view.context

            val tone1Color = ContextCompat.getColor(ctx, R.color.red_tone)
            val tone2Color = ContextCompat.getColor(ctx, R.color.green_primary)
            val tone3Color = ContextCompat.getColor(ctx, R.color.blue_tone)
            val tone4Color = ContextCompat.getColor(ctx, R.color.purple_tone)
            val neutralColor = ContextCompat.getColor(ctx, R.color.gray_neutral)

            pinyin.forEachIndexed { index, char ->
                val color = when (char) {
                    'ā', 'ē', 'ī', 'ō', 'ū', 'Ā', 'Ē', 'Ī', 'Ō', 'Ū' -> tone1Color
                    'á', 'é', 'í', 'ó', 'ú', 'ǘ', 'Á', 'É', 'Í', 'Ó', 'Ú' -> tone2Color
                    'ǎ', 'ě', 'ǐ', 'ǒ', 'ǔ', 'ǚ', 'Ǎ', 'Ě', 'Ǐ', 'Ǒ', 'Ǔ' -> tone3Color
                    'à', 'è', 'ì', 'ò', 'ù', 'ǜ', 'À', 'È', 'Ì', 'Ò', 'Ù' -> tone4Color
                    else -> null
                }
                color?.let {
                    spannable.setSpan(
                        ForegroundColorSpan(it),
                        index, index + 1,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
            return spannable
        }
    }

    class ItemDiffCallback : DiffUtil.ItemCallback<Item>() {
        override fun areItemsTheSame(oldItem: Item, newItem: Item) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Item, newItem: Item) = oldItem == newItem
    }
}
