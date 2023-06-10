package utility;


import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.*;

public class StatusHandler {
    static JDA jda;

    public StatusHandler(JDA jda) {
        this.jda = jda;
    }

    public static void setSlashCommands() {
        jda.updateCommands()
                .addCommands(
                        Commands.slash("play", "Adds a song to the queue with a URL or search terms").
                                addOption(OptionType.STRING, "term", "The link or search terms of the music to queue", true))
                .addCommands(
                        Commands.slash("queue-recursive", "dds a set amount of random songs from a playlist").
                                addOption(OptionType.STRING, "url", "The link of the playlist", true)
                                .addOption(OptionType.INTEGER, "amount", "The amount of songs to queue", true))
                .addCommands(
                        Commands.slash("leave", "Clears the queue and disconnects the bot from voice channel"))
                .addCommands(
                        Commands.slash("showqueue", "Shows the current queue"))
                .addCommands(
                        Commands.slash("pause", "Pauses the player"))
                .addCommands(
                        Commands.slash("controls", "Show an interface to control the player"))
                .addCommands(
                        Commands.slash("skip", "Skips the current song"))
                .addCommands(
                        Commands.slash("nowplaying", "Shows a detailed view of the current song playing"))
                .addCommands(
                        Commands.slash("stop", "Stops the player and clears the queue"))
                .addCommands(
                        Commands.slash("remove", "Removes a song from the queue"))
                .addCommands(
                        Commands.slash("shuffle", "Shuffles the queue"))
                .addCommands(
                        Commands.slash("vtmusic", "Queues a set number of random VTuber songs and covers").
                                addOption(OptionType.INTEGER, "number", "Number of songs to queue", true))
                .addCommands(
                        Commands.slash("volume", "Set the volume or leave blank to check current volume").
                                addOption(OptionType.INTEGER, "volume", "Volume from 0-100"))
                .queue();

    }

}
