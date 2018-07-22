package Audio;

import Main.WeenieBot;
import com.sedmelluq.discord.lavaplayer.player.*;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import net.dv8tion.jda.core.audio.AudioSendHandler;

public class SoundPlayer extends AudioEventAdapter implements AudioSendHandler
{
    private static SoundPlayer instance;

    public static SoundPlayer getInstance()
    {
        if (instance == null)
        {
            instance = new SoundPlayer();
        }

        return instance;
    }

    public static void ResetSoundPlayer()
    {
        if (instance != null)
        {
            try
            {
                instance.player.destroy();
                instance.manager.shutdown();
            }
            catch (Exception ex)
            {
                // Ignore exceptions here, if we can't clear resources, we just re-initialize anyways.
            }
            finally
            {
                instance = new SoundPlayer();
            }
        }
    }

    private final AudioPlayer player;
    private final AudioPlayerManager manager;
    private AudioFrame lastFrame;
    private Runnable action;

    private SoundPlayer()
    {
        manager = new DefaultAudioPlayerManager();
        manager.getConfiguration().setResamplingQuality(AudioConfiguration.ResamplingQuality.MEDIUM);
        manager.registerSourceManager(new LocalAudioSourceManager());
        player = manager.createPlayer();
        player.addListener(this);
    }

    public void play(String fileName, Runnable action)
    {
        this.action = action;

        manager.loadItem(fileName, new AudioLoadResultHandler()
        {
            @Override
            public void trackLoaded(AudioTrack track)
            {
                WeenieBot.log.info("Attempting to play the requested sound.");
                player.playTrack(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist)
            {
                // Not used for what we're using the library for.
            }

            @Override
            public void noMatches()
            {
                if (action != null)
                    action.run();
            }

            @Override
            public void loadFailed(FriendlyException exception)
            {
                if (action != null)
                    action.run();
            }
        });
    }

    public void stop()
    {
        if (player != null && player.getPlayingTrack() != null)
        {
            player.stopTrack();
            WeenieBot.log.info("Successfully stopped the track.");
        }
    }

    public void setVolume(int volume)
    {
        player.setVolume(volume);
    }

    public int getVolume()
    {
        return player.getVolume();
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason reason)
    {
        if (action != null)
            action.run();

        player.stopTrack();
    }

    @Override
    public boolean canProvide()
    {
        lastFrame = player.provide();
        return lastFrame != null;
    }

    @Override
    public byte[] provide20MsAudio()
    {
        return lastFrame.getData();
    }

    @Override
    public boolean isOpus()
    {
        return true;
    }
}
