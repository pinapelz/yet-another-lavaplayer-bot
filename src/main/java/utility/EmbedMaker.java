package utility;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

public class EmbedMaker {
    public EmbedBuilder makeNowPlayingEmbed(AudioTrack currentTrack, String position, String duration,
                                            String thumbnailUrl, String url,Color color) {
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(color)
                .setTitle("Now Playing: " + currentTrack.getInfo().title)
                .setDescription(currentTrack.getInfo().author)
                .setImage(thumbnailUrl);
        embed.addField("Timestamp: ", "**[" + position + "/" + duration + "]**", false);
        embed.addField("", url, false);
        return embed;
    }

}
