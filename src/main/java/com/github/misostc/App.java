package com.github.misostc;

import org.apache.commons.text.similarity.LevenshteinDistance;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class App {

	public static final LevenshteinDistance levenshtein = LevenshteinDistance.getDefaultInstance();
	public static final String CLIENT_ID = System.getenv("CLIENT_ID");
	public static final String CLIENT_SECRET = System.getenv("CLIENT_SECRET");
	public static final String REFRESH_TOKEN = System.getenv("REFRESH_TOKEN");
	public static final String PLAYLIST_ID = System.getenv("PLAYLIST_ID");

	public static void main(String[] args) {
		RadioFMApi radioFMApi = new RadioFMApi();
		Set<RadioFMTrack> tracksFromFM = radioFMApi.getTracks(10);

		com.github.misostc.SpotifyApi spotifyApi = new SpotifyApi(CLIENT_ID, CLIENT_SECRET, REFRESH_TOKEN);
		List<SpotifyTrack> resultingPlaylist = new ArrayList<>();
		for (RadioFMTrack fmTrack : tracksFromFM) {
			System.out.println("Locating: " + fmTrack.toSimpleString());
			spotifyApi.findTrackInSpotify(fmTrack).stream()
					.filter(App::durationFilter)
					.min(levenshteinSort(fmTrack))
					.ifPresent(spotifyTrack -> {
						System.out.println("Found:    " + spotifyTrack.toSimpleString());
						System.out.println("URI:      " + spotifyTrack.getSpotifyUri());
						resultingPlaylist.add(spotifyTrack);
					});
			System.out.println();
		}

		spotifyApi.updatePlaylist(PLAYLIST_ID, resultingPlaylist);

		byte[] imageJPG = new PlaylistCoverGenerator().getImageJPG(resultingPlaylist.hashCode());
		spotifyApi.updatePlaylistCover(PLAYLIST_ID, imageJPG);
	}

	private static boolean durationFilter(SpotifyTrack spotifyTrack) {
		return spotifyTrack.getDuration().compareTo(Duration.ofMinutes(1)) >= 0
				&& spotifyTrack.getDuration().compareTo(Duration.ofMinutes(7)) <= 0;
	}

	private static Comparator<SpotifyTrack> levenshteinSort(RadioFMTrack fmTrack) {
		return Comparator.comparing(spotifyTrack -> levenshtein.apply(fmTrack.toSimpleString(), spotifyTrack.toSimpleString()));
	}

}
