package utility;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.IOException;

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
}
