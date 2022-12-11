package utility;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.ArrayList;

public class YouTubeAPI {
    private String ytapiKey = "";
    public YouTubeAPI(String ytapiKey){
        this.ytapiKey = ytapiKey;
    }
    public String returnTopVideoURL(String keyword )throws IOException {
        String url = "https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=1&q="+keyword+"&type=video&key="+ytapiKey;
        url = url.replaceAll(" ", "%20");
        String data = Jsoup.connect(url).ignoreContentType(true).execute().body();
        JSONObject obj = new JSONObject(data);
        JSONArray arr = obj.getJSONArray("items");
        String videoID = "";
        for (int i = 0; i < arr.length(); i++)
        {
            videoID = arr.getJSONObject(i).getJSONObject("id").getString("videoId");
            System.out.println("Parsed ID "+ videoID);
        }
        return "https://www.youtube.com/watch?v="+videoID;
    }
    public void getAllURLPlaylist(String playlistID){
        try {
            String url = "https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&maxResults=50&playlistId="+playlistID+"&key="+ytapiKey;
            System.out.println(url);
            url = url.replaceAll(" ", "%20");
            String data = Jsoup.connect(url).ignoreContentType(true).execute().body();
            JSONObject obj = new JSONObject(data);
            JSONArray arr = obj.getJSONArray("items");
            //print arr
            for (int i = 0; i < arr.length(); i++) {
                String videoID = arr.getJSONObject(i).getJSONObject("id").getString("videoId");
                System.out.println("Parsed ID " + videoID);
            }

            }
         catch (IOException e) {
            e.printStackTrace();
        }
    }
}
