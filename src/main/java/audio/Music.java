package audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import commands.CommandManager;
import commands.UIPusher;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;


import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;

import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import utility.*;
import javax.security.auth.login.LoginException;
import java.awt.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Queue;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;



public class Music extends ListenerAdapter {
    public static JDA jda;
    public static JDABuilder jdabuilder;
    private ArrayList<String> currentlyLoadedPlaylist = new ArrayList<>();
    private String ytapiKey;
    static String append = "$";
    private final URLChecker urlCheck = new URLChecker();
    private StatusHandler statusHandler;
    private UIPusher uiPusher = new UIPusher();
    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagers;
    private final SpotifyAPI spotifyAPI = new SpotifyAPI();
    private final EmbedMaker embedMaker = new EmbedMaker();

    public Music(String append, String ytapiKey, String discordToken) {
        this.musicManagers = new HashMap<>();
        this.ytapiKey = ytapiKey;
        Music.append = append;
        this.playerManager = new DefaultAudioPlayerManager();
        jdabuilder = JDABuilder.createDefault(discordToken);
        try {
            jdabuilder.enableIntents(GatewayIntent.MESSAGE_CONTENT);
            jdabuilder.addEventListeners(this);
            jdabuilder.addEventListeners(new CommandManager(this));
            jda = jdabuilder.build();
            statusHandler = new StatusHandler(jda);
            statusHandler.setSlashCommands();
            System.out.println("Bot Started");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //Registering audio sources
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
    }
    private synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        GuildMusicManager musicManager = musicManagers.get(guildId);

        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager,jda);
            musicManagers.put(guildId, musicManager);
        }
        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }

    private void populateFileFromURL(String link,String fileName){
        try {
            URL url = new URL(link);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            File f = new File("data//"+fileName);
            if(!f.exists()){ //if file doesn't exist, create it
                f.createNewFile();
            }else{//if file exists, delete it
                f.delete();
                f.createNewFile();
            }

            FileWriter writer = new FileWriter("data//"+fileName);
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
    private void fillLoadedPlaylist(String url,String fileName){
        populateFileFromURL(url,fileName);
        Scanner s = null;
        try {
            s = new Scanner(new File("data//"+fileName));
        } catch (FileNotFoundException e) {
            System.out.println("File Creation was unsuccessful. Can't access local playlist");
        }
        while (true){
            assert s != null;
            if (!s.hasNext()) break;
            currentlyLoadedPlaylist.add(s.nextLine());
        }
        s.close();
    }
    public void queueTrackFromLoadedList(SlashCommandInteractionEvent event, int songsToQueue, String url){
        fillLoadedPlaylist(url,"songdb.txt");
        Collections.shuffle(currentlyLoadedPlaylist);
        for (int i = 0;i<songsToQueue;i++){
            loadAndPlay((TextChannel) event.getChannel(), currentlyLoadedPlaylist.get(i),false);
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String msg = event.getMessage().getContentRaw();
        //TODO: Add voice recognition for commands?
        super.onMessageReceived(event);
    }




    public void showQueueMenu(SlashCommandInteractionEvent event, String param, String instruction){
        Guild guild = event.getGuild();
        GuildMusicManager mng = getGuildAudioPlayer(guild);
        Queue<AudioTrack> queue = getGuildAudioPlayer(event.getGuild()).scheduler.queue;
        List<SelectOption> trackMenuOptions = new ArrayList<SelectOption>();
        synchronized (queue)
        {
            if (queue.isEmpty())
            {
                event.reply("The queue is currently empty!").queue();
            }
            else
            {
                int trackCount = 0;
                for (AudioTrack track : queue)
                {
                    if (trackCount != 25)
                    {
                        SelectOption option = SelectOption.of(track.getInfo().title,param+" "+track.getInfo().title);
                        trackMenuOptions.add(option);
                        trackCount++;
                    }
                }
                StringSelectMenu menu = StringSelectMenu.create("menu:class")
                        .setPlaceholder("-Select a track-") // shows the placeholder indicating what this menu is for
                        .setRequiredRange(1,1)// only one can be selected
                        .addOptions(trackMenuOptions)
                        .build();
                event.reply(instruction)
                        .setEphemeral(true)
                        .addActionRow(menu)
                        .queue();

            }
        }
    }
    public void showControls(SlashCommandInteractionEvent event){
        uiPusher.showControls(event);
    }
    public void shuffleQueue(SlashCommandInteractionEvent event){
        Guild guild = event.getGuild();
        GuildMusicManager mng = getGuildAudioPlayer(guild);
        Queue<AudioTrack> queue = getGuildAudioPlayer(event.getGuild()).scheduler.queue;
        if (queue.isEmpty())
        {
            event.reply("The queue is currently empty!").queue();
            return;
        }
        else{

            ArrayList<Object> currentQueue = new ArrayList(queue); //Conversion of queue to arraylist to allow for shuffling
            Collections.shuffle(currentQueue);
            BlockingQueue<AudioTrack> newQueue =  new LinkedBlockingQueue<>();
            for (Object track : currentQueue) {
                newQueue.add((AudioTrack) track);
            }
            mng.scheduler.queue = newQueue;
        }
        event.reply("The queue has been shuffled!").queue();
    }

    public void playMusic(SlashCommandInteractionEvent event){
       final YouTubeAPI youtubeAPI = new YouTubeAPI(ytapiKey);
        try {
            String userQuery = event.getOption("term").getAsString();
            if (urlCheck.isURL(userQuery) && !urlCheck.getURLType(userQuery).equals("spotify")&&!urlCheck.getURLType(userQuery).equals("spotify-playlist")) { //The term is a URL
                event.reply("Found Video: " + userQuery).queue();

                loadAndPlay((TextChannel) event.getChannel(), userQuery, false);
            }
            else { //Run checks if its not a directly playable URL
                try {
                    if (urlCheck.getURLType(userQuery).equals("spotify")){

                        event.deferReply().queue();
                        event.getHook().sendMessage("Matched Video From Spotify: " + youtubeAPI.returnTopVideoURL(spotifyAPI.getSearchTerm_sync(urlCheck.getSpotifyTrackID(userQuery)))).queue();
                        loadAndPlay((TextChannel) event.getChannel(), youtubeAPI.returnTopVideoURL(spotifyAPI.getSearchTerm_sync(urlCheck.getSpotifyTrackID(userQuery))), true);
                    }
                    else if(urlCheck.getURLType(userQuery).equals("spotify-playlist")){
                        event.deferReply().queue();
                        //TODO: Add playlist support using selection menu
                        String randomSong = spotifyAPI.getRandomPlaylistTrack_Sync(urlCheck.getSpotifyPlaylistID(userQuery));
                        event.getHook().sendMessage("Matched Video From Spotify Playlist: " + youtubeAPI.returnTopVideoURL(spotifyAPI.getSearchTerm_sync(randomSong))).queue();
                        loadAndPlay((TextChannel) event.getChannel(), youtubeAPI.returnTopVideoURL(spotifyAPI.getSearchTerm_sync(randomSong)), true);
                    }
                    else {
                        String top_video = youtubeAPI.returnTopVideoURL(userQuery);
                        event.reply("Found Video: " + top_video).queue();
                        loadAndPlay((TextChannel) event.getChannel(), top_video, true);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        catch(Exception e){
            event.reply("Error! Hazukashii! " + e);
        }
    }

    public void setVolume(SlashCommandInteractionEvent event, String command){
        Guild guild = event.getGuild();
        assert guild != null;
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

    public void stopPlayer(SlashCommandInteractionEvent event){
        Guild guild = event.getGuild();
        GuildMusicManager mng = getGuildAudioPlayer(guild);
        AudioPlayer player = mng.player;
        TrackScheduler scheduler = mng.scheduler;
        scheduler.queue.clear();
        player.stopTrack();
        player.setPaused(false);
        event.reply("Playback has been completely stopped and the queue has been cleared.").queue();
    }

    public void pausePlayer(SlashCommandInteractionEvent event){
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

    public void showNowPlaying(SlashCommandInteractionEvent event){
        Guild guild = event.getGuild();
        GuildMusicManager mng = getGuildAudioPlayer(guild);
        AudioPlayer player = mng.player;
        AudioTrack currentTrack = player.getPlayingTrack();
        if (currentTrack != null)
        {
            String currentTrackUrl  = currentTrack.getInfo().uri;
            String currentTrackUrlType = urlCheck.getURLType(currentTrackUrl);
            System.out.println(currentTrack.getInfo().uri);
            String position = getTimestamp(currentTrack.getPosition());
            String duration = getTimestamp(currentTrack.getDuration());
            if(currentTrackUrlType=="yt") { //YOUTUBE EMBED
                EmbedBuilder embed = embedMaker.makeNowPlayingEmbed(currentTrack,position,duration,
                        "https://img.youtube.com/vi/" + currentTrack.getIdentifier() + "/hqdefault.jpg",
                        "https://www.youtube.com/watch?v=" + currentTrack.getIdentifier(),new Color(0xFD0001));
                MessageCreateBuilder messageBuilder = new MessageCreateBuilder().setEmbeds(embed.build());
                event.reply(messageBuilder.build()).queue();
            }
            else if(currentTrackUrlType=="snd") { //SOUNDCLOUD EMBED
                EmbedBuilder embed = embedMaker.makeNowPlayingEmbed(currentTrack,position,duration,
                        "https://1000logos.net/wp-content/uploads/2021/04/Soundcloud-logo.png",
                        currentTrack.getInfo().uri,new Color(0xFD5401));
                MessageCreateBuilder messageBuilder = new MessageCreateBuilder().setEmbeds(embed.build());

                event.reply(messageBuilder.build()).queue();
            }
            else if(currentTrackUrlType=="twitch"){ //TWITCH EMBED
                EmbedBuilder embed = embedMaker.makeNowPlayingEmbed(currentTrack,"Currently","Live",
                        "https://static-cdn.jtvnw.net/previews-ttv/live_user_" +
                                currentTrack.getIdentifier().replaceAll("https://www.twitch.tv/","") + "-440x248.jpg",
                        currentTrack.getIdentifier(),
                        new Color(0xA86FFE));
                MessageCreateBuilder messageBuilder = new MessageCreateBuilder().setEmbeds(embed.build());
                event.reply(messageBuilder.build()).queue();
            }
        }
        else {
            event.reply("The player is not currently playing anything!").queue();
        }
    }

    public void showQueue(SlashCommandInteractionEvent event){
        Queue<AudioTrack> queue = getGuildAudioPlayer(event.getGuild()).scheduler.queue;
        synchronized (queue)
        {
            if (queue.isEmpty())
            {
                event.reply("The queue is currently empty!").queue();
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


    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event){
        if(event.getValues().get(0).contains("remove-queue")) {
            boolean deletedSong = false;
            Queue<AudioTrack> queue = getGuildAudioPlayer(event.getGuild()).scheduler.queue;
            BlockingQueue<AudioTrack> newQueue =  new LinkedBlockingQueue<>();
            String trackName = event.getValues().get(0).replaceAll("remove-queue ", "");
            synchronized (queue) {
                if (queue.isEmpty())
                {
                    event.reply("The queue is currently empty!").queue();
                }
                else {
                    for (AudioTrack track : queue) {
                        if (!track.getInfo().title.equals(trackName)) {
                            newQueue.add(track);
                        }
                        else{
                            deletedSong = true;
                        }
                    }
                    getGuildAudioPlayer(event.getGuild()).scheduler.queue = newQueue;
                    if(deletedSong){
                        event.reply("Removed " + trackName + " from the queue!").queue();
                    }
                    else{
                        event.reply("Could not find " + trackName + " in the queue!").queue();
                    }

                }

            }
        }

    }
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event){
        Guild guild = event.getGuild();
        GuildMusicManager mng = getGuildAudioPlayer(guild);
        AudioPlayer player = mng.player;
        if(event.getComponentId().equals("action-volumedown")){
            int newVolume = Math.max(10, Math.min(100, player.getVolume()-5));
            int oldVolume = player.getVolume();
            player.setVolume(newVolume);
            event.reply("Player volume changed from `" + oldVolume + "` to `" + newVolume + "`").queue();

        }
        else if(event.getComponentId().equals("action-volumeup")){
            int newVolume = Math.max(10, Math.min(100, player.getVolume()+5));
            int oldVolume = player.getVolume();
            player.setVolume(newVolume);
            event.reply("Player volume changed from `" + oldVolume + "` to `" + newVolume + "`").queue();

        }
        else if(event.getComponentId().equals("action-pause")){
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
        else if(event.getComponentId().equals("action-stop")){
            TrackScheduler scheduler = mng.scheduler;
            scheduler.queue.clear();
            player.stopTrack();
            player.setPaused(false);
            event.reply("Playback has been completely stopped and the queue has been cleared.").queue();
        }
        else if(event.getComponentId().equals("action-skip")){
            GuildMusicManager musicManager = getGuildAudioPlayer(event.getGuild());
            musicManager.scheduler.nextTrack();
            event.reply("Skipped to next track.").queue();
        }


    }
    public void recursiveQueue(SlashCommandInteractionEvent event, String playlistUrl,int amount){
        final YouTubeAPI youtubeAPI = new YouTubeAPI(ytapiKey);
        System.out.println(urlCheck.getURLType(playlistUrl));
        if(urlCheck.isURL(playlistUrl) && urlCheck.getURLType(playlistUrl).equals("spotify-playlist")){
            event.deferReply().queue();
            try {
                for(int i = 0;i<amount;i++) {
                    String randomSong = spotifyAPI.getRandomPlaylistTrack_Sync(urlCheck.getSpotifyPlaylistID(playlistUrl));
                    loadAndPlay((TextChannel) event.getChannel(), youtubeAPI.returnTopVideoURL(spotifyAPI.getSearchTerm_sync(randomSong)), false);
                }
                event.getHook().sendMessage("Queueing " + amount + " songs recursively").queue();
            }
            catch(Exception e){
                event.getHook().sendMessage("Error: " + e.getMessage()).queue();
            }
            System.out.println("Spotify Playlist Recursive");
        }


    }
    public void loadAndPlay(final TextChannel channel, final String trackUrl, boolean returnMessage) {
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
                    exception.printStackTrace();
                }
            }
        });

    }



    private void play(Guild guild, GuildMusicManager musicManager, AudioTrack track) {
        connectToFirstVoiceChannel(guild.getAudioManager());
        musicManager.scheduler.queue(track);
        System.out.println("Playing " + track.getInfo().title);
    }

    public void skipTrack(SlashCommandInteractionEvent event) {
        GuildMusicManager musicManager = getGuildAudioPlayer(event.getGuild());
        musicManager.scheduler.nextTrack();
        event.reply("Skipped to next track.").queue();
    }

    private static void connectToFirstVoiceChannel(AudioManager audioManager) {
        System.out.println("Connecting to voice channel");
        if (!audioManager.isConnected()) {

            for (VoiceChannel voiceChannel : audioManager.getGuild().getVoiceChannels()) {
                System.out.println("Attempting to connect to " + voiceChannel.getName());
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



}
