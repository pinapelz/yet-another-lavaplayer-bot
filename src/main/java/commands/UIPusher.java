package commands;

import audio.GuildMusicManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class UIPusher {

    public void showControls(SlashCommandEvent event){
        event.reply("Controls for the player:")
                .addActionRow(
                        Button.primary("action-volumedown", Emoji.fromUnicode("U+1F509")),
                        Button.primary("action-skip", Emoji.fromUnicode("U+23E9")),
                        Button.primary("action-pause", Emoji.fromUnicode("U+23EF")),
                        Button.primary("action-stop", Emoji.fromUnicode("U+23F9")),
                        Button.primary("action-volumeup", Emoji.fromUnicode("U+1F50A"))
                )
                .queue();
    }

}
