package com.jdmedia.model;
import java.time.Instant;
public record ConversionRecord(Instant startedAt,String source,String output,long originalBytes,long finalBytes,long elapsedSeconds,String configuration,String result) { }
