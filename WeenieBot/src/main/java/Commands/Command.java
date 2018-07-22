package Commands;

import net.dv8tion.jda.core.events.Event;

public abstract class Command
{
    public String          user;
    public String          message;
    public Event           event;
    public CommandType     type;

    public Command(String user, String message, Event event, CommandType type)
    {
        this.user       = user;
        this.message    = message;
        this.event      = event;
        this.type       = type;
    }

    public abstract void processCommand();
}
