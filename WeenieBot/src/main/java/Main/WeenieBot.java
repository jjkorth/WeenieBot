package Main;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.*;

import javax.security.auth.login.LoginException;

import CommandStuff.Command;
import Listeners.ChatListener;
import Listeners.EntranceListener;
import SoundStuff.Sound;
import SoundStuff.SoundLibrary;
import SoundStuff.SoundPlayer;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.managers.AudioManager;
import net.dv8tion.jda.core.utils.SimpleLog;

public class WeenieBot 
{
	private static final int	MAX_ATTEMPTS = 20;
	
	private static 	WeenieBot 	instance;
	private Properties			properties;
	private Timer				commandTimer;
	private SoundLibrary		library;
	
	private JDA 				bot;	
	private SimpleLog 			log;	
	
	private SoundPlayer			player;
	
	private Queue<Command> 		commandQueue;
	
	private boolean 			isLocked;
	
	private WeenieBot()
	{
	}
	
	// Singleton to allow only 1 bot instance
	public static WeenieBot getBot()
	{
		if (instance == null)
		{
			instance = new WeenieBot();
			instance.initialize();
		}
		
		return instance;
	}
	
	private void initialize()
	{
		this.commandQueue 	= new LinkedList<Command>();
		this.log 			= SimpleLog.getLog("WeenieBot");
		this.isLocked		= false;
		
		// Load the properties from prop.properties
		loadProperties();
		
		// Initialize the sound library and player
		this.library = new SoundLibrary(properties.getProperty("directory", "sounds"));
		this.player = new SoundPlayer();
		
		initializeBot();
		
		// Schedule the command timer <Default 1 second ticks>
		commandTimer = new Timer();
		commandTimer.scheduleAtFixedRate(new Tick(), 0, 1000);
	}
	
	private void initializeBot()
	{
		// Creates the JDA Isntance
		log.info("Initializing the Discord Bot.");
		try 
		{
			bot = new JDABuilder(AccountType.BOT)
					.setAudioEnabled(true)
					.setAutoReconnect(true)
					.setToken(properties.getProperty("token"))
					.buildBlocking();
			
			// Adds the listeners to process discord events.
			EntranceListener entranceListener = new EntranceListener();
			bot.addEventListener(entranceListener);
			
			ChatListener chatListener = new ChatListener(properties.getProperty("TOGGLE_CHAR", "!"), 2000);
			bot.addEventListener(chatListener);	
			
		} 
		catch (LoginException e)
		{
			log.fatal("Could not log in during bot initialization, make sure your token is correct.");
			System.exit(1);
		} 
		catch (IllegalArgumentException e)
		{
			log.fatal("Illegal Argument Exception during bot initialization.");
			bot.shutdown();
			System.exit(1);
		} 
		catch (InterruptedException e)
		{
			log.fatal("Interrupted during bot initialization.");
			bot.shutdown();
			System.exit(1);
		} 
		catch (RateLimitedException e)
		{
			log.fatal("Rate Limited Exception during bot initialization.");
			bot.shutdown();
			System.exit(1);
		}
	}
	
	// Adds a command to the command queue.
	public void addCommand(Command c)
	{
		log.info("Adding <" + c.getMessage() + "> command to queue.");
		commandQueue.add(c);
	}
	
