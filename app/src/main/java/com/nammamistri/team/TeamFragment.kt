package com.nammamistri.team

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
import com.nammamistri.database.Worker
import kotlinx.coroutines.launch

class TeamFragment : Fragment() {

    private lateinit var etName: EditText
    private lateinit var etWage: EditText
    private lateinit var btnAdd: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: WorkerAdapter
    private lateinit var db: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_team, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup
        db = AppDatabase.getDatabase(requireContext())
        etName = view.findViewById(R.id.et_worker_name)
        etWage = view.findViewById(R.id.et_daily_wage)
        btnAdd = view.findViewById(R.id.btn_add_worker)
        recyclerView = view.findViewById(R.id.rv_workers)

        // Setup RecyclerView
        adapter = WorkerAdapter(
            workers = emptyList(),
            onAddDay = { worker ->
                // Add 1 day to this worker
                lifecycleScope.launch {
                    db.workerDao().updateWorker(
                        worker.copy(totalDays = worker.totalDays + 1)
                    )
                }
            },
            onAddAdvance = { worker, amount ->
                lifecycleScope.launch {
                    db.workerDao().updateWorker(
                        worker.copy(totalAdvance = worker.totalAdvance + amount)
                    )
                }
            },
            onDelete = { worker ->
                lifecycleScope.launch {
                    db.workerDao().deleteWorker(worker)
                }
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Observe database changes — list auto-refreshes!
        db.workerDao().getAllWorkers().observe(viewLifecycleOwner) { workers ->
            adapter.updateList(workers)
        }

        // Add worker button
        btnAdd.setOnClickListener {
            val name = etName.text.toString().trim()
            val wageText = etWage.text.toString().trim()

            if (name.isEmpty() || wageText.isEmpty()) {
                Toast.makeText(requireContext(), "Enter name and wage!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val wage = wageText.toDoubleOrNull()
            if (wage == null || wage <= 0) {
                Toast.makeText(requireContext(), "Enter a valid wage!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                db.workerDao().insertWorker(Worker(name = name, dailyWage = wage))
            }

            // Clear inputs
            etName.text.clear()
            etWage.text.clear()
            Toast.makeText(requireContext(), "$name added to team!", Toast.LENGTH_SHORT).show()
        }
    }
}