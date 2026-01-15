package com.regula.idv.sample.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.regula.idv.sample.databinding.ItemWorkflowBinding

class UiWorkflowsAdapter(
    private val onItemClick: (UiWorkflow) -> Unit,
) : ListAdapter<UiWorkflow, UiWorkflowsAdapter.ViewHolder>(WorkflowDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            ItemWorkflowBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemWorkflowBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(workflow: UiWorkflow) {
            binding.rbWorkflowName.text = workflow.name

            binding.rbWorkflowName.isChecked = workflow.isSelected
            binding.rbWorkflowName.isClickable = false

            binding.root.setOnClickListener {
                onItemClick.invoke(workflow)
            }
        }
    }
}

private class WorkflowDiffCallback : DiffUtil.ItemCallback<UiWorkflow>() {

    override fun areItemsTheSame(oldItem: UiWorkflow, newItem: UiWorkflow): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: UiWorkflow, newItem: UiWorkflow): Boolean =
        oldItem == newItem
}
