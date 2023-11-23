import audio.Music;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.FileReader;


public class Main extends ListenerAdapter {


    public static void main( String[] args)
    {
        new Music("$",readSetting("YOUTUBEAPIKEY"),readSetting("DISCORDTOKEN"));
    }

    public static String readSetting(String parameter){
        String value = System.getenv(parameter);
        if (value != null) {
            return value;
        }
        throw new RuntimeException("Environment variable " + parameter + " not found");
    }


    @Override
    public void onReady(ReadyEvent event) {
        System.out.println("Loading Complete");
    }
}
