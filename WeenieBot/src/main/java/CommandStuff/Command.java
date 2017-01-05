package CommandStuff;
import net.dv8tion.jda.core.events.Event;

public class Command 
{
	private String 		user;
	private String 		message;
	private Event 		event;
	private CommandType type;
	
	public Command(String user, String message, Event event, CommandType type)
	{
		this.user 		= user;
		this.message 	= message;
		this.event 		= event;
		this.type 		= type;
	}
	
	public String getUser()
	{
		return this.user;
	}
	
	public String getMessage()
	{
		return this.message;
	}
	
	public Event getEvent()
	{
		return this.event;
	}
	
	public CommandType getType()
	{
		return this.type;
	}

}
