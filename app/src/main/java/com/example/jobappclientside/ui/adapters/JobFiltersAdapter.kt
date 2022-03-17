package com.example.jobappclientside.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.jobappclientside.databinding.FilterItemViewBinding
import com.example.jobappclientside.datamodels.regular.JobFilter
import com.example.jobappclientside.datamodels.regular.JobFilterItem
import com.example.jobappclientside.ui.core.viewmodels.JobSearchViewModel

class JobFiltersAdapter(private val viewModel: JobSearchViewModel): RecyclerView.Adapter<JobFiltersAdapter.FilterViewHolder>() {

    class FilterViewHolder(val binding: FilterItemViewBinding): RecyclerView.ViewHolder(binding.root)


    private val itemCallback = object: DiffUtil.ItemCallback<JobFilterItem>() {
        override fun areItemsTheSame(oldItem: JobFilterItem, newItem: JobFilterItem): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: JobFilterItem, newItem: JobFilterItem): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, itemCallback)

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterViewHolder {
        val layout = FilterItemViewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return FilterViewHolder(layout)
    }

    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
        val curItem = differ.currentList[position]
        holder.binding.apply {
            tvFilterText.text = curItem.filterValue
            onFilterClick?.let { removeFilter ->
                removeFilter(curItem)
            }
        }
    }

    private var onFilterClick: ((JobFilterItem) -> Unit)? = null

    fun setOnFilterClick(functionality: (JobFilterItem) -> Unit) {
        onFilterClick = functionality
    }
}