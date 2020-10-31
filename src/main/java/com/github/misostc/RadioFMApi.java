package com.github.misostc;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;

class RadioFMApi {

    public Set<RadioFMTrack> getTracks(int pageCount) {
        Set<RadioFMTrack> tracks = new TreeSet<>();
        for (int page = 1; page <= pageCount; page++) {
            try {
                Document doc = Jsoup.connect("https://fm.rtvs.sk/playlist?page=" + page).get();
                Elements playlistTableRows = doc.select("tbody>tr.table--playlist__list");
                for (Element row : playlistTableRows) {
                    Element artist = row.select("td.artist").get(0);
                    Element title = row.select("td.title").get(0);

                    RadioFMTrack track = new RadioFMTrack();
                    track.setArtist(artist.ownText());
                    track.setTitle(title.ownText());

                    tracks.add(track);
                }
            } catch (IOException e) {
                // ignore
            }
        }
        return tracks;
    }
}
