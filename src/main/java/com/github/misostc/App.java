package com.github.misostc;

import com.neovisionaries.i18n.CountryCode;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRefreshRequest;
import com.wrapper.spotify.requests.data.playlists.ReplacePlaylistsItemsRequest;
import com.wrapper.spotify.requests.data.search.simplified.SearchTracksRequest;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.apache.hc.core5.http.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

class TrackWithScore {

	private final Track track;

	private final Integer score;

	public TrackWithScore(Track track, Integer score) {
		this.track = track;
		this.score = score;
	}

	public Track getTrack() {
		return track;
	}

	public Integer getScore() {
		return score;
	}

}

public class App {

	static final SpotifyApi spotifyApi;

	public static final LevenshteinDistance levenshtein = LevenshteinDistance.getDefaultInstance();

	public static final String CLIENT_ID = System.getenv("CLIENT_ID");

	public static final String CLIENT_SECRET = System.getenv("CLIENT_SECRET");

	public static final String REFRESH_TOKEN = System.getenv("REFRESH_TOKEN");

	public static final String PLAYLIST_ID = System.getenv("PLAYLIST_ID");

	static {
		spotifyApi = new SpotifyApi.Builder().setClientId(CLIENT_ID).setClientSecret(CLIENT_SECRET)
				.setRefreshToken(REFRESH_TOKEN).build();

		final AuthorizationCodeRefreshRequest authorizationCodeRefreshRequest = spotifyApi.authorizationCodeRefresh()
				.build();
		final AuthorizationCodeCredentials clientCredentials;
		try {
			clientCredentials = authorizationCodeRefreshRequest.execute();
			spotifyApi.setAccessToken(clientCredentials.getAccessToken());
		}
		catch (Exception e) {
			System.exit(-1);
		}
	}

	public static void main(String[] args) throws IOException, InterruptedException {

		List<String> trackUris = getTrackUris();

		System.out.println("Got tracks:");
		System.out.println(String.join("\n", trackUris));

		ReplacePlaylistsItemsRequest replacePlaylistsItemsRequest = spotifyApi
				.replacePlaylistsItems(PLAYLIST_ID, trackUris.toArray(new String[0])).build();

		try {
			final String string = replacePlaylistsItemsRequest.execute();

			System.out.println("Null: " + string);
		}
		catch (IOException | SpotifyWebApiException | ParseException e) {
			System.out.println("Error: " + e.getMessage());
		}
	}

	private static List<String> getTrackUris() throws IOException, InterruptedException {
		List<String> trackUris = new ArrayList<>();

		Map<String, Integer> fmSongs = getFMSongs();
		Comparator<Map.Entry<String, Integer>> comparator = Comparator.comparingInt(Map.Entry::getValue);

		List<Map.Entry<String, Integer>> sortedTracks = new ArrayList<>(fmSongs.entrySet());
		sortedTracks.sort(comparator.reversed());

		for (Map.Entry<String, Integer> track : sortedTracks) {
			final SearchTracksRequest searchTracksRequest = spotifyApi.searchTracks(track.getKey())
					.market(CountryCode.SK).limit(3).offset(0).includeExternal("audio").build();
			Thread.sleep(new Random().nextInt(500));
			try {
				final Paging<Track> trackPaging = searchTracksRequest.execute();
				Arrays.stream(trackPaging.getItems()).map(spotifyTrack -> {
					String trackTitle = Arrays.stream(spotifyTrack.getArtists()).map(ArtistSimplified::getName)
							.collect(Collectors.joining(", ")) + " - " + spotifyTrack.getName();
					Integer distance = levenshtein.apply(track.getKey().toLowerCase(), trackTitle.toLowerCase());
					return new TrackWithScore(spotifyTrack, distance);
				}).min(Comparator.comparing(TrackWithScore::getScore)).ifPresent(item -> {
					trackUris.add("spotify:track:" + item.getTrack().getId());
				});

			}
			catch (IOException | SpotifyWebApiException | ParseException ex) {
				System.out.println("Error: " + ex.getMessage());
			}
		}
		return trackUris;
	}

	private static Map<String, Integer> getFMSongs() throws IOException, InterruptedException {
		Map<String, Integer> tracksWithPopularity = new TreeMap<>();
		for (int page = 1; page < 10; page++) {
			Document doc = Jsoup.connect("https://fm.rtvs.sk/playlist?page=" + page).get();
			Elements playlistTableRows = doc.select("tbody>tr.table--playlist__list");
			for (Element row : playlistTableRows) {
				Element artist = row.select("td.artist").get(0);
				Element title = row.select("td.title").get(0);

				String track = String.format("%s - %s", artist.ownText(), title.ownText());
				tracksWithPopularity.put(track, Optional.ofNullable(tracksWithPopularity.get(track)).orElse(0) + 1);
			}
			Thread.sleep(new Random().nextInt(500));
		}
		return tracksWithPopularity;
	}

}
