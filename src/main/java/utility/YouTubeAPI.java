package utility;

import com.google.gson.stream.JsonReader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;

public class YouTubeAPI {
    private String ytapiKey = "";
    public YouTubeAPI(String ytapiKey){
        this.ytapiKey = ytapiKey;
    }

    public static String convertToURLCompatible(String inputText) {
        try {
            String encodedText = URLEncoder.encode(inputText, "UTF-8");
            return encodedText.replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }

    public String returnTopVideoURL(String keyword) throws IOException {
        String url = "https://www.googleapis.com/youtube/v3/search?q=" + convertToURLCompatible(keyword) + "&key=" + ytapiKey;
        try (InputStream is = new URL(url).openStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")))) {
            StringBuilder sb = new StringBuilder();
            int cb;
            while ((cb = br.read()) != -1) {
                sb.append((char) cb);
            }
            JSONObject jsonObject = new JSONObject(sb.toString());
            String result = jsonObject.getJSONArray("items").getJSONObject(0).getJSONObject("id").getString("videoId");
            return "https://www.youtube.com/watch?v=" + result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
