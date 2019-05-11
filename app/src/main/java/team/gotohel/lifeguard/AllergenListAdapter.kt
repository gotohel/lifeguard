package team.gotohel.lifeguard

import android.content.Context
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_allergen_list.view.*

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
        return AllergenListItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_allergen_list, parent, false))
    }


    override fun onBindViewHolder(holder: AllergenListItemViewHolder, position: Int) {

        holder.itemView.text_allergen_name.text = allergenList[position]

        holder.itemView.setOnLongClickListener {
            AlertDialog.Builder(allergenListActivity)
                .setTitle("DELETE")
                .setMessage("Do you want delete '${allergenList[position]}'?")
                .setPositiveButton("delete") { _, _ -> removeItem(position) }
                .setNegativeButton("cancel", null)
                .show()
            return@setOnLongClickListener true
        }

    }
}