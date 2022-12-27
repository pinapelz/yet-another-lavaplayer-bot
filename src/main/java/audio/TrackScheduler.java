package audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class schedules tracks for the audio player. It contains the queue of tracks.
 */
public class TrackScheduler extends AudioEventAdapter {
    public final AudioPlayer player;
    public BlockingQueue<AudioTrack> queue;
    public JDA jda;
    /**
     * @param player The audio player this scheduler uses
     */
    public TrackScheduler(AudioPlayer player, JDA jda) {
        this.player = player;
        this.jda = jda;
        this.queue = new LinkedBlockingQueue<>();
    }

    /**
     * Add the next track to queue or play right away if nothing is in the queue.
     *
     * @param track The track to play or add to queue.
     */
    public void queue(AudioTrack track) {
        //!player.startTrack(track, true)
        if (player.getPlayingTrack()!= null) {
            queue.offer(track);
            jda.getPresence().setActivity(Activity.playing(player.getPlayingTrack().getInfo().title));
        }
        else{
            player.startTrack(track, true);
            jda.getPresence().setActivity(Activity.playing(player.getPlayingTrack().getInfo().title));
        }

    }

    /**
     * Start the next track, stopping the current one if it is playing.
     */
    public void nextTrack() {
        player.startTrack(queue.poll(), false);
        jda.getPresence().setActivity(Activity.playing(player.getPlayingTrack().getInfo().title));


    }
    //TODO: FIGURE OUT HOW TO CHANGE THE STATUS AT THIS PART
    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        // Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
        jda.getPresence().setActivity(null);
        if (endReason.mayStartNext) {
            jda.getPresence().setActivity(Activity.playing(track.getInfo().title));
            nextTrack();
        }
    }
    public void shuffle()
    {
        Collections.shuffle((List<?>) queue);
    }
}