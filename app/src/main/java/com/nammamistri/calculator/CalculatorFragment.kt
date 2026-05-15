package com.nammamistri.calculator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.nammamistri.R
import com.nammamistri.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.ceil

// ═══════════════════════════════════════════════════════════════
//  DATA CLASS: BrickConfig
// ═══════════════════════════════════════════════════════════════
data class BrickConfig(
    val label: String,
    val bricksPerM3: Double,
    val cementRatio: Double,
    val sandRatio: Double
)

// ═══════════════════════════════════════════════════════════════
//  OBJECT: BrickTypes
// ═══════════════════════════════════════════════════════════════
object BrickTypes {
    val NORMAL = BrickConfig(
        label       = "Normal Brick",
        bricksPerM3 = 500.0,
        cementRatio = 100.0,
        sandRatio   = 0.3
    )
    val HOLLOW = BrickConfig(
        label       = "Hollow Brick",
        bricksPerM3 = 65.0,
        cementRatio = 150.0,
        sandRatio   = 0.25
    )
}

// ═══════════════════════════════════════════════════════════════
//  OBJECT: UnitConverter
// ═══════════════════════════════════════════════════════════════
object UnitConverter {

    fun toMeters(value: Double, unit: String): Double = when (unit) {
        "m"    -> value
        "cm"   -> value / 100.0
        "mm"   -> value / 1000.0
        "ft"   -> value * 0.3048
        "inch" -> value * 0.0254
        else   -> value
    }

    fun volumeFromM3(m3: Double, unit: String): Double = when (unit) {
        "m³"  -> m3
        "ft³" -> m3 * 35.3147
        else  -> m3
    }

    fun sandFromM3(sandM3: Double, unit: String): Double = when (unit) {
        "m³"    -> sandM3
        "loads" -> sandM3 / 2.0
        else    -> sandM3
    }
}

// ═══════════════════════════════════════════════════════════════
//  DATA CLASS: CalculationResult
// ═══════════════════════════════════════════════════════════════
data class CalculationResult(
    val lengthM: Double,
    val heightM: Double,
    val thicknessM: Double,
    val volumeM3: Double,
    val bricks: Int,
    val cementBags: Int,
    val sandM3: Double,
    val config: BrickConfig
)

// ═══════════════════════════════════════════════════════════════
//  FRAGMENT: CalculatorFragment
// ═══════════════════════════════════════════════════════════════
class CalculatorFragment : Fragment() {

    // ── Inputs ────────────────────────────────────────────────
    private lateinit var etLength: EditText
    private lateinit var etHeight: EditText
    private lateinit var etThickness: EditText
    private lateinit var spinnerLength: Spinner
    private lateinit var spinnerHeight: Spinner
    private lateinit var spinnerThickness: Spinner

    // ── Brick type buttons ────────────────────────────────────
    private lateinit var btnNormalBrick: LinearLayout
    private lateinit var btnHollowBrick: LinearLayout
    private lateinit var tvNormalLabel: TextView
    private lateinit var tvHollowLabel: TextView

    // ── Output preference radios ──────────────────────────────
    private lateinit var rgVolumeUnit: RadioGroup
    private lateinit var rgSandUnit: RadioGroup

    // ── Action buttons ────────────────────────────────────────
    private lateinit var btnCalculate: Button
    private lateinit var btnClear: Button

    // ── Summary card ──────────────────────────────────────────
    private lateinit var cardSummary: CardView
    private lateinit var tvWallArea: TextView
    private lateinit var tvAreaUnit: TextView
    private lateinit var tvWallVolume: TextView
    private lateinit var tvVolumeUnitLabel: TextView

