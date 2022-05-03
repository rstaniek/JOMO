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
import dk.mths.jomo.service.DaltonizerService
import dk.mths.jomo.service.FireLog
import dk.mths.jomo.service.IJomoTrigger
import java.time.Instant
import java.time.format.DateTimeFormatter

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val jomoTrigger: IJomoTrigger = DaltonizerService(requireActivity().contentResolver)

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
                jomoTrigger.enable()
            } else {
                jomoTrigger.disable()
            }
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}