package com.github.misostc;

import java.time.Duration;
import java.util.List;

public class SpotifyTrack {
    public static final String SYMBOLS_REGEX = "[\\p{Punct}\\p{Sm}]";

    private List<String> artists;
    private String title;
    private Duration duration;
    private String spotifyUri;

    public List<String> getArtists() {
        return artists;
    }

    public void setArtists(List<String> artists) {
        this.artists = artists;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public String getSpotifyUri() {
        return spotifyUri;
    }

    public void setSpotifyUri(String spotifyUri) {
        this.spotifyUri = spotifyUri;
    }

    public String toSimpleString() {
        return String.format("%s %s",
                String.join(" ", getArtists()).replaceAll(SYMBOLS_REGEX, ""),
                getTitle().replaceAll(SYMBOLS_REGEX, ""))
                .toLowerCase();
    }
}
