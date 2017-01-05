package SoundStuff;
import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;

import net.dv8tion.jda.core.audio.AudioSendHandler;
import net.dv8tion.jda.core.utils.SimpleLog;

public class SoundPlayer extends AudioEventAdapter implements AudioSendHandler
{
	
	private final AudioPlayer 			player;
	private final AudioPlayerManager 	playerManager;
	private AudioFrame 					lastFrame;
	private Runnable 					action;
	private SimpleLog					log;
	
	/* Adapter class for the LavaPlayer audio layer. */
	
	public SoundPlayer()
	{
		log = SimpleLog.getLog("SoundPlayer");
		
		playerManager = new DefaultAudioPlayerManager();
		playerManager.getConfiguration().setResamplingQuality(AudioConfiguration.ResamplingQuality.MEDIUM);
		playerManager.registerSourceManager(new LocalAudioSourceManager());
		player = playerManager.createPlayer();
		player.addListener(this);
	}
	
	public void play(String fileName, Runnable action)
	{
		this.action = action;
		
		playerManager.loadItem(fileName, new AudioLoadResultHandler()
		{

			@Override
			public void trackLoaded(AudioTrack track) 
			{
				log.info("Attempting to play the requested sound.");
				player.playTrack(track);
			}

			@Override
			public void playlistLoaded(AudioPlaylist playlist) 
			{
				
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
				
			}			
		});
	}
	
	public void stop()
	{
		if (player != null && player.getPlayingTrack() != null)
		{
			player.stopTrack();
			log.info("Successfully stopped track from playing.");
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
		return lastFrame.data;
	}

	@Override
	public boolean isOpus()
	{
		return true;
	}
	
}
