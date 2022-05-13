package dk.mths.jomo.ui.home

import android.Manifest
import android.app.AppOpsManager
import android.app.AppOpsManager.MODE_ALLOWED
import android.app.AppOpsManager.OPSTR_GET_USAGE_STATS
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import dk.mths.jomo.R
import dk.mths.jomo.databinding.FragmentHomeBinding
import dk.mths.jomo.service.UsageStatsService
import dk.mths.jomo.utils.App
import dk.mths.jomo.utils.AppsAdapter
import java.util.*
import java.util.concurrent.TimeUnit

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    lateinit var enableBtn: Button
    lateinit var showBtn: Button
    lateinit var permissionDescriptionTv: TextView
    lateinit var usageTv: TextView
    lateinit var appsList: ListView
    lateinit var todayBtn: Button
    lateinit var lastWeekBtn: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        enableBtn = binding.enableBtn
        showBtn = binding.showBtn
        permissionDescriptionTv = binding.permissionDescriptionTv
        usageTv = binding.usageTv
        appsList = binding.appsList
        todayBtn = binding.todayButton
        lastWeekBtn = binding.lastWeekButton

        if (checkUsageStatsPermission()) {
            showHideWithPermission()
            showBtn.setOnClickListener { view: View? -> showUsageStats(1) }
        } else {
            showHideNoPermission()
            enableBtn.setOnClickListener { view: View? ->
                startActivity(
                    Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                )
            }
        }

        todayBtn.setOnClickListener { view: View? -> showUsageStats(1) }
        lastWeekBtn.setOnClickListener { view: View? -> showUsageStats(7) }

        return root
    }

    private fun showUsageStats(historyInDays: Int) {
        val usageService = UsageStatsService(requireContext())
        val appsList = usageService.showUsageStats(historyInDays)
        // build the adapter
        val adapter = AppsAdapter(requireContext(), appsList)

        // attach the adapter to a ListView
        val listView: ListView = binding.appsList
        listView.adapter = adapter

        showHideItemsWhenShowApps(historyInDays)
    }

    /**
     * helper method used to show/hide items in the view when  PACKAGE_USAGE_STATS permission is not allowed
     */
    private fun showHideNoPermission() {
        enableBtn.visibility = View.VISIBLE
        permissionDescriptionTv.visibility = View.VISIBLE
        showBtn.visibility = View.GONE
        todayBtn.visibility = View.GONE
        lastWeekBtn.visibility = View.GONE
        usageTv.visibility = View.GONE
        appsList.visibility = View.GONE
    }

    /**
     * helper method used to show/hide items in the view when  PACKAGE_USAGE_STATS permission allowed
     */
    private fun showHideWithPermission() {
        enableBtn.visibility = View.GONE
        permissionDescriptionTv.visibility = View.GONE
        showBtn.visibility = View.VISIBLE
        todayBtn.visibility = View.GONE
        lastWeekBtn.visibility = View.GONE
        usageTv.visibility = View.GONE
        appsList.visibility = View.GONE
    }

    /**
     * helper method used to show/hide items in the view when showing the apps list
     */
    private fun showHideItemsWhenShowApps(days: Int) {
        enableBtn.visibility = View.GONE
        permissionDescriptionTv.visibility = View.GONE
        showBtn.visibility = View.GONE
        todayBtn.visibility = View.VISIBLE
        lastWeekBtn.visibility = View.VISIBLE
        usageTv.visibility = View.VISIBLE
        usageTv.text = "Your app usage for the last $days days"
        appsList.visibility = View.VISIBLE
    }

    private fun checkUsageStatsPermission(): Boolean {
        val appOps = requireActivity()
            .getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode: Int = appOps.checkOpNoThrow(
            OPSTR_GET_USAGE_STATS,
            Process.myUid(), requireActivity().getPackageName()
        )
        return if (mode == AppOpsManager.MODE_DEFAULT) {
            requireActivity().checkCallingOrSelfPermission(Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED
        } else {
            mode == MODE_ALLOWED
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}