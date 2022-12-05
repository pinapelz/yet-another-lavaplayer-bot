package audio;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import utility.SpotifyAPI;
import utility.URLChecker;

import java.awt.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.BlockingQueue;


public class Music extends ListenerAdapter {
    ArrayList<String> hololiveMusicURL = new ArrayList<String>();
    String ytapiKey = "";
    String spotifyapiKey = "";
    static String append = "!";
    private URLChecker urlCheck = new URLChecker();
    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagers;
    private SpotifyAPI spotifyAPI = new SpotifyAPI();
    public Music(String append, String ytapiKey) {
        this.musicManagers = new HashMap<>();
        this.ytapiKey = ytapiKey;
        this.spotifyapiKey = spotifyapiKey;
        this.append = append;
        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
        System.out.println("Filling Music List");
    }
    private synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        GuildMusicManager musicManager = musicManagers.get(guildId);

        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager);
            musicManagers.put(guildId, musicManager);
        }
        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }
    private void populateVTuberMusic(){
        try {
            URL url = new URL("https://pinapelz.github.io/vTuberDiscordBot/hololiveMusic.txt");
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            FileWriter writer = new FileWriter("data//hololiveMusic.txt");
            while ((line = in.readLine()) != null) {
                writer.write(line+"\n");
            }
            writer.close();
            in.close();
        }
        catch (MalformedURLException e) {
            System.out.println("Malformed URL: " + e.getMessage());
        }
        catch (IOException e) {
            System.out.println("I/O Error: " + e.getMessage());
        }
    }
    private void fillVTuberMusic(){
        populateVTuberMusic();
        Scanner s = null;
        try {
            s = new Scanner(new File("data//hololiveMusic.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        while (s.hasNext()){
            hololiveMusicURL.add(s.nextLine());
        }
        s.close();
    }
    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        Guild guild = event.getGuild();
        GuildMusicManager mng = getGuildAudioPlayer(guild);
        TrackScheduler scheduler = mng.scheduler;
        String[] command = event.getMessage().getContentRaw().split(" ", 2);

        if ((append+"play").equals(command[0]) && command.length == 2) {
            loadAndPlay(event.getChannel(), command[1],true);
        }
        else if((append+"refreshlist").equals(command[0])){
            event.getChannel().sendMessage("Refreshing songs database").queue();
            fillVTuberMusic();
            event.getChannel().sendMessage("Refresh Complete!").queue();
        }
        else if ((append+"shuffle").equals(command[0]))
        {
            if (scheduler.queue.isEmpty())
            {
                event.getChannel().sendMessage("The queue is currently empty!").queue();
                return;
            }

            scheduler.shuffle();
            event.getChannel().sendMessage("The queue has been shuffled!").queue();
        }
        else if("!holoadd".equals(command[0])){
            event.getChannel().sendMessage("The url has been successfully added to the database").queue();
        }
        else if("!dev".equals(command[0])){
            try {
                spotifyAPI.clientCredentials_Sync();
            }
            catch (Exception e){

            }
        }

        super.onGuildMessageReceived(event);
    }
    public void playMusic(SlashCommandEvent event){
        try {
            String userQuery = event.getOption("term").getAsString();
            if (urlCheck.isURL(userQuery) && !urlCheck.getURLType(userQuery).equals("spotify")&&!urlCheck.getURLType(userQuery).equals("spotify-playlist")) { //The term is a URL
                event.reply("Found Video: " + userQuery).queue();
                loadAndPlay((TextChannel) event.getChannel(), userQuery, false);
            } else {
                try {
                    if (urlCheck.getURLType(userQuery).equals("spotify")){

                        event.deferReply().queue();
                        event.getHook().sendMessage("Matched Video From Spotify: " + returnTopVideoURL(spotifyAPI.getSearchTerm_sync(urlCheck.getSpotifyTrackID(userQuery)))).queue();
                        loadAndPlay((TextChannel) event.getChannel(), returnTopVideoURL(spotifyAPI.getSearchTerm_sync(urlCheck.getSpotifyTrackID(userQuery))), true);
                    }
                    else if(urlCheck.getURLType(userQuery).equals("spotify-playlist")){
                        event.deferReply().queue();
                        String randomSong = spotifyAPI.getPlaylist_Sync(urlCheck.getSpotifyPlaylistID(userQuery));
                        event.getHook().sendMessage("Matched Video From Spotify Playlist: " + returnTopVideoURL(spotifyAPI.getSearchTerm_sync(randomSong))).queue();
                        loadAndPlay((TextChannel) event.getChannel(), returnTopVideoURL(spotifyAPI.getSearchTerm_sync(randomSong)), true);
                    }
                    else {
                        event.reply("Found Video: " + returnTopVideoURL(userQuery)).queue();
                        loadAndPlay((TextChannel) event.getChannel(), returnTopVideoURL(userQuery), true);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        catch(Exception e){
            event.reply("Error! Hazukashii! " + e.toString());
        }
    }

    public void setVolume( SlashCommandEvent event, String command){
        Guild guild = event.getGuild();
        GuildMusicManager mng = getGuildAudioPlayer(guild);
        AudioPlayer player = mng.player;
        if (command.equals("CHECK"))
        {
            event.reply("Current player volume: **" + player.getVolume() + "**").queue();
        }
        else
        {
            try
            {
                int newVolume = Math.max(10, Math.min(100, Integer.parseInt(command)));
                int oldVolume = player.getVolume();
                player.setVolume(newVolume);
               event.reply("Player volume changed from `" + oldVolume + "` to `" + newVolume + "`").queue();
            }
            catch (NumberFormatException e)
            {
                event.reply("`" + command + "` is not a valid integer. (10 - 100)").queue();
            }
        }
    }
    public void stopPlayer(SlashCommandEvent event){
        Guild guild = event.getGuild();
        GuildMusicManager mng = getGuildAudioPlayer(guild);
        AudioPlayer player = mng.player;
        TrackScheduler scheduler = mng.scheduler;
        scheduler.queue.clear();
        player.stopTrack();
        player.setPaused(false);
        event.reply("Playback has been completely stopped and the queue has been cleared.").queue();
    }
    public void pausePlayer(final TextChannel channel, SlashCommandEvent event){
        Guild guild = event.getGuild();
        GuildMusicManager mng = getGuildAudioPlayer(guild);
        AudioPlayer player = mng.player;
        if (player.getPlayingTrack() == null)
        {
            event.reply("Cannot pause or resume player because no track is loaded for playing.").queue();
            return;
        }
        player.setPaused(!player.isPaused());
        if (player.isPaused())
            event.reply("The player has been paused.").queue();
        else
            event.reply("The player has resumed playing.").queue();
    }
    public void queueVTMusic(final TextChannel channel, int songsToQueue){
        fillVTuberMusic();
        Collections.shuffle(hololiveMusicURL);
        System.out.println("Requesting to queue " + songsToQueue + " songs");
        System.out.println("Queueing all Hololive Music");
        for (int i = 0;i<songsToQueue;i++){
            loadAndPlay(channel, hololiveMusicURL.get(i),false);
        }
    }
    public void showNowPlaying(SlashCommandEvent event){
        Guild guild = event.getGuild();
        GuildMusicManager mng = getGuildAudioPlayer(guild);
        AudioPlayer player = mng.player;
        AudioTrack currentTrack = player.getPlayingTrack();
        if (currentTrack != null)
        {
            String currentTrackUrl  = currentTrack.getInfo().uri;
            String currentTrackUrlType = urlCheck.getURLType(currentTrackUrl);
            String title = currentTrack.getInfo().title;
            System.out.println(currentTrack.getInfo().uri);
            String position = getTimestamp(currentTrack.getPosition());
            String duration = getTimestamp(currentTrack.getDuration());
            if(currentTrackUrlType=="yt") { //YOUTUBE EMBED
                EmbedBuilder embed = new EmbedBuilder()
                        .setColor(new Color(0xFD0001))
                        .setTitle("Now Playing: " + title)
                        .setDescription(currentTrack.getInfo().author)
                        .setImage("https://img.youtube.com/vi/" + currentTrack.getIdentifier() + "/hqdefault.jpg");
                embed.addField("Timestamp: ", "**[" + position + "/" + duration + "]**", false);
                embed.addField("", "https://www.youtube.com/watch?v=" + currentTrack.getIdentifier(), false);
                MessageBuilder messageBuilder = (MessageBuilder) new MessageBuilder().setEmbeds(embed.build());
                event.reply(messageBuilder.build()).queue();
            }
            else if(currentTrackUrlType=="snd") { //SOUNDCLOUD EMBED
                EmbedBuilder embed = new EmbedBuilder()
                        .setColor(new Color(0xFD5401))
                        .setTitle("Now Playing: " + title)
                        .setDescription(currentTrack.getInfo().author);
                embed.addField("Timestamp: ", "**[" + position + "/" + duration + "]**", false);
                embed.addField("", currentTrack.getInfo().uri, false);
                MessageBuilder messageBuilder = (MessageBuilder) new MessageBuilder().setEmbeds(embed.build());
                event.reply(messageBuilder.build()).queue();
            }
            else if(currentTrackUrlType=="twitch"){ //TWITCH EMBED
                System.out.println("https://static-cdn.jtvnw.net/previews-ttv/live_user_" + currentTrack.getIdentifier() + "-440x248.jpg");
                EmbedBuilder embed = new EmbedBuilder()
                        .setColor(new Color(0xA86FFE))
                        .setTitle("Now Playing: " + title)
                        .setDescription(currentTrack.getInfo().author)//https://static-cdn.jtvnw.net/previews-ttv/live_user_cdawgva-440x248.jpg
                        .setImage("https://static-cdn.jtvnw.net/previews-ttv/live_user_" + currentTrack.getIdentifier().replaceAll("https://www.twitch.tv/","") + "-440x248.jpg");
                embed.addField("Timestamp: ", "Currently Live!", false);
                embed.addField("", currentTrack.getIdentifier(), false);
                MessageBuilder messageBuilder = (MessageBuilder) new MessageBuilder().setEmbeds(embed.build());
                event.reply(messageBuilder.build()).queue();
            }
        }
        else {
            event.reply("The player is not currently playing anything!").queue();
        }
    }
    public void showQueue(final TextChannel channel, @NotNull SlashCommandEvent event){
        Queue<AudioTrack> queue = getGuildAudioPlayer(event.getGuild()).scheduler.queue;
        synchronized (queue)
        {
            if (queue.isEmpty())
            {
                channel.sendMessage("The queue is currently empty!").queue();
            }
            else
            {
                int trackCount = 0;
                long queueLength = 0;
                StringBuilder sb = new StringBuilder();
                sb.append("```Current Queue: Entries: ").append(queue.size()).append("\n");
                for (AudioTrack track : queue)
                {
                    queueLength += track.getDuration();
                    if (trackCount < 10)
                    {
                        sb.append(trackCount+1 +". [").append(getTimestamp(track.getDuration())).append("] ");
                        sb.append(track.getInfo().title).append("\n");
                        trackCount++;
                    }
                }
                sb.append("\n").append("Total Queue Time Length: ").append(getTimestamp(queueLength)+"```");
                event.reply(sb.toString()).queue();
            }
        }
    }
    public void loadAndPlay(final @NotNull TextChannel channel, final String trackUrl, boolean returnMessage) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                if(returnMessage) {
                    channel.sendMessage("Adding to queue " + track.getInfo().title).queue();

                }

                play(channel.getGuild(), musicManager, track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack firstTrack = playlist.getSelectedTrack();

                if (firstTrack == null) {
                    firstTrack = playlist.getTracks().get(0);
                }
;                if(returnMessage){
                    channel.sendMessage("Adding to queue " + firstTrack.getInfo().title + " (first track of playlist " + playlist.getName() + ")").queue();
                }


                play(channel.getGuild(), musicManager, firstTrack);
            }

            @Override
            public void noMatches() {
                channel.sendMessage("Nothing found by " + trackUrl).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                if(returnMessage) {
                    channel.sendMessage("Could not play: " + exception.getMessage()).queue();
                    System.out.println(exception);
                }
            }
        });

    }



    private void play(Guild guild, GuildMusicManager musicManager, AudioTrack track) {
        connectToFirstVoiceChannel(guild.getAudioManager());
        musicManager.scheduler.queue(track);
        BlockingQueue<AudioTrack> s = musicManager.scheduler.queue;

    }

    public void skipTrack(TextChannel channel,SlashCommandEvent event) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        musicManager.scheduler.nextTrack();

        event.reply("Skipped to next track.").queue();
    }

    private static void connectToFirstVoiceChannel(AudioManager audioManager) {
        if (!audioManager.isConnected() && !audioManager.isAttemptingToConnect()) {
            for (VoiceChannel voiceChannel : audioManager.getGuild().getVoiceChannels()) {
                audioManager.openAudioConnection(voiceChannel);
                break;
            }
        }
    }
    private static String getTimestamp(long milliseconds)
    {
        int seconds = (int) (milliseconds / 1000) % 60 ;
        int minutes = (int) ((milliseconds / (1000 * 60)) % 60);
        int hours   = (int) ((milliseconds / (1000 * 60 * 60)) % 24);

        if (hours > 0)
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        else
            return String.format("%02d:%02d", minutes, seconds);
    }

    public String returnTopVideoURL(String keyword) throws IOException {
        String url = "https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=1&q="+keyword+"&type=video&key="+ytapiKey;
        url = url.replaceAll(" ", "%20");
        String data = Jsoup.connect(url).ignoreContentType(true).execute().body();
        JSONObject obj = new JSONObject(data);
        JSONArray arr = obj.getJSONArray("items");
        String videoID = "";
        for (int i = 0; i < arr.length(); i++)
        {
            videoID = arr.getJSONObject(i).getJSONObject("id").getString("videoId");
            System.out.println("Parsed ID "+ videoID);
        }
        return "https://www.youtube.com/watch?v="+videoID;
    }





}
