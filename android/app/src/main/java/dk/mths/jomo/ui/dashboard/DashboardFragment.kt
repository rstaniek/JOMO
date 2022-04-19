package dk.mths.jomo.ui.dashboard

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.switchmaterial.SwitchMaterial
import dk.mths.jomo.databinding.FragmentDashboardBinding
import dk.mths.jomo.service.FireLog
import java.time.Instant
import java.time.format.DateTimeFormatter

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
                ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val switchControl: SwitchMaterial = binding.switch1
        switchControl.setOnClickListener {
            if (switchControl.isChecked){
                handleOnGrayscaleActivated()
            } else {
                handleOnGrayscaleDeactivated()
            }
        }
        return root
    }

    private fun handleOnGrayscaleActivated() {
        writeSetting("accessibility_display_daltonizer_enabled", "1")
        writeSetting("accessibility_display_daltonizer", "0")
        FireLog().withContext("daltonizer", "true").sendLog("daltonizer")
    }

    private fun handleOnGrayscaleDeactivated() {
        writeSetting("accessibility_display_daltonizer_enabled", "0")
        writeSetting("accessibility_display_daltonizer", "-1")
        FireLog().withContext("daltonizer", "false").sendLog("daltonizer")
    }

    @SuppressLint("HardwareIds")
    private fun composeGrayscaleOpLogMsg(enabled: Boolean): HashMap<String, String> {
        return hashMapOf(
            "daltonizer" to enabled.toString(),
            "timestamp" to DateTimeFormatter.ISO_INSTANT.format(Instant.now()),
            "uid" to Settings.Secure.getString(requireActivity().contentResolver, Settings.Secure.ANDROID_ID)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun writeSetting(name: String, value: String){
        Settings.Secure.putString(requireActivity().contentResolver, name, value)
    }
}