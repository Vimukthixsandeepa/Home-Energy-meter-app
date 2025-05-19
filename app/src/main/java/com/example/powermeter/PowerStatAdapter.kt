package com.example.powermeter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class PowerStat(
    val icon: Int,
    val label: String,
    val value: String,
    val backgroundColor: Int
)

class PowerStatAdapter : RecyclerView.Adapter<PowerStatAdapter.StatViewHolder>() {
    private var stats = listOf<PowerStat>()

    fun updateStats(newStats: List<PowerStat>) {
        stats = newStats
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_power_stat, parent, false)
        return StatViewHolder(view)
    }

    override fun onBindViewHolder(holder: StatViewHolder, position: Int) {
        holder.bind(stats[position])
    }

    override fun getItemCount() = stats.size

    class StatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon: ImageView = itemView.findViewById(R.id.statIcon)
        private val label: TextView = itemView.findViewById(R.id.statLabel)
        private val value: TextView = itemView.findViewById(R.id.statValue)
        private val container: View = itemView.findViewById(android.R.id.content)

        fun bind(stat: PowerStat) {
            icon.setImageResource(stat.icon)
            label.text = stat.label
            value.text = stat.value
            container.setBackgroundColor(stat.backgroundColor)
        }
    }
} 