package utility;

public class URLChecker {
    public boolean isURL(String term){
        return term.matches("^(http|https)://.*");
    }
    public String getURLType(String url) {
        if(url.matches("^((?:https?:)?\\/\\/)?((?:www|m)\\.)?youtube\\.com\\/playlist\\?list=([\\w\\-]+)$")){
            System.out.println("yt-playlist");
            return "yt-playlist";
        }
        else if (url.matches("^((?:https?:)?\\/\\/)?((?:www|m)\\.)?((?:youtube(-nocookie)?\\.com|youtu.be))(\\/(?:[\\w\\-]+\\?v=|embed\\/|v\\/)?)([\\w\\-]+)(\\S+)?$")) {
            return "yt"; //Youtube
        }
        else if (url.matches("^(https?:\\/\\/)?(www.)?(m\\.)?soundcloud\\.com\\/[\\w\\-\\.]+(\\/)+[\\w\\-\\.]+/?$")) {
            return "snd";
        } else if (url.matches("^(?:https?:\\/\\/)?(?:www\\.|go\\.)?twitch\\.tv\\/([a-z0-9_]+)($|\\?)")) {
            return "twitch";
        } else if (url.split("\\?si=")[0].matches("^(https?://)?(www.)?(open.)?spotify.com/(user/[a-zA-Z0-9]+|artist/[a-zA-Z0-9]+|album/[a-zA-Z0-9]+|track/[a-zA-Z0-9]+|playlist/[a-zA-Z0-9]+)$")) {
            return url.split("\\?si=")[0].matches("^(https?://)?(www.)?(open.)?spotify.com/playlist/[a-zA-Z0-9]+$") ? "spotify-playlist" : "spotify";
        }
        else if(url.endsWith(".txt")){
            return "txt-playlist";
        }
        if(!url.matches("^(https?|ftp|file)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]*[-A-Za-z0-9+&@#/%=~_|]")){
            return "search-term";
        }
        return "unknown";

        }

    public String getSpotifyTrackID(String uri){
        String[] uriParts = uri.split("\\?si=");
        return uriParts[0].replaceAll("https://open.spotify.com/track/","");

    }
    public String getSpotifyPlaylistID(String url){
        String[] uriParts = url.split("\\?si=");
        return uriParts[0].replaceAll("https://open.spotify.com/playlist/","");

    }
    public String getYouTubePlaylistID(String url){
        return url.split("list=")[1];
    }
}
