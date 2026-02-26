package com.example.iqromandarin.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.iqromandarin.R
import com.example.iqromandarin.databinding.ItemJilidCardBinding
import com.example.iqromandarin.model.Jilid

class JilidAdapter(
    private val onJilidClick: (Jilid) -> Unit
) : ListAdapter<Jilid, JilidAdapter.JilidViewHolder>(JilidDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JilidViewHolder {
        val binding = ItemJilidCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return JilidViewHolder(binding)
    }

    override fun onBindViewHolder(holder: JilidViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class JilidViewHolder(private val binding: ItemJilidCardBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(jilid: Jilid) {
            binding.tvJilidNomor.text = "Jilid ${jilid.nomorJilid}"
            binding.tvJilidNama.text = jilid.nama
            binding.tvJilidCina.text = jilid.namaCina
            binding.tvJilidDesc.text = jilid.deskripsi
            binding.tvJilidIcon.text = jilid.icon

            val ctx = binding.root.context

            if (jilid.isUnlocked) {
                // Unlocked: show accent color, clickable
                binding.root.isEnabled = true
                binding.root.alpha = 1.0f
                binding.cardRoot.setCardBackgroundColor(
                    ContextCompat.getColor(ctx, R.color.card_background)
                )
                binding.tvStatusBadge.text = if (jilid.isSelesai) "✅ Selesai" else "▶️ Aktif"
                binding.tvStatusBadge.setBackgroundColor(
                    if (jilid.isSelesai)
                        ContextCompat.getColor(ctx, R.color.green_primary)
                    else
                        ContextCompat.getColor(ctx, R.color.blue_tone)
                )

                binding.root.setOnClickListener {
                    onJilidClick(jilid)
                }
            } else {
                // Locked: grayed out, not clickable
                binding.root.isEnabled = false
                binding.root.alpha = 0.45f
                binding.tvStatusBadge.text = "🔒 Terkunci"
                binding.tvStatusBadge.setBackgroundColor(
                    ContextCompat.getColor(ctx, R.color.gray_neutral)
                )
                binding.root.setOnClickListener(null)
            }

            // Progress bar: based on totalHalaman vs completed
            binding.progressJilid.max = maxOf(jilid.totalHalaman, 1)
        }
    }

    class JilidDiffCallback : DiffUtil.ItemCallback<Jilid>() {
        override fun areItemsTheSame(oldItem: Jilid, newItem: Jilid) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Jilid, newItem: Jilid) = oldItem == newItem
    }
}
