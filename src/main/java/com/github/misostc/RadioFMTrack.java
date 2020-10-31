package com.github.misostc;

import java.util.Comparator;
import java.util.Objects;

class RadioFMTrack implements Comparable<RadioFMTrack> {

    public static final String SYMBOLS_REGEX = "[\\p{Punct}\\p{Sm}]";

    private static final Comparator<RadioFMTrack> NATURAL_ORDER_COMPARATOR = Comparator
            .comparing(RadioFMTrack::getArtist)
            .thenComparing(RadioFMTrack::getTitle);

    private String artist;
    private String title;

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String toSimpleString() {
        return String.format("%s %s",
                getArtist().replaceAll(SYMBOLS_REGEX, ""),
                getTitle().replaceAll(SYMBOLS_REGEX, ""))
                .toLowerCase();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RadioFMTrack fmTrack = (RadioFMTrack) o;
        return Objects.equals(artist, fmTrack.artist) &&
                Objects.equals(title, fmTrack.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(artist, title);
    }

    @Override
    public int compareTo(RadioFMTrack o) {
        return NATURAL_ORDER_COMPARATOR.compare(this, o);
    }
}
