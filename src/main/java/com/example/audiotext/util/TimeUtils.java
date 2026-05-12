package com.example.audiotext.util;
public class TimeUtils {
 public static String secondsToSrtTime(double sec){ int ms=(int)Math.round(sec*1000); int h=ms/3600000; ms%=3600000; int m=ms/60000; ms%=60000; int s=ms/1000; ms%=1000; return String.format("%02d:%02d:%02d,%03d",h,m,s,ms); }
 public static String secondsToHumanReadableTime(double sec){ int t=(int)Math.round(sec); return String.format("%02d:%02d:%02d",t/3600,(t%3600)/60,t%60); }
}
