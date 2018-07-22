package Commands;

import Audio.SoundLibrary;
import Main.WeenieBot;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class RandomCommand extends Command
{
    public RandomCommand(String user, String message, Event event)
    {
        super(user, message, event, CommandType.RANDOM);
    }

    @Override
    public void processCommand()
    {
        MessageReceivedEvent e = (MessageReceivedEvent)event;

        String temp = SoundLibrary.getInstance().getRandomKey();
        if (temp == null || temp.isEmpty())
        {
            WeenieBot.getInstance().ReleaseLock();
            WeenieBot.log.warn("No sound files in the sound library, cannot play a random sound.");
            return;
        }

        // Just re-use the play command with the randomly selected sound
        PlayCommand playCommand = new PlayCommand(user, temp, event);
        playCommand.processCommand();
    }
}
