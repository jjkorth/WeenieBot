package Commands;

import Audio.SoundPlayer;
import Main.WeenieBot;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class VolumeCommand extends Command
{
    public VolumeCommand(String user, String message, Event event)
    {
        super(user, message, event, CommandType.VOLUME);
    }

    @Override
    public void processCommand()
    {
        MessageReceivedEvent e = (MessageReceivedEvent)event;

        String[] temp = message.split(" ");
        if (temp.length < 2)
        {
            WeenieBot.log.warn("No argument supplied for volume command.");
            WeenieBot.getInstance().sendMessage(e, "Please supply a value to set the volume to.");
            return;
        }

        try
        {
            // Bound the value to <0-100>
            int tempV = Integer.parseInt(temp[1]);
            if (tempV < 0)
            {
                tempV = 0;
            }
            else if (tempV > 100)
            {
                tempV = 100;
            }

            SoundPlayer.getInstance().setVolume(tempV);
        }
        catch (Exception ex)
        {
            WeenieBot.log.warn("Invalid argument supplied for volume command.\r\n\t" + ex.toString());
            WeenieBot.getInstance().sendMessage(e, "Please supply a valid volume value.");
        }
        finally
        {
            WeenieBot.getInstance().ReleaseLock();
        }
    }
}
