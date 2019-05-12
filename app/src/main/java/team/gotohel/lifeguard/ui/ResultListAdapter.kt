package team.gotohel.lifeguard.ui

import android.content.Context
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_result_list_header.view.*
import kotlinx.android.synthetic.main.item_result_list_sub.view.*
import team.gotohel.lifeguard.MyPreference
import team.gotohel.lifeguard.R
import java.io.File

class ResultListAdapter(val context: Context, val imageFileName: String, val ingredientList: ArrayList<Pair<String, String?>>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val warningList: List<String>
    init {

        val myAllergenList = MyPreference.savedAllergenList
        warningList = ingredientList.map { it.first }.filter { ingredient ->
            myAllergenList?.any { ingredient.contains(it) } == true
        }
    }

    companion object {
        val VIEW_TYPE_HEADER = 0
        val VIEW_TYPE_SUB_TITLE = 1
        val VIEW_TYPE_SUB_ITEM = 2
    }

    override fun getItemCount(): Int {
        return ingredientList.size + 2
    }

    override fun getItemViewType(position: Int): Int {
        return when(position) {
            0 -> VIEW_TYPE_HEADER
            1 -> VIEW_TYPE_SUB_TITLE
            else -> VIEW_TYPE_SUB_ITEM
        }
    }

    class HeaderViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)
    class SubTitleViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)
    class SubItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            VIEW_TYPE_HEADER -> HeaderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_result_list_header, parent, false))
            VIEW_TYPE_SUB_TITLE -> SubTitleViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_result_list_sub_title, parent, false))
            VIEW_TYPE_SUB_ITEM -> SubItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_result_list_sub, parent, false))
            else -> throw Exception("Invalid View Type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> {
                Glide.with(context)
                    .load(File(Environment.getExternalStorageDirectory(), "${CameraActivity.LIFEGUARD_FOLDER_NAME}/$imageFileName.jpg"))
                    .into(holder.itemView.img_phoro_result)

                holder.itemView.img_check.setImageResource(if (warningList.isEmpty()) R.drawable.ic_success else R.drawable.ic_warning)
                if (warningList.isEmpty()) {
                    holder.itemView.part_success_detail.visibility = View.VISIBLE
                    holder.itemView.part_warning_detail.visibility = View.GONE
                } else {
                    holder.itemView.part_success_detail.visibility = View.GONE
                    holder.itemView.part_warning_detail.visibility = View.VISIBLE

                    holder.itemView.text_warning_list.text = warningList.joinToString(", ")
                }
            }
            is SubTitleViewHolder -> {

            }
            is SubItemViewHolder -> {
                val ingredient = ingredientList[position - 2]
                holder.itemView.text_ingredient_name.text = ingredient.first
                holder.itemView.text_ingredient_metric.text = ingredient.second ?: ""
                if (position-2 == ingredientList.size-1) {
                    holder.itemView.part_margin_bottom.visibility = View.VISIBLE
                } else {
                    holder.itemView.part_margin_bottom.visibility = View.GONE
                }
            }
        }
    }
}