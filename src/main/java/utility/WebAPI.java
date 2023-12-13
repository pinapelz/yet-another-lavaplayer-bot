package utility;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class WebAPI {
    public String[] getURLsFromSite(String urlString) {
        try {
            System.out.println("Getting URLs from " + urlString);
            try (InputStream is = new URL(urlString).openStream();
                 BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                int cb;
                while ((cb = br.read()) != -1) {
                    sb.append((char) cb);
                }
                return sb.toString().split("\n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        catch (Exception e){
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }
}
