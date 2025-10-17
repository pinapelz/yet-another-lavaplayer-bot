package commands;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.components.buttons.Button;


public class UIPusher {

    public void showControls(SlashCommandInteractionEvent event) {
        event.reply("Controls for the player:")
                .addComponents(ActionRow.of(
                        Button.primary("action-volumedown", Emoji.fromUnicode("U+1F509")),
                        Button.primary("action-skip", Emoji.fromUnicode("U+23E9")),
                        Button.primary("action-pause", Emoji.fromUnicode("U+23EF")),
                        Button.primary("action-stop", Emoji.fromUnicode("U+23F9")),
                        Button.primary("action-volumeup", Emoji.fromUnicode("U+1F50A"))
                ))
                .queue();
    }
}
