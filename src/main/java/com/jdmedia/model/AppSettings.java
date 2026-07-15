package com.jdmedia.model;
import com.jdmedia.util.BundledToolLocator;
public final class AppSettings { public String ffmpegPath=BundledToolLocator.ffmpeg(); public String ffprobePath=BundledToolLocator.ffprobe(); public String moviesFolder=""; public String seriesFolder=""; public String moviesOutputFolder=""; public String seriesOutputFolder=""; public boolean onboardingComplete=false; }
