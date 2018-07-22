package Commands;

import Audio.SoundLibrary;
import Main.WeenieBot;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class ListCommand extends Command
{
    public ListCommand(String user, String message, Event event)
    {
        super(user, message, event, CommandType.LIST);
    }

    @Override
    public void processCommand()
    {
        MessageReceivedEvent e = (MessageReceivedEvent)event;

        String temp = "Sound Files Available:\r\n\r\n\t";

        // Add each key to the message
        for (String s : SoundLibrary.getInstance().getLibrary().keySet())
            temp += s + "\r\n\t";

        WeenieBot.getInstance().sendMessage(e, temp);

        WeenieBot.getInstance().ReleaseLock();
    }
}
