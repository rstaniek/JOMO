package dk.mths.jomo.ui.dashboard

import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.switchmaterial.SwitchMaterial
import dk.mths.jomo.databinding.FragmentDashboardBinding

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
                writeSetting("accessibility_display_daltonizer_enabled", "1")
                writeSetting("accessibility_display_daltonizer", "0")
            } else {
                writeSetting("accessibility_display_daltonizer_enabled", "0")
                writeSetting("accessibility_display_daltonizer", "-1")
            }
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun writeSetting(name: String, value: String){
        Settings.Secure.putString(requireActivity().contentResolver, name, value)
    }
}