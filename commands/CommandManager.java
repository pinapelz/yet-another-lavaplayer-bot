package commands;

import audio.Music;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;
import utility.URLChecker;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CommandManager extends ListenerAdapter {
    Music music;
    public CommandManager(Music music){
        this.music = music;
    }
    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        String command = event.getName(); //test
        if (command.equals("play")) {
            music.playMusic(event);
        }
        else if(command.equals("leave")){
            event.getGuild().getAudioManager().setSendingHandler(null);
            event.getGuild().getAudioManager().closeAudioConnection();
            event.reply("OtsuRose! See you later!").queue();
        }
        else if(command.equals("vtmusic")){
            event.deferReply().queue();
            music.queueVTMusic((TextChannel) event.getChannel(),Integer.parseInt(event.getOption("number").getAsString()));
            event.getHook().sendMessage("Queued up " + Integer.parseInt(event.getOption("number").getAsString())+" songs!").queue();
        }
        else if(command.equals("showqueue")){
            music.showQueue((TextChannel) event.getChannel(), event);
        }
        else if(command.equals("skip")){
            music.skipTrack((TextChannel) event.getChannel(),event);

        }
        else if(command.equals("pause")){
            music.pausePlayer((TextChannel) event.getChannel(),event);

        }
        else if(command.equals("nowplaying")){
            music.showNowPlaying(event);

        }
        else if(command.equals("stop")){
            music.stopPlayer(event);

        }
        else if(command.equals("volume")){
            music.setVolume(event,event.getOption("volume").getAsString());
        }

        else if(command.equals("remove")){
            music.showQueueMenu((TextChannel) event.getChannel(), event,"remove-queue","Select a track to remove below");

        }
        else if(command.equals("inspect")){
            music.showQueueMenu((TextChannel) event.getChannel(), event,"inspect-queue","Select a track to inspect below");

        }
        super.onSlashCommand(event);
    }
    @Override
    public void onGuildReady(GuildReadyEvent event){
        List<CommandData> commandData = new ArrayList<>();
    }


}