	public void proccessCommand()
	{
		if (this.commandQueue.peek() == null)
			return;
		
		/* We lock the bot here to prevent any commands from running while a command is being processed */
		this.isLocked = true;
		
		/* Retrieve the first command in the queue */
		Command c = commandQueue.poll();		
		log.info("Attempting to process command: <" + c.getMessage() + ">");
		
		switch (c.getType())
		{
			/* Normal Commands that get processed in the order they come */
			case PLAY:
				playCommand(c.getUser(), c.getMessage(), (MessageReceivedEvent) c.getEvent());
				break;
				
			case ENTRANCE_PLAY:	
				GuildVoiceJoinEvent tempJE = (GuildVoiceJoinEvent) c.getEvent();
				entrancePlayCommand(c.getUser(), c.getMessage(), tempJE.getGuild(), tempJE.getChannelJoined());
				break;
				
			case MOVE_PLAY:
				GuildVoiceMoveEvent tempME = (GuildVoiceMoveEvent) c.getEvent();
				entrancePlayCommand(c.getUser(), c.getMessage(), tempME.getGuild(), tempME.getChannelJoined());
				break;
				
			case RELOAD_LIBRARY:
				reloadCommand();
				this.isLocked = false;
				break;
				
			case RANDOM:
				randomCommand(c.getUser(), (MessageReceivedEvent) c.getEvent());
				break;
				
			case VOLUME:
				volumeCommand(c.getUser(), c.getMessage(), (MessageReceivedEvent) c.getEvent());
				this.isLocked = false;
				break;
				
			case LIST:
				listCommand(c.getUser(), (MessageReceivedEvent) c.getEvent());
				this.isLocked = false;
				break;
				
			default:
				break;
		}
	}
	
	public void proccessUrgentCommand(Command c)
	{
		log.info("Attempting to process urgent command: <" + c.getMessage() + ">");
		
		/* These are commands that need to be immediately processed regardless of what we are doing */
		switch (c.getType())
		{
			case STOP:
				stopCommand(c.getUser(), (MessageReceivedEvent) c.getEvent());
				break;
				
			case SHUTDOWN:
				shutdownCommand();
				break;
				
			case RESET:
				MessageReceivedEvent tE = (MessageReceivedEvent) c.getEvent();
				resetCommand(tE.getGuild());
			
			default:
				break;
		}
	}
	
	private void playCommand(String user, String command, MessageReceivedEvent e)
	{
		// If no user, find the default user from the admin property.
		if (user == null || user.isEmpty())
			user = properties.getProperty("admin");
		
		// Check if the sound exists in the library.
		Sound sound = library.getFile(command);
		if (sound == null)
		{
			log.warn("Could not find the requested sound. <" + command + ">");
			this.isLocked = false;
			return;
		}
		
		Guild guild = null;
		VoiceChannel voiceChannel = null;
		
		// If the channel is private, we've been direct messaged.
		if (e.isFromType(ChannelType.PRIVATE))
		{
			// if that is the case, we need to find the messaging user in our available guild.
			guild = getGuild(e.getAuthor());
			if (guild == null)
			{
				log.warn("Could not find requesting user's guild, aborting.");
				this.isLocked = false;
				return;
			}
			
			voiceChannel = getVoiceChannel(e.getAuthor(), guild);
			if (voiceChannel == null)
			{
				log.warn("Could not find requesting user's voice channel, aborting.");
				this.isLocked = false;
				return;
			}
		}
		else
		{
			// Otherwise, just grab the guild from the message event.
			guild = e.getGuild();
			if (guild == null)
			{
				log.warn("Could not find requesting user's guild, aborting.");
				this.isLocked = false;
				return;
			}
			
			// And grab the voice channel the same way.
			voiceChannel = e.getMember().getVoiceState().getChannel();
			if (voiceChannel == null)
			{
				log.warn("Could not find requesting user's voice channel, aborting.");
				this.isLocked = false;
				return;
			}
		}
		
		
		// Attempt to join the channel.
		if (joinChannel(voiceChannel, guild) == false)
		{
			log.warn("Could not join the requesting user's channel, aborting.");
			this.isLocked = false;
			return;
		}
		
		// Attempt to play the sound.
		if (playSound(command, guild, voiceChannel) == false)
		{
			log.warn("Could not play the sound requested.");
			leaveChannel(guild, voiceChannel);
		}
	}
	
