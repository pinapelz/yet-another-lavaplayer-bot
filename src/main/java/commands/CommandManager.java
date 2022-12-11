package commands;

import audio.Music;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Objects;

public class CommandManager extends ListenerAdapter {
    Music music;
    String vTuberSongDatabase = "https://pinapelz.github.io/vTuberDiscordBot/hololiveMusic.txt";
    public CommandManager(Music music){
        this.music = music;
    }
    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        String command = event.getName(); //test
        switch (command) {
            case "play":
                music.playMusic(event);
                break;
            case "leave":
                Objects.requireNonNull(event.getGuild()).getAudioManager().setSendingHandler(null);
                event.getGuild().getAudioManager().closeAudioConnection();
                event.reply("OtsuRose! See you later!").queue();
                break;
            case "queue-recursive":
                music.recursiveQueue(event, event.getOption("url").getAsString(), Integer.parseInt(event.getOption("amount").getAsString()));
                break;
            case "vtmusic":
                event.deferReply().queue();
                music.queueTrackFromLoadedList(event, Integer.parseInt(Objects.requireNonNull(event.getOption("number")).getAsString()), "VTubermusic.txt",vTuberSongDatabase);
                event.getHook().sendMessage("Queued up " + Integer.parseInt(Objects.requireNonNull(event.getOption("number")).getAsString()) + " songs!").queue();
                break;
            case "showqueue":
                music.showQueue(event);
                break;
            case "skip":
                music.skipTrack(event);

                break;
            case "pause":
                music.pausePlayer(event);

                break;
            case "controls":
                music.showControls(event);

                break;
            case "shuffle":
                music.shuffleQueue(event);

                break;
            case "nowplaying":
                music.showNowPlaying(event);

                break;
            case "stop":
                music.stopPlayer(event);

                break;
            case "volume":
                music.setVolume(event, Objects.requireNonNull(event.getOption("volume")).getAsString());
                break;
            case "remove":
                music.showQueueMenu(event, "remove-queue", "Select a track to remove below");

                break;
            case "inspect":
                music.showQueueMenu(event, "inspect-queue", "Select a track to inspect below");
                break;
        }
        super.onSlashCommand(event);
    }

}
