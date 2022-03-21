package com.example.jobappclientside.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.jobappclientside.R
import com.example.jobappclientside.databinding.JobPostItemViewBinding
import com.example.jobappclientside.datamodels.regular.JobPost
import com.example.jobappclientside.other.Constants.BASE_URL
import java.text.SimpleDateFormat
import java.util.*

class JobPostsAdapter:
    RecyclerView.Adapter<JobPostsAdapter.JobPostViewHolder>() {

    class JobPostViewHolder(val binding: JobPostItemViewBinding): RecyclerView.ViewHolder(binding.root)

   private val itemCallback = object : DiffUtil.ItemCallback<JobPost>() {
        override fun areItemsTheSame(oldItem: JobPost, newItem: JobPost): Boolean {
            return oldItem.jobID == newItem.jobID
        }

        override fun areContentsTheSame(oldItem: JobPost, newItem: JobPost): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, itemCallback)

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobPostViewHolder {
        val layout = JobPostItemViewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return JobPostViewHolder(layout)
    }

    override fun onBindViewHolder(holder: JobPostViewHolder, position: Int) {
        val curJob = differ.currentList[position]
        holder.binding.apply {
            val jobTimestamp = getDateFromTimestamp(curJob.jobTimestamp)
            tvJobTimestamp.text = jobTimestamp
            tvJobTitle.text = curJob.jobTitle
            tvJobCompany.text = curJob.jobCompany
            tvJobLocation.text = curJob.jobLocation
            imgSaveJob.setImageResource(
                if(curJob.isAddedToFavourites) R.drawable.ic_selected_favourite else R.drawable.ic_unselected_favourite
            )

            if(curJob.jobImageUrl.isNotEmpty()) {
                Glide.with(this.root)
                    .load(BASE_URL + curJob.jobImageUrl)
                    .into(imgJobLogo)
            }

            imgSaveJob.setOnClickListener {
                onJobSaveClick?.let { saveJob ->
                    saveJob(curJob)
                }
            }
        }
    }

    private fun getDateFromTimestamp(timeStamp: Long): String {
        val dayFormatTimestamp = SimpleDateFormat("d", Locale.getDefault())
        val monthFormatTimestamp = SimpleDateFormat("LLL", Locale.getDefault())
        val yearFormatTimestamp = SimpleDateFormat("yyy", Locale.getDefault())

        val dayFromTimestamp = dayFormatTimestamp.format(timeStamp)
        val monthFromTimestamp = monthFormatTimestamp.format(timeStamp)
        val yearFromTimestamp = yearFormatTimestamp.format(timeStamp)

        return "$dayFromTimestamp $monthFromTimestamp $yearFromTimestamp"
    }

    fun setOnJobSaveClickListener(listener: (JobPost) -> Unit) {
        onJobSaveClick = listener
    }

    private var onJobSaveClick: ((JobPost) -> Unit)? = null


}