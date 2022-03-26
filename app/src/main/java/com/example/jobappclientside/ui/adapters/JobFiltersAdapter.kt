package com.example.jobappclientside.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.jobappclientside.databinding.FilterItemViewBinding
import com.example.jobappclientside.datamodels.regular.JobFilter
import com.example.jobappclientside.datamodels.regular.JobFilterItem
import com.example.jobappclientside.ui.core.viewmodels.JobSearchViewModel
import java.lang.NumberFormatException
import java.text.NumberFormat
import java.util.*

class JobFiltersAdapter(private val viewModel: JobSearchViewModel): RecyclerView.Adapter<JobFiltersAdapter.FilterViewHolder>() {

    class FilterViewHolder(val binding: FilterItemViewBinding): RecyclerView.ViewHolder(binding.root)


    private val itemCallback = object: DiffUtil.ItemCallback<JobFilterItem>() {
        override fun areItemsTheSame(oldItem: JobFilterItem, newItem: JobFilterItem): Boolean {
            return oldItem.filterName  == newItem.filterName
        }

        override fun areContentsTheSame(oldItem: JobFilterItem, newItem: JobFilterItem): Boolean {
            return oldItem.filterValue == newItem.filterValue
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

            when(curItem.filterName) {
                "jobMinSalary" -> {
                    val numberFormat = NumberFormat.getCurrencyInstance()
                    numberFormat.maximumFractionDigits = 0
                    val convertedFormat = numberFormat.format(curItem.filterValue.toInt())
                    tvFilterText.text = convertedFormat
                }
                "jobRemote" -> {
                    if(curItem.filterValue == "Yes") {
                        tvFilterText.text = "Remote"
                    } else {
                        tvFilterText.text = "On-site"
                    }
                }
                else -> {
                    tvFilterText.text = curItem.filterValue
                }
            }

            imgRemoveFilter.setOnClickListener {
                onFilterClick?.let { removeFilter ->
                    removeFilter(curItem)
                }
            }
        }
    }

    private var onFilterClick: ((JobFilterItem) -> Unit)? = null

    fun setOnFilterClick(functionality: (JobFilterItem) -> Unit) {
        onFilterClick = functionality
    }
}