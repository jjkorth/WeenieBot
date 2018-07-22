package Commands;

import Main.WeenieBot;
import net.dv8tion.jda.core.events.Event;

public class ShutdownCommand extends Command
{
    public ShutdownCommand(String user, String message, Event event)
    {
        super(user, message, event, CommandType.SHUTDOWN);
    }

    @Override
    public void processCommand()
    {
        try
        {
            WeenieBot.getInstance().getBot().shutdown();
            WeenieBot.log.info("Bot successfully shutdown.");
        }
        catch (Exception ex)
        {
            // Ignore exceptions, we are shutting down anyway.
        }
        finally
        {
            System.exit(0);
        }
    }
}
