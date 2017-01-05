package Listeners;
import CommandStuff.Command;
import CommandStuff.CommandType;
import Main.WeenieBot;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.utils.SimpleLog;

public class ChatListener extends ListenerAdapter
{
	private String 		toggleChar;
	private int			maxMessageSize;
	private SimpleLog 	log;
	
	public ChatListener(String toggleChar, int maxMessageSize)
	{
		this.toggleChar 	= toggleChar;
		this.maxMessageSize = maxMessageSize;
		log = SimpleLog.getLog("ChatListener");
	}
	
	@Override
	public void onMessageReceived(MessageReceivedEvent e)
	{		
		String user 	= e.getAuthor().getName();
		String message 	= e.getMessage().getContent().toLowerCase();
		
		if (e.getAuthor().isBot())
			return;
		else if (!message.startsWith(toggleChar))
			return;
		else if (message.length() > maxMessageSize)
		{
			log.info("Message has exceeded max length. <" + maxMessageSize + ">");
			WeenieBot.getBot().sendMessage(e, "Message has exceeded max length. <" + maxMessageSize + ">");
			return;
		}
		
		log.info(user + " gave command: " + message);
		
		// Strip the toggle character off the front.
		message = message.substring(1);
		
		
		// Figure out which command we've been sent.
		if (message.startsWith("shutdown"))
		{
			deleteMessage(e);
			
			if (user.equalsIgnoreCase(WeenieBot.getBot().getProperties().getProperty("admin")))
				WeenieBot.getBot().proccessUrgentCommand(new Command(user, message, e, CommandType.SHUTDOWN));
			else
				log.info(user + " is not an admin.");
		}
		else if (message.startsWith("stop"))
			WeenieBot.getBot().proccessUrgentCommand(new Command(user, message, e, CommandType.STOP));
		else if (message.startsWith("reset"))
			WeenieBot.getBot().proccessUrgentCommand(new Command(user, message, e, CommandType.RESET));
		else if (message.startsWith("reload"))
			WeenieBot.getBot().addCommand(new Command(user, message, e, CommandType.RELOAD_LIBRARY));
		else if (message.startsWith("random"))
			WeenieBot.getBot().addCommand(new Command(user, message, e, CommandType.RANDOM));
		else if (message.startsWith("volume"))
			WeenieBot.getBot().addCommand(new Command(user, message, e, CommandType.VOLUME));
		else if (message.startsWith("list"))
			WeenieBot.getBot().addCommand(new Command(user, message, e, CommandType.LIST));
		else if (message.length() > 0)
			WeenieBot.getBot().addCommand(new Command(user, message, e, CommandType.PLAY));
		else
			log.info("No command supplied.");
			
		deleteMessage(e);
	}
	
	
	// Function that deletes the command message sent.
	private void deleteMessage(MessageReceivedEvent e)
	{
		if (!e.isFromType(ChannelType.PRIVATE))
		{
			try
			{
				e.getMessage().deleteMessage().queue();
			}
			catch (PermissionException pe)
			{
				log.info("WeenieBot does not have permission to delete messages.");
			}
		}
	}
}
