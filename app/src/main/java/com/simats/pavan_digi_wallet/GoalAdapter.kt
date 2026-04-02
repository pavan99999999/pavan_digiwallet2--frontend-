package com.simats.pavan_digi_wallet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator

class GoalAdapter(
    private var goals: List<GoalData>,
    private val onGoalClicked: (GoalData) -> Unit
) : RecyclerView.Adapter<GoalAdapter.GoalViewHolder>() {

    class GoalViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tv_goal_name)
        val tvSaved: TextView = view.findViewById(R.id.tv_saved)
        val tvTarget: TextView = view.findViewById(R.id.tv_target)
        val tvPercent: TextView = view.findViewById(R.id.tv_percent)
        val progress: LinearProgressIndicator = view.findViewById(R.id.progress_goal)
        val tvPriority: TextView = view.findViewById(R.id.tv_priority)
        val tvMonthly: TextView = view.findViewById(R.id.tv_monthly_info)
        val tvDaysLeft: TextView = view.findViewById(R.id.tv_days_left)
        val tvRemaining: TextView = view.findViewById(R.id.tv_amount_remaining)
        val imgIcon: android.widget.ImageView = view.findViewById(R.id.img_goal_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_goal, parent, false)
        return GoalViewHolder(view)
    }

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        val goal = goals[position]
        holder.tvName.text = goal.goalName
        
        // Amount Binding
        holder.tvSaved.text = "₹${String.format("%,.0f", goal.savedAmount)}"
        holder.tvTarget.text = "of ₹${String.format("%,.0f", goal.targetAmount)}"
        
        // Progress
        holder.tvPercent.text = String.format("%.1f%%", goal.progressPercentage)
        holder.progress.progress = goal.progressPercentage.toInt()
        
        // Priority / Status logic
        holder.tvPriority.text = goal.status.substringBefore(" ").uppercase()
        
        // Health color coding
        when {
            goal.status.contains("🟢") || goal.status.contains("Achievable") -> {
                holder.tvPriority.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#1E3A2F"))
                holder.tvPriority.setTextColor(android.graphics.Color.parseColor("#2ECC71"))
            }
            goal.status.contains("🔴") || goal.status.contains("Shortfall") -> {
                holder.tvPriority.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#3A1E1E"))
                holder.tvPriority.setTextColor(android.graphics.Color.parseColor("#E74C3C"))
            }
            else -> {
                holder.tvPriority.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#252E3E"))
                holder.tvPriority.setTextColor(android.graphics.Color.parseColor("#9A9FA5"))
            }
        }
        
        // Days Left & Remaining
        holder.tvDaysLeft.text = goal.daysRemaining.toString()
        val remaining = goal.targetAmount - goal.savedAmount
        holder.tvRemaining.text = "₹${String.format("%,.0f", if (remaining > 0) remaining else 0.0)}"
        
        // Monthly Auto-Save
        holder.tvMonthly.text = "+ ₹${String.format("%,.0f", goal.monthlyPayment)}/mo"

        // Dynamic Icon Logic (based on name or keywords)
        when {
            goal.goalName.lowercase().contains("car") -> holder.imgIcon.setImageResource(R.drawable.ic_car_alt)
            goal.goalName.lowercase().contains("laptop") -> holder.imgIcon.setImageResource(R.drawable.ic_laptop)
            goal.goalName.lowercase().contains("house") || goal.goalName.lowercase().contains("home") -> holder.imgIcon.setImageResource(R.drawable.ic_home)
            goal.goalName.lowercase().contains("travel") || goal.goalName.lowercase().contains("trip") -> holder.imgIcon.setImageResource(R.drawable.ic_travel)
            else -> holder.imgIcon.setImageResource(R.drawable.ic_target)
        }

        holder.itemView.setOnClickListener {
            onGoalClicked(goal)
        }
    }

    override fun getItemCount() = goals.size

    fun updateData(newGoals: List<GoalData>) {
        goals = newGoals
        notifyDataSetChanged()
    }
}
