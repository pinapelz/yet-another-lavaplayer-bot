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
                        .build();
                final Track track = getTrackRequest.execute();
                searchQuery = track.getName();
                ArtistSimplified[] artists = track.getArtists();
                for (int i = 0; i < artists.length; i++) {
                    searchQuery = searchQuery + " " + artists[i].getName();
                }
                System.out.println(searchQuery);
            } catch (Exception e) {
                System.out.println("Error with getting name: " + e.getMessage() +"Retrying...");
                return null;

        }
        return searchQuery;

    }
    public static String getRandomPlaylistTrack_Sync(String playlistId) {
        checkRefreshToken();
        GetPlaylistRequest getPlaylistRequest = spotifyApi.getPlaylist(playlistId).build();
        try {
            Playlist playlist = getPlaylistRequest.execute();
            PlaylistTrack[] tracks = playlist.getTracks().getItems();
            int randomTrack = (int) (Math.random() * tracks.length);
            System.out.println(tracks[randomTrack].getTrack().getId());
            return tracks[randomTrack].getTrack().getId();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        return "";
    }
    public static PlaylistTrack[] getPlaylist_Sync(String playlistId) {
        checkRefreshToken();
        GetPlaylistRequest getPlaylistRequest = spotifyApi.getPlaylist(playlistId).build();
        try {
            Playlist playlist = getPlaylistRequest.execute();
            PlaylistTrack[] tracks = playlist.getTracks().getItems();
            return tracks;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        return null;
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
        String value = System.getenv(parameter);
        if (value != null) {
            return value;
        }
        throw new RuntimeException("Environment variable " + parameter + " not found");
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
