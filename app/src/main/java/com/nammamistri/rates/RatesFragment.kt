package com.nammamistri.rates

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nammamistri.R
import com.nammamistri.database.AppDatabase
import com.nammamistri.database.MaterialRate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RatesFragment : Fragment() {

    private lateinit var etName:       EditText
    private lateinit var etUnit:       EditText
    private lateinit var etPrice:      EditText
    private lateinit var btnAdd:       Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter:      RatesAdapter
    private lateinit var db:           AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_rates, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db      = AppDatabase.getDatabase(requireContext())
        etName  = view.findViewById(R.id.et_material_name)
        etUnit  = view.findViewById(R.id.et_unit)
        etPrice = view.findViewById(R.id.et_price)
        btnAdd  = view.findViewById(R.id.btn_add_rate)
        recyclerView = view.findViewById(R.id.rv_rates)

        // ── Setup adapter with Edit + Delete callbacks ─────────
        adapter = RatesAdapter(
            rates    = emptyList(),
            onEdit   = { rate, newName, newUnit, newPrice ->
                // Save the edited rate back to database
                lifecycleScope.launch {
                    db.materialRateDao().updateRate(
                        rate.copy(
                            materialName = newName,
                            unit         = newUnit,
                            pricePerUnit = newPrice
                        )
                    )
                }
                Toast.makeText(requireContext(), "$newName updated!", Toast.LENGTH_SHORT).show()
            },
            onDelete = { rate ->
                lifecycleScope.launch {
                    db.materialRateDao().deleteRate(rate)
                }
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter       = adapter

        // ── Observe DB — list refreshes automatically ──────────
        db.materialRateDao().getAllRates().observe(viewLifecycleOwner) { rates ->
            adapter.updateList(rates)
        }

        // ── Add default rates ONCE — only if table is empty ───
        //
        //    FIX: Previously this was inside observe(), which fires
        //    every time the DB changes — causing infinite reset.
        //    Now we check count() once at startup using a coroutine.
        //
        lifecycleScope.launch {
            val count = withContext(Dispatchers.IO) {
                db.materialRateDao().getCount()
            }
            if (count == 0) {
                addDefaultRates()
            }
        }

        // ── Add Rate button ────────────────────────────────────
        btnAdd.setOnClickListener {
            addRateFromInput()
        }
    }

    // ── Read fields, validate, then save to DB ────────────────
    private fun addRateFromInput() {
        val name  = etName.text.toString().trim()
        val unit  = etUnit.text.toString().trim()
        val price = etPrice.text.toString().trim().toDoubleOrNull()

        // Validation
        when {
            name.isEmpty() -> {
                etName.error = "Enter material name"
                etName.requestFocus()
                return
            }
            unit.isEmpty() -> {
                etUnit.error = "Enter unit (e.g. bag, load)"
                etUnit.requestFocus()
                return
            }
            price == null || price <= 0 -> {
                etPrice.error = "Enter a valid price"
                etPrice.requestFocus()
                return
            }
        }

        // Clear errors
        etName.error  = null
        etUnit.error  = null
        etPrice.error = null

        lifecycleScope.launch {
            db.materialRateDao().insertRate(
                MaterialRate(materialName = name, unit = unit, pricePerUnit = price!!)
            )
        }

        // Clear inputs after saving
        etName.text.clear()
        etUnit.text.clear()
        etPrice.text.clear()
        etName.requestFocus()

        Toast.makeText(requireContext(), "$name added!", Toast.LENGTH_SHORT).show()
    }

    // ── Default rates — added only when DB is empty ───────────
    //
    //    FIX: This now runs ONCE via getCount() check above.
    //    It will NEVER run again after rates are saved.
    //
    private fun addDefaultRates() {
        lifecycleScope.launch {
            listOf(
                MaterialRate(
                    materialName = "Cement (ಸಿಮೆಂಟ್)",
                    unit         = "bag (50 kg)",
                    pricePerUnit = 380.0
                ),
                MaterialRate(
                    materialName = "Bricks (ಇಟ್ಟಿಗೆ)",
                    unit         = "1000 nos",
                    pricePerUnit = 7000.0
                ),
                MaterialRate(
                    materialName = "Sand (ಮರಳು)",
                    unit         = "load (2 m³)",
                    pricePerUnit = 2500.0
                ),
                MaterialRate(
                    materialName = "Steel (ಕಬ್ಬಿಣ)",
                    unit         = "kg",
                    pricePerUnit = 75.0
                ),
                MaterialRate(
                    materialName = "Aggregate (ಜಲ್ಲಿ)",
                    unit         = "load",
                    pricePerUnit = 1800.0
                )
            ).forEach { db.materialRateDao().insertRate(it) }
        }
    }
}