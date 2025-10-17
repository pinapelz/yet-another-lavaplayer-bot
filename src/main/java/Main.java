import audio.Music;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import io.github.cdimascio.dotenv.Dotenv;

public class Main extends ListenerAdapter {
    private static final Dotenv dotenv = Dotenv.load();

    public static void main(String[] args) {
        new Music("$",
                readSetting("YOUTUBEAPIKEY"),
                readSetting("DISCORDTOKEN")
        );
    }

    public static String readSetting(String parameter) {
        String value = System.getenv(parameter);
        if (value != null) return value;
        return dotenv.get(parameter);
    }

    @Override
    public void onReady(ReadyEvent event) {
        System.out.println("Loading Complete");
    }
}
