import audio.Music;
import commands.CommandManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.security.auth.login.LoginException;
import java.io.FileReader;



public class Main extends ListenerAdapter {
    public static JDABuilder jdabuilder = JDABuilder.createDefault(readSetting("discordToken")).addEventListeners(new Main());
    public static JDA jda;

    public static Music musicPlayer = new Music("$",readSetting("youtubeApi"));
    public static void main( String[] args)
    {
        try {
            jdabuilder.addEventListeners(musicPlayer);
            jdabuilder.addEventListeners(new CommandManager(musicPlayer));
            jda = jdabuilder.build();
            setSlashCommands();
            System.out.println("Bot Started");
        } catch (LoginException e) {
            throw new RuntimeException(e);
        }
    }


    public static void setSlashCommands(){
       // jda.updateCommands().queue();
        jda.upsertCommand(new CommandData("play","Adds a song to the queue with a URL or search terms").
                addOption(OptionType.STRING,"term","The link or search terms of the music to queue")
                ).queue();
        jda.upsertCommand(new CommandData("leave","Clears the queue and disconnects the bot from voice channel")).queue();
        jda.upsertCommand(new CommandData("showqueue","Shows the current queue")).queue();
        jda.upsertCommand(new CommandData("pause","Pauses the player")).queue();
        jda.upsertCommand(new CommandData("controls","Show an interface to control the player")).queue();
        jda.upsertCommand(new CommandData("skip","Skips the current song")).queue();
        jda.upsertCommand(new CommandData("nowplaying","Shows a detailed view of the current song playing")).queue();
        jda.upsertCommand(new CommandData("stop","Stops the player and clears the queue")).queue();
        jda.upsertCommand(new CommandData("remove","Remove a track in queue")).queue();
        jda.upsertCommand(new CommandData("shuffle","Shuffle the current queue")).queue();
        jda.upsertCommand(new CommandData("vtmusic","Queues a set number of random VTuber songs and covers").
                addOption(OptionType.INTEGER,"number","Number of songs to queue")
        ).queue();
        jda.upsertCommand(new CommandData("volume","Set the volume or leave blank to check current volume").
                addOption(OptionType.INTEGER,"volume","Volume from 0-100")
        ).queue();


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
    public void onMessageReceived(MessageReceivedEvent e) {
        JDA jda = e.getJDA();
        Message message = e.getMessage();
        String msg = message.getContentDisplay();
        if(msg.startsWith("!maintenance") && e.getAuthor().getId().equals("246787839570739211")){
            jda.getPresence().setActivity(net.dv8tion.jda.api.entities.Activity.watching("Maintenance Mode!"));
        }

        }

    @Override
    public void onReady(ReadyEvent event) {
        System.out.println("Loading Complete");
    }
}
