package Listeners;

import Commands.*;
import Main.PropertyLoader;
import Main.WeenieBot;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class ChatListener extends ListenerAdapter
{
    private String  toggleChar;
    private int     maxMessageSize;

    public ChatListener(String toggleChar, int maxMessageSize)
    {
        this.toggleChar         = toggleChar;
        this.maxMessageSize     = maxMessageSize;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent e)
    {
        String user = e.getAuthor().getId();
        String message = e.getMessage().getContentStripped().toLowerCase();

        if (e.getAuthor().isBot())
            return;
        else if (!message.startsWith(toggleChar))
            return;
        else if (message.length() > maxMessageSize)
        {
            WeenieBot.log.warn("Message has exceeded max length: <" + maxMessageSize + ">");
            WeenieBot.getInstance().sendMessage(e, "Message has exceeded max length: <" + maxMessageSize + ">");
            return;
        }

        WeenieBot.log.info(user + " [" + e.getAuthor().getName() + "] gave command: " + message);

        // Strip the toggle character off the front of the message
        message = message.substring(1);

        // Special case for shutdown command
        if (message.startsWith("shutdown"))
        {
            if (user.equals(PropertyLoader.getProperties().getProperty("admin")))
            {
                deleteMessage(e);
                WeenieBot.getInstance().processUrgentCommand(new ShutdownCommand(user, message, e));
            }
            else
            {
                WeenieBot.log.warn(user + " [" + e.getAuthor().getName() + "] is not an admin. Ignoring command.");
            }
        }
        else if (message.startsWith("stop"))
            WeenieBot.getInstance().processUrgentCommand(new StopCommand(user, message, e));
        else if (message.startsWith("reset"))
            WeenieBot.getInstance().processUrgentCommand(new ResetCommand(user, message, e));
        else if (message.startsWith("reload"))
            WeenieBot.getInstance().addCommand(new ReloadCommand(user, message, e));
        else if (message.startsWith("random"))
            WeenieBot.getInstance().addCommand(new RandomCommand(user, message, e));
        else if (message.startsWith("volume"))
            WeenieBot.getInstance().addCommand(new VolumeCommand(user,message, e));
        else if (message.startsWith("list"))
            WeenieBot.getInstance().addCommand(new ListCommand(user, message, e));
        else if (message.length() > 0)
            WeenieBot.getInstance().addCommand(new PlayCommand(user, message, e));
        else
            WeenieBot.log.warn("No command supplied...");

        deleteMessage(e);
    }

    private void deleteMessage(MessageReceivedEvent e)
    {
        if (!e.isFromType(ChannelType.PRIVATE))
        {
            try
            {
                e.getMessage().delete().complete();
            }
            catch (PermissionException pe)
            {
                WeenieBot.log.warn("Bot does not have permission to delete messages.");
            }
        }
    }
}
