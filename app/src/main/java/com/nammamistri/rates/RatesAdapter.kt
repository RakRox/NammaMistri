package com.nammamistri.rates

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.nammamistri.R
import com.nammamistri.database.MaterialRate

class RatesAdapter(
    private var rates: List<MaterialRate>,
    private val onEdit: (MaterialRate, String, String, Double) -> Unit,
    private val onDelete: (MaterialRate) -> Unit
) : RecyclerView.Adapter<RatesAdapter.RateViewHolder>() {

    // ── ViewHolder holds references to each row's views ───────
    class RateViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName:   TextView = view.findViewById(R.id.tv_material_name)
        val tvUnit:   TextView = view.findViewById(R.id.tv_unit)
        val tvPrice:  TextView = view.findViewById(R.id.tv_price)
        val btnEdit:  Button   = view.findViewById(R.id.btn_edit_rate)
        val btnDel:   Button   = view.findViewById(R.id.btn_delete_rate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        RateViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_rate, parent, false)
        )

    override fun onBindViewHolder(holder: RateViewHolder, position: Int) {
        val rate = rates[position]

        // Fill in the data
        holder.tvName.text  = rate.materialName
        holder.tvUnit.text  = "per ${rate.unit}"
        holder.tvPrice.text = "₹${rate.pricePerUnit.toInt()}"

        // ── EDIT BUTTON ───────────────────────────────────────
        holder.btnEdit.setOnClickListener {
            showEditDialog(holder.itemView.context, rate)
        }

        // ── DELETE BUTTON — shows confirmation first ──────────
        holder.btnDel.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Delete Rate?")
                .setMessage("Delete '${rate.materialName}' from your rates list?")
                .setPositiveButton("Yes, Delete") { _, _ ->
                    onDelete(rate)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    // ── Edit Dialog — pre-filled with existing values ─────────
    private fun showEditDialog(context: Context, rate: MaterialRate) {

        // Build dialog layout programmatically
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 8)
        }

        val etName = EditText(context).apply {
            hint    = "Material Name"
            setText(rate.materialName)
            textSize = 16f
            setPadding(16, 12, 16, 12)
        }

        val etUnit = EditText(context).apply {
            hint    = "Unit (e.g. bag, load)"
            setText(rate.unit)
            textSize = 16f
            setPadding(16, 12, 16, 12)
        }

        val etPrice = EditText(context).apply {
            hint        = "Price ₹"
            setText(rate.pricePerUnit.toInt().toString())
            inputType   = android.text.InputType.TYPE_CLASS_NUMBER or
                    android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            textSize    = 16f
            setPadding(16, 12, 16, 12)
        }

        layout.addView(etName)
        layout.addView(etUnit)
        layout.addView(etPrice)

        AlertDialog.Builder(context)
            .setTitle("✏️ Edit Rate")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->

                // ── Validate fields inside dialog ──────────────
                val newName  = etName.text.toString().trim()
                val newUnit  = etUnit.text.toString().trim()
                val newPrice = etPrice.text.toString().trim().toDoubleOrNull()

                when {
                    newName.isEmpty()  ->
                        Toast.makeText(context, "Name cannot be empty!", Toast.LENGTH_SHORT).show()
                    newUnit.isEmpty()  ->
                        Toast.makeText(context, "Unit cannot be empty!", Toast.LENGTH_SHORT).show()
                    newPrice == null || newPrice <= 0 ->
                        Toast.makeText(context, "Enter a valid price!", Toast.LENGTH_SHORT).show()
                    else ->
                        onEdit(rate, newName, newUnit, newPrice)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun getItemCount() = rates.size

    fun updateList(newRates: List<MaterialRate>) {
        rates = newRates
        notifyDataSetChanged()
    }
}