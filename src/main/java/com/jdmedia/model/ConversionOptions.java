package com.jdmedia.model;
import java.nio.file.Path;import java.util.Map;
public record ConversionOptions(String preset,int crf,Map<Path,Integer> audioStreamIndexes,SubtitleMode subtitles,boolean deleteOriginals,boolean convertHdrToSdr) { public enum SubtitleMode { KEEP, REMOVE, SELECT } }
