package team.gotohel.lifeguard.ui

import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_allergen_list.view.*
import team.gotohel.lifeguard.MyPreference
import team.gotohel.lifeguard.R

class AllergenListAdapter(val allergenListActivity: AllergenListActivity): RecyclerView.Adapter<AllergenListAdapter.AllergenListItemViewHolder>() {

    private val allergenList = mutableListOf<String>()

    fun setList(list: List<String>) {
        allergenList.clear()
        allergenList.addAll(list)
        notifyDataSetChanged()
    }

    private fun removeItem(position: Int) {
        allergenList.removeAt(position)
        MyPreference.savedAllergenList = allergenList
        allergenListActivity.refreshAllergenList()
    }

    override fun getItemCount(): Int {
        return allergenList.size
    }

    class AllergenListItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllergenListItemViewHolder {
        return AllergenListItemViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_allergen_list,
                parent,
                false
            )
        )
    }


    override fun onBindViewHolder(holder: AllergenListItemViewHolder, position: Int) {

        holder.itemView.text_allergen_name.text = allergenList[position]

        holder.itemView.text_allergen_name.setOnLongClickListener {
            AlertDialog.Builder(allergenListActivity)
                .setTitle("DELETE")
                .setMessage("Do you want delete '${allergenList[position]}'?")
                .setPositiveButton("delete") { _, _ -> removeItem(position) }
                .setNegativeButton("cancel", null)
                .show()
            return@setOnLongClickListener true
        }

        if (position == 0) {
            holder.itemView.view_margin_top.visibility = View.VISIBLE
        } else {
            holder.itemView.view_margin_top.visibility = View.GONE
        }

        if (position == allergenList.size-1) {
            holder.itemView.view_divider.visibility = View.GONE
            holder.itemView.view_margin_bottom.visibility = View.VISIBLE
        } else {
            holder.itemView.view_divider.visibility = View.VISIBLE
            holder.itemView.view_margin_bottom.visibility = View.GONE
        }
    }
}