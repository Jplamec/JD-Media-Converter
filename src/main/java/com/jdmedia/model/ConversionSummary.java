package com.jdmedia.model;
public record ConversionSummary(int totalFiles,int completedFiles,long originalBytes,long finalBytes,long elapsedSeconds,boolean cancelled) { }
