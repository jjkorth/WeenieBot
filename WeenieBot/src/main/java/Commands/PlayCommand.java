package Commands;

import Audio.Sound;
import Audio.SoundLibrary;
import Main.PropertyLoader;
import Main.Utilities;
import Main.WeenieBot;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class PlayCommand extends Command
{
    public PlayCommand(String user, String message, Event event)
    {
        super(user, message, event, CommandType.PLAY);
    }

    @Override
    public void processCommand()
    {
        if (this.user == null || user.isEmpty())
            user = PropertyLoader.getProperties().getProperty("admin");

        Sound sound = SoundLibrary.getInstance().getFile(message);
        if (sound == null)
        {
            WeenieBot.log.warn("Could not find the requested sound file. <" + message + ">");
            WeenieBot.getInstance().ReleaseLock();
            return;
        }

        Guild guild;
        VoiceChannel vChannel;
        MessageReceivedEvent e = (MessageReceivedEvent)event;

        // If the channel is private, we have been direct messaged
        if (e.isFromType(ChannelType.PRIVATE))
        {
            guild = Utilities.getGuild(e.getAuthor());
            if (guild == null)
            {
                WeenieBot.log.warn("Could not find requesting user's Discord, aborting...");
                WeenieBot.getInstance().ReleaseLock();
                return;
            }

            vChannel = Utilities.getVoiceChannel(e.getAuthor(), guild);
            if (vChannel == null)
            {
                WeenieBot.log.warn("Could not find requesting user's voice channel, aborting...");
                WeenieBot.getInstance().ReleaseLock();
                return;
            }
        }
        // Otherwise, just grab the guild from the message event
        else
        {
            guild = e.getGuild();
            if (guild == null)
            {
                WeenieBot.log.warn("Could not find requesting user's Discord, aborting...");
                WeenieBot.getInstance().ReleaseLock();
                return;
            }

            vChannel = e.getMember().getVoiceState().getChannel();
            if (vChannel == null)
            {
                WeenieBot.log.warn("Could not find requesting user's voice channel, aborting...");
                WeenieBot.getInstance().ReleaseLock();
                return;
            }
        }

        // Attempt to join the voice channel
        if (!WeenieBot.getInstance().joinVoiceChannel(vChannel, guild))
        {
            WeenieBot.log.warn("Could not join the requesting user's channel, aborting...");
            WeenieBot.getInstance().ReleaseLock();
            return;
        }

        // Attempt to play the sound
        if (!WeenieBot.getInstance().playSound(message, guild, vChannel))
        {
            WeenieBot.log.warn("Could not play the sound requested");
            WeenieBot.getInstance().leaveVoiceChannel(guild);
        }
    }
}
