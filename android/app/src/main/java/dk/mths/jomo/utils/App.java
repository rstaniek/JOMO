package dk.mths.jomo.utils;

import android.graphics.drawable.Drawable;

public class App {
    public  Drawable appIcon;
    public  String appName;
    public  int usagePercentage;
    public int percentageOfLongestRunning;
    public  String usageDuration;
    public int openCount;


    public App(Drawable appIcon, String appName, int usagePercentage, int percentageOfLongestRunning, String usageDuration, int openCount) {
        this.appIcon = appIcon;
        this.appName = appName;
        this.usagePercentage = usagePercentage;
        this.percentageOfLongestRunning = percentageOfLongestRunning;
        this.usageDuration = usageDuration;
        this.openCount = openCount;
    }
}