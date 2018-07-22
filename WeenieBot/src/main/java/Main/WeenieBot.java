package Main;

import Audio.*;
import Commands.*;
import Listeners.ChatListener;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.managers.AudioManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import javax.security.auth.login.LoginException;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Queue;
import java.util.Timer;

public class WeenieBot
{
    //--------------------------------------------------------------------------------

    public static final Logger log = LogManager.getLogger("WeenieBot");

    //--------------------------------------------------------------------------------

    private static final int MAX_CONNECTION_ATTEMPTS = 20;

    //--------------------------------------------------------------------------------

    // Singleton Main.WeenieBot instance
    private static WeenieBot instance = null;

    //--------------------------------------------------------------------------------

    private JDA bot = null;

    private Queue<Command> commandQueue = null;

    private Properties  properties      = null;
    private Timer       commandTimer    = null;

    private boolean isLocked = false;

    private Guild test;

    //--------------------------------------------------------------------------------

    public static WeenieBot getInstance()
    {
        if (instance == null)
            instance = new WeenieBot();

        return instance;
    }

    private WeenieBot()
    {
        // Initialize Properties
        properties = PropertyLoader.getProperties();

        this.commandQueue = new LinkedList<>();

        // Initialize SoundLibrary
        SoundLibrary.getInstance();

        log.info("Initializing the Discord Bot...");
        try
        {
            bot = new JDABuilder(AccountType.BOT)
                    .setToken(properties.getProperty("token"))
                    .setAudioEnabled(true)
                    .setAutoReconnect(true)
                    .buildBlocking();

            bot.addEventListener(new ChatListener(properties.getProperty("toggle_char"), 2000));
        }
        catch (LoginException le)
        {
            log.fatal("Could not log in to Discord Server, make sure your token is correct. Exiting...");
            System.exit(1);
        }
        catch (InterruptedException ie)
        {
            log.fatal("Interrupted exception during initialization, this should not happen. Exiting.");
            System.exit(1);
        }

        // Startup the command timer
        commandTimer = new Timer();
        commandTimer.scheduleAtFixedRate(new Tick(), 0, 1000);
    }

    public boolean AcquireLock()
    {
        if (!isLocked)
        {
            isLocked = true;
            return true;
        }

        return false;
    }

    public boolean isLocked()
    {
        return isLocked;
    }

    public void ReleaseLock()
    {
        isLocked = false;
    }

    public void addCommand(Command command)
    {
        log.info("Adding <" + command.message + "> to the command queue.");
        commandQueue.add(command);
    }

    public void processCommand()
    {
        if (commandQueue.peek() == null)
            return;

        AcquireLock();

        Command command = commandQueue.poll();
        log.info("Attempting to process command: <" + command.message + ">");

        try
        {
            command.processCommand();
        }
        catch (Exception ex)
        {
            // Release lock on error
            log.error("Could not process command: <" + command.message + ">\r\n\t" + ex.toString());
            ReleaseLock();
        }

        // Lock is released in each command
    }

    public void processUrgentCommand(Command command)
    {
        log.info("Attempting to process urgent command: <" + command.message + ">");

        try
        {
            command.processCommand();
        }
        catch (Exception ex)
        {
            log.error("Could not process command: <" + command.message + ">\r\n\t" + ex.toString());
        }
    }

    public void sendMessage(MessageReceivedEvent e, String message)
    {
        log.info("Sending message to: " + e.getAuthor().getId());
        e.getAuthor().openPrivateChannel().complete().sendMessage(message).complete();
    }

    public boolean playSound(String id, Guild guild, VoiceChannel vChannel)
    {
        // Gets the audio manager, if it's null, set it to the player
        AudioManager manager = guild.getAudioManager();
        if (manager.getSendingHandler() == null)
            manager.setSendingHandler(SoundPlayer.getInstance());

        Sound sound = SoundLibrary.getInstance().getFile(id);

        SoundPlayer.getInstance().play(sound.getPath(), () -> leaveVoiceChannel(guild));

        return true;
    }

    public boolean joinVoiceChannel(VoiceChannel vChannel, Guild guild)
    {
        log.info("Attempting to join channel: <" + vChannel.getName() + ">");

        AudioManager audioManager = guild.getAudioManager();
        if (audioManager.isConnected() || audioManager.isAttemptingToConnect())
        {
            audioManager.closeAudioConnection();
            audioManager.openAudioConnection(vChannel);
        }
        else
        {
            audioManager.openAudioConnection(vChannel);
        }

        // Wait for the connection to occur
        int i = 0;
        while (!audioManager.isConnected())
        {
            try
            {
                if (i >= MAX_CONNECTION_ATTEMPTS)
                    return false;

                Thread.sleep(100);
                i++;
            }
            catch (InterruptedException ie)
            {
                log.warn("InterruptedException while attempting to connect to voice channel, aborting.");
                return false;
            }
        }

        log.info("Successfully joined channel: <" + vChannel.getName() + ">");

        return true;
    }

    public boolean leaveVoiceChannel(Guild guild)
    {
        try
        {
            guild.getAudioManager().closeAudioConnection();

            ReleaseLock();
        }
        catch (IllegalStateException iex)
        {
            // For some reason, the new JDA library screws up on the first leave
            //leaveVoiceChannel(guild);
            guild.getAudioManager().closeAudioConnection();
        }
        catch (Exception ex)
        {
            return false;
        }

        return true;
    }

    public void ClearCommands()
    {
        this.commandQueue = new LinkedList<>();
    }

    //region Getters

    public JDA getBot()
    {
        return bot;
    }

    //endregion
}
