import audio.Music;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.FileReader;


public class Main extends ListenerAdapter {


    public static void main( String[] args)
    {
        new Music("$",readSetting("youtubeApi"),readSetting("discordToken"));
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


    @Override
    public void onReady(ReadyEvent event) {
        System.out.println("Loading Complete");
    }
}
