package com.jdmedia.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

/** Checks GitHub Releases without downloading or installing anything automatically. */
public final class UpdateService {
    private static final String CURRENT_VERSION = "1.0.0";
    private static final URI LATEST_RELEASE = URI.create("https://api.github.com/repos/Jplamec/jd-media-converter/releases/latest");
    public Optional<Release> latestRelease() {
        try {
            HttpRequest request=HttpRequest.newBuilder(LATEST_RELEASE).header("Accept","application/vnd.github+json").timeout(Duration.ofSeconds(8)).GET().build();
            String body=HttpClient.newHttpClient().send(request,HttpResponse.BodyHandlers.ofString()).body();
            JsonObject json=JsonParser.parseString(body).getAsJsonObject(); String tag=json.get("tag_name").getAsString().replaceFirst("^[vV]","");
            return isNewer(tag,CURRENT_VERSION)?Optional.of(new Release(tag,json.get("html_url").getAsString())):Optional.empty();
        } catch(Exception ignored) { return Optional.empty(); }
    }
    public String currentVersion(){return CURRENT_VERSION;}
    private boolean isNewer(String candidate,String current){String[] a=candidate.split("\\."),b=current.split("\\.");for(int i=0;i<Math.max(a.length,b.length);i++){int x=i<a.length?Integer.parseInt(a[i].replaceAll("\\D.*","")):0,y=i<b.length?Integer.parseInt(b[i].replaceAll("\\D.*","")):0;if(x!=y)return x>y;}return false;}
    public record Release(String version,String url){}
}
