package utility;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetTrackRequest;
import java.io.FileReader;
import java.time.Instant;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
public class SpotifyAPI {
    private static final String clientId = readSetting("spotifyClientID");
    private static final String clientSecret = readSetting("spotifyClientSecret");

    public static String spotifyapiKey = "";
    public static long lastRefresh = 0;
    private static final SpotifyApi spotifyApi = new SpotifyApi.Builder()
            .setClientId(clientId)
            .setClientSecret(clientSecret)
            .build();
    private static final ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials()
            .build();


    public SpotifyAPI(){
        this.spotifyapiKey = readSetting("spotifyApi");
    }
    public static String getSearchTerm_sync(String trackid) {
        checkRefreshToken();
        String searchQuery = "";
        try {
           GetTrackRequest getTrackRequest = spotifyApi.getTrack(trackid)
//          .market(CountryCode.SE)
                    .build();
            final Track track = getTrackRequest.execute();
            searchQuery = track.getName();
            ArtistSimplified[] artists = track.getArtists();
            for (int i = 0;i< artists.length;i++){
                searchQuery = searchQuery +  " "+artists[i].getName();
            }
            System.out.println(searchQuery);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        return searchQuery;

    }
    public static String getPlaylist_Sync(String playlistId) {
        checkRefreshToken();
        GetPlaylistRequest getPlaylistRequest = spotifyApi.getPlaylist(playlistId).build();
        try {
            Playlist playlist = getPlaylistRequest.execute();
            PlaylistTrack[] tracks = playlist.getTracks().getItems();
            //pick a random track and return it
            int randomTrack = (int) (Math.random() * tracks.length);
            System.out.println(tracks[randomTrack].getTrack().getId());
            return tracks[randomTrack].getTrack().getId();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        return "";
    }
    public static void clientCredentials_Sync() {
        try {
            final ClientCredentials clientCredentials = clientCredentialsRequest.execute();
            spotifyApi.setAccessToken(clientCredentials.getAccessToken());
            System.out.println("Expires in: " + clientCredentials.getExpiresIn());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    public static String readSetting(String parameter){
        Object obj = null;
        try {
            obj = new JSONParser().parse(new FileReader("settings//config.json"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject jo = (JSONObject) obj;
        return (String) jo.get(parameter);
    }
    public static  void checkRefreshToken(){
        long unixTime = Instant.now().getEpochSecond();
        if(lastRefresh + 3600 <= unixTime){
            System.out.println("Spotify Token is expired, refreshing");
            clientCredentials_Sync();
            lastRefresh = Instant.now().getEpochSecond();
        }
    }

}
