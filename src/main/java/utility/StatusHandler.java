package utility;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class StatusHandler {
    static JDA jda;
    public StatusHandler(JDA jda)
    {
        this.jda = jda;
    }
    public static void setSlashCommands(){
        // jda.updateCommands().queue();
        jda.upsertCommand(new CommandData("play","Adds a song to the queue with a URL or search terms").
                addOption(OptionType.STRING,"term","The link or search terms of the music to queue")
        ).queue();
        jda.upsertCommand(new CommandData("queue-recursive","Adds a set amount of random songs from a playlist").
                addOption(OptionType.STRING,"url","The link of the playlist")
                .addOption(OptionType.INTEGER,"amount","The amount of songs to queue")
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

}
