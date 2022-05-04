package dk.mths.jomo.ui.home

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
import dk.mths.jomo.utils.App
import dk.mths.jomo.utils.AppsAdapter
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val HistoryInDays = 7
    lateinit var enableBtn: Button
    lateinit var showBtn: Button
    lateinit var permissionDescriptionTv: TextView
    lateinit var usageTv: TextView
    lateinit var appsList: ListView

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

        usageTv.text = "Your Apps Usage For Last $HistoryInDays days"

        if (checksageStatsPermission()) {
            showHideWithPermission()
            showBtn.setOnClickListener { view: View? -> showUsageStats() }
        } else {
            showHideNoPermission()
            enableBtn.setOnClickListener { view: View? ->
                startActivity(
                    Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                )
            }
        }

        return root
    }

    private fun showUsageStats() {
        var usageStatsManager: UsageStatsManager =
            activity?.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        var queryUsageStats: List<UsageStats> = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            System.currentTimeMillis() - 1000 * 3600 * 24 * HistoryInDays,
            System.currentTimeMillis()
        )
        queryUsageStats = queryUsageStats.filter { it.totalTimeInForeground > 0 }

        // Group the usageStats by application and sort them by total time in foreground
        if (queryUsageStats.size > 0) {
            val mySortedMap: MutableMap<String, UsageStats> = TreeMap()
            for (usageStats in queryUsageStats) {
                mySortedMap[usageStats.packageName] = usageStats
            }
            showAppsUsage(mySortedMap)
        }
    }

    private fun showAppsUsage(mySortedMap: Map<String, UsageStats>) {
        //public void showAppsUsage(List<UsageStats> usageStatsList) {
        var appsList: ArrayList<App> = ArrayList()
        val usageStatsList: List<UsageStats> = ArrayList(mySortedMap.values)

        // sort the applications by time spent in foreground
        Collections.sort(
            usageStatsList
        ) { z1: UsageStats, z2: UsageStats ->
            java.lang.Long.compare(
                z1.totalTimeInForeground,
                z2.totalTimeInForeground
            )
        }

        // get total time of apps usage to calculate the usagePercentage for each app
        val totalTime = usageStatsList.stream().map { obj: UsageStats -> obj.totalTimeInForeground }
            .mapToLong { obj: Long -> obj }.sum()

        val longestAppRunTime = usageStatsList.sortedWith(compareBy { it.totalTimeInForeground }).last().totalTimeInForeground

        //fill the appsList
        for (usageStats in usageStatsList) {
            try {
                val packageName = usageStats.packageName
                var icon: Drawable? = getDrawable(requireActivity(), R.drawable.no_image)
                val packageNames = packageName.split("\\.").toTypedArray()
                var appName = packageNames[packageNames.size - 1].trim { it <= ' ' }

                if (isAppInfoAvailable(usageStats)) {
                    val ai: ApplicationInfo =
                        requireActivity().getApplicationContext().getPackageManager()
                            .getApplicationInfo(packageName, 0)
                    icon = requireActivity().getApplicationContext().getPackageManager()
                        .getApplicationIcon(ai)
                    appName = requireActivity().getApplicationContext().getPackageManager()
                        .getApplicationLabel(ai)
                        .toString()
                }

                val usageDuration: String = getDurationBreakdown(usageStats.totalTimeInForeground)
                val usagePercentage = (usageStats.totalTimeInForeground * 100 / totalTime).toInt()
                val percentageOfLongestRunningApp = (usageStats.totalTimeInForeground * 100 / longestAppRunTime).toInt()

                val usageStatDTO = App(icon, appName, usagePercentage, percentageOfLongestRunningApp, usageDuration)
                appsList.add(usageStatDTO)
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace();
            }
        }

        // reverse the list to get most usage first
        appsList.reverse()

        // build the adapter
        val adapter = AppsAdapter(requireContext(), appsList)

        // attach the adapter to a ListView
        val listView: ListView = binding.appsList
        listView.adapter = adapter

        showHideItemsWhenShowApps()
    }

    /**
     * check if the application info is still existing in the device / otherwise it's not possible to show app detail
     * @return true if application info is available
     */
    private fun isAppInfoAvailable(usageStats: UsageStats): Boolean {
        return try {
            requireActivity().getApplicationContext().getPackageManager()
                .getApplicationInfo(usageStats.packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * helper method to get string in format hh:mm:ss from miliseconds
     *
     * @param millis (application time in foreground)
     * @return string in format hh:mm:ss from miliseconds
     */
    private fun getDurationBreakdown(millis: Long): String {
        require(millis >= 0) { "Duration must be greater than zero!" }

        var milliseconds = millis

        var hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
        milliseconds -= TimeUnit.HOURS.toMillis(hours)
        var minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
        milliseconds -= TimeUnit.MINUTES.toMillis(minutes)
        var seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds)

        return "$hours h $minutes m $seconds s"
    }

    /**
     * helper method used to show/hide items in the view when  PACKAGE_USAGE_STATS permission is not allowed
     */
    private fun showHideNoPermission() {
        enableBtn.visibility = View.VISIBLE
        permissionDescriptionTv.visibility = View.VISIBLE
        showBtn.visibility = View.GONE
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
        usageTv.visibility = View.GONE
        appsList.visibility = View.GONE
    }

    /**
     * helper method used to show/hide items in the view when showing the apps list
     */
    private fun showHideItemsWhenShowApps() {
        enableBtn.visibility = View.GONE
        permissionDescriptionTv.visibility = View.GONE
        showBtn.visibility = View.GONE
        usageTv.visibility = View.VISIBLE
        appsList.visibility = View.VISIBLE
    }

    private fun checksageStatsPermission(): Boolean {
        var appOpsManager: AppOpsManager? = null
        var mode: Int = 0
        appOpsManager = activity?.getSystemService(Context.APP_OPS_SERVICE)!! as AppOpsManager
        mode = appOpsManager.checkOpNoThrow(
            OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(), activity?.packageName.toString()
        );
        return mode == MODE_ALLOWED

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}