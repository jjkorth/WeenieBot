package Commands;

import Audio.SoundPlayer;
import Main.Utilities;
import Main.WeenieBot;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class StopCommand extends Command
{
    public StopCommand(String user, String message, Event event)
    {
        super(user, message, event, CommandType.STOP);
    }

    @Override
    public void processCommand()
    {
        MessageReceivedEvent e = (MessageReceivedEvent)event;

        SoundPlayer.getInstance().stop();

        if (e.isFromType(ChannelType.PRIVATE))
        {
            Guild guild = Utilities.getGuild(e.getAuthor());
            if (guild == null)
            {
                WeenieBot.log.warn("Could not find requesting user's guild, aborting.");
                WeenieBot.getInstance().ReleaseLock();
                return;
            }

            VoiceChannel vChannel = Utilities.getVoiceChannel(e.getAuthor(), guild);
            if (vChannel == null)
            {
                WeenieBot.log.warn("Could not find requesting user's voice channel, aborting.");
                WeenieBot.getInstance().ReleaseLock();
                return;
            }

            if (WeenieBot.getInstance().leaveVoiceChannel(guild))
            {
                WeenieBot.log.error("Could not leave the voice channel.");
            }
        }
        else
        {
            if (!WeenieBot.getInstance().leaveVoiceChannel(e.getGuild()))
            {
                WeenieBot.log.error("Could not leave the voice channel.");
            }
        }

        WeenieBot.getInstance().ReleaseLock();
    }
}
