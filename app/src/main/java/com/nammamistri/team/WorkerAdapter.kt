package com.nammamistri.team

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nammamistri.R
import com.nammamistri.database.Worker

class WorkerAdapter(
    private var workers: List<Worker>,
    private val onAddDay: (Worker) -> Unit,
    private val onAddAdvance: (Worker, Double) -> Unit,
    private val onDelete: (Worker) -> Unit
) : RecyclerView.Adapter<WorkerAdapter.WorkerViewHolder>() {

    // ViewHolder holds references to the views in each list item
    class WorkerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName:        TextView = itemView.findViewById(R.id.tv_worker_name)
        val tvWage:        TextView = itemView.findViewById(R.id.tv_wage)
        val tvDays:        TextView = itemView.findViewById(R.id.tv_days)
        val tvEarned:      TextView = itemView.findViewById(R.id.tv_total_earned)
        val tvAdvance:     TextView = itemView.findViewById(R.id.tv_advance)
        val tvBalance:     TextView = itemView.findViewById(R.id.tv_balance)
        val btnAddDay:     Button   = itemView.findViewById(R.id.btn_add_day)
        val btnAddAdvance: Button   = itemView.findViewById(R.id.btn_add_advance)
        val btnDelete:     Button   = itemView.findViewById(R.id.btn_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_worker, parent, false)
        return WorkerViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkerViewHolder, position: Int) {
        val worker = workers[position]

        // Calculate financials
        val totalEarned = worker.dailyWage * worker.totalDays
        val balance = totalEarned - worker.totalAdvance

        // Fill in the data
        holder.tvName.text    = worker.name
        holder.tvWage.text    = "₹${worker.dailyWage.toInt()}/day"
        holder.tvDays.text    = "${worker.totalDays}"
        holder.tvEarned.text  = "₹${totalEarned.toInt()}"
        holder.tvAdvance.text = "₹${worker.totalAdvance.toInt()}"
        holder.tvBalance.text = "₹${balance.toInt()}"

        // Button: Add 1 day of work
        holder.btnAddDay.setOnClickListener {
            onAddDay(worker)
        }

        // Button: Add advance payment — shows a dialog box
        holder.btnAddAdvance.setOnClickListener {
            showAdvanceDialog(holder.itemView.context, worker)
        }

        // Button: Delete worker
        holder.btnDelete.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Delete Worker?")
                .setMessage("Delete ${worker.name} from your team?")
                .setPositiveButton("Delete") { _, _ -> onDelete(worker) }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun showAdvanceDialog(context: Context, worker: Worker) {
        val input = EditText(context).apply {
            hint = "Enter advance amount ₹"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                    android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            textSize = 18f
            setPadding(32, 16, 32, 16)
        }

        AlertDialog.Builder(context)
            .setTitle("Advance for ${worker.name}")
            .setView(input)
            .setPositiveButton("Add Advance") { _, _ ->
                val amount = input.text.toString().toDoubleOrNull()
                if (amount != null && amount > 0) {
                    onAddAdvance(worker, amount)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun getItemCount() = workers.size

    // Call this to refresh the list when data changes
    fun updateList(newWorkers: List<Worker>) {
        workers = newWorkers
        notifyDataSetChanged()
    }
}