	// Called when a user joins a channel.
	private void entrancePlayCommand(String user, String command, Guild guild, VoiceChannel channel)
	{		
		if (joinChannel(channel, guild) == false)
		{
			log.warn("Could not join the requesting user's channel, aborting.");
			this.isLocked = false;
			return;
		}
		
		if (playSound(command, guild, channel) == false)
		{
			log.warn("Could not play the sound requested.");
			leaveChannel(guild, channel);
		}
	}
	
	private void stopCommand(String user, MessageReceivedEvent e)
	{			
		log.info(user + " is attempting to stop the track.");
		
		player.stop();
		
		// If the message is private, we've been direct messaged and need to find the guild/channel to stop.
		if (e.isFromType(ChannelType.PRIVATE))
		{
			Guild guild = getGuild(e.getAuthor());
			if (guild == null)
			{
				log.warn("Could not find requesting user's guild, aborting.");
				return;
			}
			
			VoiceChannel voiceChannel = getVoiceChannel(e.getAuthor(), guild);
			if (voiceChannel == null)
			{
				log.warn("Could not find requesting user's voice channel, aborting.");
				return;
			}
			
			if (leaveChannel(guild, voiceChannel) == false)
				log.warn("Could not leave the channel");
		}
		else
		{
			// Otherwise, just leave the channel using the event's values.
			if (leaveChannel(e.getGuild(), e.getMember().getVoiceState().getChannel()) == false)
				log.warn("Could not leave the channel");
		}
	}
	
	private void randomCommand(String user, MessageReceivedEvent e)
	{
		// Grab a random key from the library.
		String temp = library.getRandomKey();
		if (temp == null || temp.isEmpty())
		{
			this.isLocked = false;
			log.warn("No sound files in the sound library, cannot play random sound.");
			return;
		}
		
		// Then play it using the play command.
		playCommand(user, temp, e);
	}
	
	private void volumeCommand(String user, String command, MessageReceivedEvent e)
	{
		String[] temp = command.split(" ");
		if (temp.length < 2)
		{
			log.warn("No argument supplied for volume command.");
			sendMessage(e, "No argument supplied for volume command.");
			return;
		}
		
		// Bound the value to <0-100>
		int tempV = Integer.parseInt(temp[1]);
		if (tempV < 0)
			tempV = 0;
		else if (tempV > 100)
			tempV = 100;
		
		player.setVolume(tempV);
	}
	
	private void listCommand(String user, MessageReceivedEvent e)
	{
		String tempMessage = "Sounds Available:\n\n";
		
		// Add each key <sound> to the message
		for (String s : library.getLibrary().keySet())
			tempMessage += s + "\n";
		
		sendMessage(e, tempMessage);
	}
	
	private void reloadCommand()
	{
		// Reloads the library.
		library = new SoundLibrary(properties.getProperty("directory", "sounds"));
	}
	
	private boolean playSound(String id, Guild guild, VoiceChannel channel)
	{
		// Gets the audio manager, if it's null, sets it to the player.
		AudioManager manager = guild.getAudioManager();
		if (manager.getSendingHandler() == null)
			manager.setSendingHandler(player);
		
		// Gets the file.
		Sound sound = library.getFile(id);
		
		// Tells the sound player to play the file and what to do after it has done so (leave channel)
		player.play(sound.getPath(), () -> leaveChannel(guild, channel));
	
		return true;
	}
	
	private boolean joinChannel(VoiceChannel channel, Guild guild)
	{
		log.info("Attempt to join channel: <" + channel.getName() + ">");
		
		// Gets the audio manager, and opens an audio connection (Connects to the voice channel)
		AudioManager manager = guild.getAudioManager();
		if (manager.isConnected() || manager.isAttemptingToConnect())
		{
			manager.closeAudioConnection();
			manager.openAudioConnection(channel);
		}
		else		
			manager.openAudioConnection(channel);		
		
		// Waits for the connection to occur.
		int iterations = 0;
		while (!manager.isConnected())
		{
			try 
			{
				if (iterations >= MAX_ATTEMPTS)
					break;
			
				Thread.sleep(100);
				iterations++;
			} 
			catch (InterruptedException e) 
			{
				log.warn("Interrupted while attempting to connect to voice channel.");
				return false;
			}
		}
		
		log.info("Successfully joined channel: <" + channel.getName() + ">");
		
		return true;
	}
	
