package com.github.misostc;

import com.google.gson.JsonArray;
import com.neovisionaries.i18n.CountryCode;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.model_objects.special.SnapshotResult;
import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRefreshRequest;
import com.wrapper.spotify.requests.data.playlists.AddItemsToPlaylistRequest;
import com.wrapper.spotify.requests.data.playlists.ReplacePlaylistsItemsRequest;
import com.wrapper.spotify.requests.data.playlists.UploadCustomPlaylistCoverImageRequest;
import com.wrapper.spotify.requests.data.search.simplified.SearchTracksRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

class SpotifyApi {

    private final com.wrapper.spotify.SpotifyApi spotifyApi;

    public SpotifyApi(String clientId, String clientSecret, String refreshToken) {
        spotifyApi = new com.wrapper.spotify.SpotifyApi.Builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setRefreshToken(refreshToken).build();
        final AuthorizationCodeRefreshRequest authorizationCodeRefreshRequest = spotifyApi.authorizationCodeRefresh()
                .build();
        final AuthorizationCodeCredentials clientCredentials;
        try {
            clientCredentials = authorizationCodeRefreshRequest.execute();
            spotifyApi.setAccessToken(clientCredentials.getAccessToken());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<SpotifyTrack> findTrackInSpotify(RadioFMTrack radioFMTrack) {
        String searchString = toSearchString(radioFMTrack);
        final SearchTracksRequest searchTracksRequest =
                spotifyApi.searchTracks(searchString)
                        .market(CountryCode.SK)
                        .limit(3)
                        .offset(0)
                        .includeExternal("audio").build();
        try {
            final Paging<Track> trackPaging = searchTracksRequest.execute();
            return toSpotifyTracks(trackPaging.getItems());
        } catch (IOException | SpotifyWebApiException | ParseException ex) {
            System.err.println("Error while searching song");
            throw new RuntimeException(ex);
        }
    }

    private String toSearchString(RadioFMTrack radioFMTrack) {
        return radioFMTrack.toSimpleString();
    }

    private List<SpotifyTrack> toSpotifyTracks(Track[] items) {
        List<SpotifyTrack> resultList = new ArrayList<>();
        for (Track track : items) {
            SpotifyTrack spotifyTrack = new SpotifyTrack();
            spotifyTrack.setArtists(Arrays.stream(track.getArtists()).map(ArtistSimplified::getName).collect(Collectors.toList()));
            spotifyTrack.setTitle(track.getName());
            spotifyTrack.setDuration(Duration.ofMillis(track.getDurationMs()));
            spotifyTrack.setSpotifyUri("spotify:track:" + track.getId());
            resultList.add(spotifyTrack);
        }
        return resultList;
    }

    public void updatePlaylist(String playlistId, List<SpotifyTrack> trackList) {

        clearPlaylist(playlistId);

        final List<String> uris = new ArrayList<>();
        uris.addAll(trackList.stream().map(SpotifyTrack::getSpotifyUri).collect(Collectors.toList()));

        while (!uris.isEmpty()) {
            final List<String> firstChunk = uris.size() > 100 ? uris.subList(0, 99) : uris;

            JsonArray playlistArray = new JsonArray();
            firstChunk.forEach(playlistArray::add);
            
            AddItemsToPlaylistRequest addItemsToPlaylistRequest = spotifyApi
                    .addItemsToPlaylist(playlistId, playlistArray).build();
            try {
                final SnapshotResult result = addItemsToPlaylistRequest.execute();
                System.out.println("SnapshotID: " + result.getSnapshotId());
                uris.removeAll(firstChunk);
            } catch (IOException | SpotifyWebApiException | ParseException ex) {
                System.err.println("Error while updating playlist");
                throw new RuntimeException(ex);
            }
        }
    }

    private void clearPlaylist(final String playlistId) {
        ReplacePlaylistsItemsRequest makePlaylistEmpty = spotifyApi
                .replacePlaylistsItems(playlistId, new JsonArray()).build();
        try {
            final String string = makePlaylistEmpty.execute();
            System.out.println("ClearedPlaylist: " + string);
        } catch (IOException | SpotifyWebApiException | ParseException ex) {
            System.err.println("Error while clearing playlist");
            throw new RuntimeException(ex);
        }
    }

    public void updatePlaylistCover(String playlistId, byte[] imageJPG) {
        UploadCustomPlaylistCoverImageRequest coverImageRequest = spotifyApi.uploadCustomPlaylistCoverImage(playlistId)
                .image_data(Base64.getEncoder().encodeToString(imageJPG))
                .build();
        try {
            coverImageRequest.execute();
        } catch (IOException | SpotifyWebApiException | ParseException ex) {
            throw new RuntimeException(ex);
        }
    }
}
