package dk.mths.jomo.ui.dashboard

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.work.WorkManager
import com.google.android.material.switchmaterial.SwitchMaterial
import dk.mths.jomo.databinding.FragmentDashboardBinding
import dk.mths.jomo.service.DimmerService
import dk.mths.jomo.service.DaltonizerService
import dk.mths.jomo.service.FireLog
import dk.mths.jomo.service.IJomoTrigger


class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var daltonizerService: IJomoTrigger
    private lateinit var dimmerService : IJomoTrigger
    lateinit var mWorkManager : WorkManager

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        changeWriteSettingsPermission()
        daltonizerService = DaltonizerService(requireContext().contentResolver)
        dimmerService = DimmerService(requireContext().contentResolver)
        mWorkManager = WorkManager.getInstance(requireContext())

        val switchControl: SwitchMaterial = binding.switch1
        if(daltonizerService.isEnabled())
            switchControl.isChecked = true

        switchControl.setOnClickListener {
            if (switchControl.isChecked) {
                daltonizerService.enable(FireLog.JOMO)
            } else {
                daltonizerService.disable()
            }
        }


        val switchControl2: SwitchMaterial = binding.switch2
        if(dimmerService.isEnabled())
            switchControl2.isChecked = true

        switchControl2.setOnClickListener {
            if (switchControl2.isChecked) {
                dimmerService.enable(FireLog.JOMO)
            } else {
                dimmerService.disable()
            }
        }

        return root
    }

    private fun changeWriteSettingsPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            val canWriteSettings = Settings.System.canWrite(requireContext())
            if (!canWriteSettings) {
                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                startActivity(intent)
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}