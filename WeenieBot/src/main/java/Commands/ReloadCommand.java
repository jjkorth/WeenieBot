package Commands;

import Audio.SoundLibrary;
import Main.WeenieBot;
import net.dv8tion.jda.core.events.Event;

public class ReloadCommand extends Command
{
    public ReloadCommand(String user, String message, Event event)
    {
        super(user, message, event, CommandType.RELOAD_LIBRARY);
    }

    @Override
    public void processCommand()
    {
        SoundLibrary.ReloadLibrary();
        WeenieBot.getInstance().ReleaseLock();
    }
}
