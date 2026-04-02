package com.simats.pavan_digi_wallet

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class Category(val name: String, val iconRes: Int, val groupLabel: String? = null)

class CategoryAdapter(private val categories: List<Category>, private val type: String = "expense") : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvGroupLabel: TextView = view.findViewById(R.id.tv_group_label)
        val imgIcon: ImageView = view.findViewById(R.id.img_category_icon)
        val tvName: TextView = view.findViewById(R.id.tv_category_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category_grid, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.tvName.text = category.name
        holder.imgIcon.setImageResource(category.iconRes)
        
        if (category.groupLabel != null) {
            holder.tvGroupLabel.visibility = View.VISIBLE
            holder.tvGroupLabel.text = category.groupLabel
        } else {
            holder.tvGroupLabel.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, AmountInputActivity::class.java)
            intent.putExtra("category", category.name)
            intent.putExtra("icon_res", category.iconRes)
            intent.putExtra("type", type)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount() = categories.size
}
