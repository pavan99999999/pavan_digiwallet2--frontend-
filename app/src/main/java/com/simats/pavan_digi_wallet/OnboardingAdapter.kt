package com.simats.pavan_digi_wallet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class OnboardingAdapter(private val items: List<OnboardingPage>) : RecyclerView.Adapter<OnboardingAdapter.ViewHolder>() {

    data class OnboardingPage(
        val title: String,
        val description: String,
        val imageRes: Int,
        val bgColor: Int
    )

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val container: CardView = view.findViewById(R.id.image_container)
        val imageView: ImageView = view.findViewById(R.id.onboarding_image)
        val titleText: TextView = view.findViewById(R.id.title_text)
        val descriptionText: TextView = view.findViewById(R.id.description_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_onboarding, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val page = items[position]
        holder.container.setCardBackgroundColor(page.bgColor)
        holder.imageView.setImageResource(page.imageRes)
        holder.titleText.text = page.title
        holder.descriptionText.text = page.description
    }

    override fun getItemCount() = items.size
}