    // ── Materials card ────────────────────────────────────────
    private lateinit var cardResults: CardView
    private lateinit var tvBrickTypeBadge: TextView
    private lateinit var tvBricks: TextView
    private lateinit var tvBricksSub: TextView
    private lateinit var tvCement: TextView
    private lateinit var tvCementSub: TextView
    private lateinit var tvSand: TextView
    private lateinit var tvSandSub: TextView
    private lateinit var tvSandUnitLabel: TextView
    private lateinit var tvFormulaBreakdown: TextView

    // ── Cost card ─────────────────────────────────────────────
    private lateinit var cardCost: CardView
    private lateinit var tvBrickCost: TextView
    private lateinit var tvCementCost: TextView
    private lateinit var tvSandCost: TextView
    private lateinit var tvTotalCost: TextView

    // ── State ─────────────────────────────────────────────────
    private var selectedConfig: BrickConfig = BrickTypes.NORMAL
    private lateinit var db: AppDatabase
    private val INPUT_UNITS = listOf("ft", "m", "cm", "mm", "inch")

    // ──────────────────────────────────────────────────────────
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_calculator, container, false)

    // ──────────────────────────────────────────────────────────
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = AppDatabase.getDatabase(requireContext())
        bindViews(view)
        setupSpinners()
        setupBrickTypeButtons()
        btnCalculate.setOnClickListener { onCalculateClicked() }
        btnClear.setOnClickListener     { onClearClicked() }
    }

    // ──────────────────────────────────────────────────────────
    private fun bindViews(v: View) {
        etLength          = v.findViewById(R.id.et_length)
        etHeight          = v.findViewById(R.id.et_height)
        etThickness       = v.findViewById(R.id.et_thickness)
        spinnerLength     = v.findViewById(R.id.spinner_length_unit)
        spinnerHeight     = v.findViewById(R.id.spinner_height_unit)
        spinnerThickness  = v.findViewById(R.id.spinner_thickness_unit)
        btnNormalBrick    = v.findViewById(R.id.btn_normal_brick)
        btnHollowBrick    = v.findViewById(R.id.btn_hollow_brick)
        tvNormalLabel     = v.findViewById(R.id.tv_normal_label)
        tvHollowLabel     = v.findViewById(R.id.tv_hollow_label)
        rgVolumeUnit      = v.findViewById(R.id.rg_volume_unit)
        rgSandUnit        = v.findViewById(R.id.rg_sand_unit)
        btnCalculate      = v.findViewById(R.id.btn_calculate)
        btnClear          = v.findViewById(R.id.btn_clear)
        cardSummary       = v.findViewById(R.id.card_summary)
        tvWallArea        = v.findViewById(R.id.tv_wall_area)
        tvAreaUnit        = v.findViewById(R.id.tv_area_unit)
        tvWallVolume      = v.findViewById(R.id.tv_wall_volume)
        tvVolumeUnitLabel = v.findViewById(R.id.tv_volume_unit_label)
        cardResults       = v.findViewById(R.id.card_results)
        tvBrickTypeBadge  = v.findViewById(R.id.tv_brick_type_badge)
        tvBricks          = v.findViewById(R.id.tv_bricks)
        tvBricksSub       = v.findViewById(R.id.tv_bricks_sub)
        tvCement          = v.findViewById(R.id.tv_cement)
        tvCementSub       = v.findViewById(R.id.tv_cement_sub)
        tvSand            = v.findViewById(R.id.tv_sand)
        tvSandSub         = v.findViewById(R.id.tv_sand_sub)
        tvSandUnitLabel   = v.findViewById(R.id.tv_sand_unit_label)
        tvFormulaBreakdown = v.findViewById(R.id.tv_formula_breakdown)
        cardCost          = v.findViewById(R.id.card_cost)
        tvBrickCost       = v.findViewById(R.id.tv_brick_cost)
        tvCementCost      = v.findViewById(R.id.tv_cement_cost)
        tvSandCost        = v.findViewById(R.id.tv_sand_cost)
        tvTotalCost       = v.findViewById(R.id.tv_total_cost)
    }

    // ──────────────────────────────────────────────────────────
    private fun setupSpinners() {
        fun makeAdapter() = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            INPUT_UNITS
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        spinnerLength.adapter    = makeAdapter()
        spinnerHeight.adapter    = makeAdapter()
        spinnerThickness.adapter = makeAdapter()
        spinnerLength.setSelection(0)
        spinnerHeight.setSelection(0)
        spinnerThickness.setSelection(INPUT_UNITS.indexOf("inch"))
    }

    // ──────────────────────────────────────────────────────────
    private fun setupBrickTypeButtons() {
        btnNormalBrick.setOnClickListener { selectBrickType(BrickTypes.NORMAL) }
        btnHollowBrick.setOnClickListener { selectBrickType(BrickTypes.HOLLOW) }
        selectBrickType(BrickTypes.NORMAL)
    }

    // ──────────────────────────────────────────────────────────
    //  FIX: Read colors from resources, not hardcoded hex.
    //  In light mode: button_text = #FFFFFF, primary = #E65100
    //  In dark mode:  button_text = #FFFFFF, primary = #FF7043
    //  Android picks the right value automatically.
    // ──────────────────────────────────────────────────────────
    private fun selectBrickType(config: BrickConfig) {
        selectedConfig = config

        // Read theme-aware colors from resources
        val colorSelected   = ContextCompat.getColor(requireContext(), R.color.button_text)
        val colorUnselected = ContextCompat.getColor(requireContext(), R.color.primary)

        if (config == BrickTypes.NORMAL) {
            btnNormalBrick.setBackgroundResource(R.drawable.brick_type_selected)
            btnHollowBrick.setBackgroundResource(R.drawable.brick_type_unselected)
            tvNormalLabel.setTextColor(colorSelected)
            tvHollowLabel.setTextColor(colorUnselected)
        } else {
            btnNormalBrick.setBackgroundResource(R.drawable.brick_type_unselected)
            btnHollowBrick.setBackgroundResource(R.drawable.brick_type_selected)
            tvNormalLabel.setTextColor(colorUnselected)
            tvHollowLabel.setTextColor(colorSelected)
        }
    }

    // ──────────────────────────────────────────────────────────
    private fun getVolumeUnit(): String =
        if (rgVolumeUnit.checkedRadioButtonId == R.id.rb_m3) "m³" else "ft³"

    private fun getSandUnit(): String =
        if (rgSandUnit.checkedRadioButtonId == R.id.rb_sand_loads) "loads" else "m³"

    // ══════════════════════════════════════════════════════════
    //  MAIN CALCULATE FLOW
    // ══════════════════════════════════════════════════════════
    private fun onCalculateClicked() {
        val rawLength    = etLength.text.toString().trim()
        val rawHeight    = etHeight.text.toString().trim()
        val rawThickness = etThickness.text.toString().trim()

        if (!validateInputs(rawLength, rawHeight, rawThickness)) return

        val lengthM    = UnitConverter.toMeters(rawLength.toDouble(),    spinnerLength.selectedItem.toString())
        val heightM    = UnitConverter.toMeters(rawHeight.toDouble(),    spinnerHeight.selectedItem.toString())
        val thicknessM = UnitConverter.toMeters(rawThickness.toDouble(), spinnerThickness.selectedItem.toString())

        val result = calculate(lengthM, heightM, thicknessM, selectedConfig)

        displaySummary(result)
        displayMaterials(result)
        loadRatesAndShowCost(result)
    }

    // ══════════════════════════════════════════════════════════
    //  VALIDATION
    // ══════════════════════════════════════════════════════════
    private fun validateInputs(length: String, height: String, thickness: String): Boolean {
        if (length.isEmpty())    { etLength.error    = "Please enter length";    etLength.requestFocus();    return false }
        if (height.isEmpty())    { etHeight.error    = "Please enter height";    etHeight.requestFocus();    return false }
        if (thickness.isEmpty()) { etThickness.error = "Please enter thickness"; etThickness.requestFocus(); return false }

        val l = length.toDoubleOrNull()
        val h = height.toDoubleOrNull()
        val t = thickness.toDoubleOrNull()

        if (l == null || l <= 0) { etLength.error    = "Enter a valid positive number"; etLength.requestFocus();    return false }
        if (h == null || h <= 0) { etHeight.error    = "Enter a valid positive number"; etHeight.requestFocus();    return false }
        if (t == null || t <= 0) { etThickness.error = "Enter a valid positive number"; etThickness.requestFocus(); return false }

        val lM = UnitConverter.toMeters(l, spinnerLength.selectedItem.toString())
        val hM = UnitConverter.toMeters(h, spinnerHeight.selectedItem.toString())
        val tM = UnitConverter.toMeters(t, spinnerThickness.selectedItem.toString())

        if (lM > 500)  { etLength.error    = "Length too large (max ~500 m)";   return false }
        if (hM > 100)  { etHeight.error    = "Height too large (max ~100 m)";   return false }
        if (tM > 2.0)  { etThickness.error = "Thickness too large (max ~2 m)";  return false }
        if (tM < 0.05) { etThickness.error = "Thickness too small (min ~5 cm)"; return false }

        etLength.error = null; etHeight.error = null; etThickness.error = null
        return true
    }

    // ══════════════════════════════════════════════════════════
    //  CORE CALCULATION ENGINE — all inputs MUST be in meters
    // ══════════════════════════════════════════════════════════
    private fun calculate(
        lengthM: Double, heightM: Double,
        thicknessM: Double, config: BrickConfig
    ): CalculationResult {
        val volumeM3   = lengthM * heightM * thicknessM
        val bricks     = ceil(volumeM3 * config.bricksPerM3).toInt()
        val cementBags = ceil(bricks.toDouble() / config.cementRatio).toInt()
        val sandM3     = cementBags.toDouble() * config.sandRatio
        return CalculationResult(lengthM, heightM, thicknessM, volumeM3, bricks, cementBags, sandM3, config)
    }

    // ══════════════════════════════════════════════════════════
    //  DISPLAY FUNCTIONS
    // ══════════════════════════════════════════════════════════
    private fun displaySummary(r: CalculationResult) {
        val volUnit    = getVolumeUnit()
        val displayVol = UnitConverter.volumeFromM3(r.volumeM3, volUnit)
        val areaM2     = r.lengthM * r.heightM

        tvWallArea.text        = "%.2f".format(areaM2)
        tvAreaUnit.text        = "m²"
        tvWallVolume.text      = "%.3f".format(displayVol)
        tvVolumeUnitLabel.text = volUnit
        cardSummary.visibility = View.VISIBLE
    }

    private fun displayMaterials(r: CalculationResult) {
        val sandUnit   = getSandUnit()
        val sandOutput = UnitConverter.sandFromM3(r.sandM3, sandUnit)
        val config     = r.config

        tvBrickTypeBadge.text = config.label
        tvBricks.text         = "%,d".format(r.bricks)
        tvBricksSub.text      = "${"%.3f".format(r.volumeM3)} m³ × ${config.bricksPerM3.toInt()}"
        tvCement.text         = "${r.cementBags}"
        tvCementSub.text      = "${r.bricks} ÷ ${config.cementRatio.toInt()} = ${r.cementBags} bags"

        val sandFormatted  = if (sandUnit == "loads") "%.1f".format(sandOutput) else "%.3f".format(sandOutput)
        tvSand.text          = sandFormatted
        tvSandSub.text       = "${r.cementBags} × ${config.sandRatio} = ${"%.3f".format(r.sandM3)} m³"
        tvSandUnitLabel.text = sandUnit

        tvFormulaBreakdown.text = buildBreakdown(r, sandUnit, sandOutput)
        cardResults.visibility  = View.VISIBLE
    }

    private fun buildBreakdown(r: CalculationResult, sandUnit: String, sandOutput: Double): String =
        buildString {
            val c = r.config
            appendLine("Brick type    : ${c.label}")
            appendLine("Density       : ${c.bricksPerM3.toInt()} bricks/m³")
            appendLine("Cement ratio  : 1 bag per ${c.cementRatio.toInt()} bricks")
            appendLine("Sand ratio    : ${c.sandRatio} m³ per bag")
            appendLine("──────────────────────────────────")
            appendLine("Length        : ${"%.4f".format(r.lengthM)} m")
            appendLine("Height        : ${"%.4f".format(r.heightM)} m")
            appendLine("Thickness     : ${"%.4f".format(r.thicknessM)} m")
            appendLine("──────────────────────────────────")
            appendLine("Volume (m³)   : ${"%.4f".format(r.volumeM3)}")
            appendLine("Bricks        : ${r.bricks}")
            appendLine("Cement bags   : ${r.cementBags}")
            appendLine("Sand (m³)     : ${"%.3f".format(r.sandM3)} m³")
            if (sandUnit == "loads") append("Sand (loads)  : ${"%.1f".format(sandOutput)}")
        }

    private fun loadRatesAndShowCost(r: CalculationResult) {
        lifecycleScope.launch {
            val rates = withContext(Dispatchers.IO) { db.materialRateDao().getAllRatesOnce() }
            if (rates.isEmpty()) { cardCost.visibility = View.GONE; return@launch }

            val brickRate  = rates.firstOrNull { it.materialName.contains("brick",  ignoreCase = true) || it.materialName.contains("ಇಟ್ಟಿಗೆ") }
            val cementRate = rates.firstOrNull { it.materialName.contains("cement", ignoreCase = true) || it.materialName.contains("ಸಿಮೆಂಟ್") }
            val sandRate   = rates.firstOrNull { it.materialName.contains("sand",   ignoreCase = true) || it.materialName.contains("ಮರಳು") }

            val sandLoads  = ceil(r.sandM3 / 2.0).toInt()
            val brickCost  = brickRate?.let  { (r.bricks / 1000.0) * it.pricePerUnit }
            val cementCost = cementRate?.let { r.cementBags          * it.pricePerUnit }
            val sandCost   = sandRate?.let   { sandLoads              * it.pricePerUnit }

            if (brickCost == null && cementCost == null && sandCost == null) {
                cardCost.visibility = View.GONE; return@launch
            }

            tvBrickCost.text  = brickCost  ?.let { "₹ ${"%.0f".format(it)}" } ?: "₹ — (add rate)"
            tvCementCost.text = cementCost ?.let { "₹ ${"%.0f".format(it)}" } ?: "₹ — (add rate)"
            tvSandCost.text   = sandCost   ?.let { "₹ ${"%.0f".format(it)}" } ?: "₹ — (add rate)"

            val total = (brickCost ?: 0.0) + (cementCost ?: 0.0) + (sandCost ?: 0.0)
            tvTotalCost.text  = "₹ ${"%.0f".format(total)}"
            cardCost.visibility = View.VISIBLE
        }
    }

    // ══════════════════════════════════════════════════════════
    //  CLEAR
    // ══════════════════════════════════════════════════════════
    private fun onClearClicked() {
        etLength.text.clear(); etHeight.text.clear(); etThickness.text.clear()
        etLength.error = null;  etHeight.error = null; etThickness.error = null
        spinnerLength.setSelection(0)
        spinnerHeight.setSelection(0)
        spinnerThickness.setSelection(INPUT_UNITS.indexOf("inch"))
        selectBrickType(BrickTypes.NORMAL)
        cardSummary.visibility = View.GONE
        cardResults.visibility = View.GONE
        cardCost.visibility    = View.GONE
        etLength.requestFocus()
    }
}