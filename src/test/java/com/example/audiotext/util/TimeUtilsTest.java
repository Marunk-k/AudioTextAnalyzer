package com.example.audiotext.util;
import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class TimeUtilsTest { @Test void srt(){assertEquals("00:00:01,500",TimeUtils.secondsToSrtTime(1.5));} @Test void human(){assertEquals("01:01:01",TimeUtils.secondsToHumanReadableTime(3661));} }