	private boolean leaveChannel(Guild guild, VoiceChannel channel)
	{
		// Gets the audio manager and closes the connection (Leaves the voice channel)
		AudioManager manager = channel.getGuild().getAudioManager();
		manager.closeAudioConnection();
		
		this.isLocked = false;
		
		return true;
	}
	
	public void shutdownCommand()
	{
		// Cleanly shuts down the bot.
		bot.shutdown();
		log.info("WeenieBot successfully shutdown.");
		
		System.exit(1);
	}
	
	public void resetCommand(Guild g)
	{
		// Reinitializes the sound player and sets the guilds sending handler to it.
		this.commandQueue = new LinkedList<Command>();
		
		player = new SoundPlayer();
		g.getAudioManager().setSendingHandler(player);
		
		commandTimer.cancel();
		commandTimer = new Timer();
		commandTimer.scheduleAtFixedRate(new Tick(), 0, 1000);
		
		this.isLocked = false;
	}
	
	public void sendMessage(MessageReceivedEvent e, String message)
	{
		// Sends a private message to a user.
		log.info("Sending message to: " + e.getAuthor().getName());
		if (e.getAuthor().getPrivateChannel() == null)
			e.getAuthor().openPrivateChannel().queue();
		
		e.getAuthor().getPrivateChannel().sendMessage(message).queue();
	}
	
	// Function to find a user in all of the guilds the bot is in.
	private Guild getGuild(User user)
	{
		for (Guild g : bot.getGuilds())
		{
			for (VoiceChannel v : g.getVoiceChannels())
			{
				for (Member m : v.getMembers())
				{
					if (m.getUser().getId().equalsIgnoreCase(user.getId()))
					{
						return g;
					}
				}
			}
		}
		
		return null;
	}
	
	// Function to find a channel where a user presides.
	private VoiceChannel getVoiceChannel(User user, Guild guild)
	{
		for (VoiceChannel v : guild.getVoiceChannels())
		{
			for (Member m : v.getMembers())
			{
				if (m.getUser().getId().equalsIgnoreCase(user.getId()))
				{
					return v;
				}
			}
		}
		
		return null;
	}
	
	public Properties getProperties()
	{
		return this.properties;
	}
	
	public boolean isLocked()
	{
		return this.isLocked;
	}
	
	private void loadProperties()
	{
		properties = new Properties();
		InputStream inStream;
		
		try
		{
			log.info("Loading the properties from the properties file: prop.properties");
			
			// Loads the prop.properties file.
			inStream = new FileInputStream("prop.properties");
			properties.load(inStream);
			inStream.close();
			return;
		}
		catch (FileNotFoundException fe)
		{
			log.warn("Could not find the properties file, creating a blank template one.");
			
			// If it doesn't exist, create it.
			try 
			{
				PrintStream writer = new PrintStream(new File("prop.properties"));
				
				writer.println("token=");
				writer.println("admin=");
				writer.println("directory=sounds");
				writer.println("toggle_char=!");
				writer.println("wait_time=100");
				writer.println("max_attempts=20");
				
				writer.close();
				
				log.warn("Properties file successfully created, please fill in the token and admin field, then try again.");
				return;			
			} 
			catch (IOException ie) 
			{
				log.fatal("I/O error while trying to write the properties file.");
				bot.shutdown();
				System.exit(1);
			}
		}
		catch (IOException ie)
		{
			log.fatal("I/O error while trying to read the properties file.");
			bot.shutdown();
			System.exit(1);
		}	
	}
}
