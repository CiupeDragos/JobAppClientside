package com.example.jobappclientside.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.example.jobappclientside.R
import com.example.jobappclientside.databinding.JobPostItemViewBinding
import com.example.jobappclientside.datamodels.regular.JobPost
import com.example.jobappclientside.other.Constants
import java.text.SimpleDateFormat
import java.util.*

class FavouritesAdapter:
    RecyclerView.Adapter<FavouritesAdapter.JobPostViewHolder>() {

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
            imgSaveJob.setImageResource(R.drawable.ic_delete_save_job)

            if(curJob.jobRemote == "Yes") {
                tvJobRemote.text = "(Remote)"
            }

            if(curJob.jobImageUrl.isNotEmpty()) {
                Glide.with(this.root)
                    .load(Constants.BASE_URL + curJob.jobImageUrl)
                    .signature(ObjectKey(UUID.randomUUID()))
                    .into(imgJobLogo)
            }

            imgSaveJob.setOnClickListener {
                onJobDeleteClick?.let { saveJob ->
                    saveJob(curJob)
                }
            }

            root.setOnClickListener {
                onJobClick?.let { openJob ->
                    openJob(curJob)
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

    fun setOnJobDeleteClickListener(listener: (JobPost) -> Unit) {
        onJobDeleteClick = listener
    }

    fun setOnJobClickListener(listener: (JobPost) -> Unit) {
        onJobClick = listener
    }

    private var onJobDeleteClick: ((JobPost) -> Unit)? = null

    private var onJobClick: ((JobPost) -> Unit)? = null

}