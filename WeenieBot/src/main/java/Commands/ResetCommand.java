package Commands;

import Audio.SoundPlayer;
import Main.WeenieBot;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class ResetCommand extends Command
{
    public ResetCommand(String user, String message, Event event)
    {
        super(user, message, event, CommandType.RESET);
    }

    @Override
    public void processCommand()
    {
        MessageReceivedEvent e = (MessageReceivedEvent)event;

        WeenieBot.getInstance().ClearCommands();

        SoundPlayer.ResetSoundPlayer();
        e.getGuild().getAudioManager().setSendingHandler(SoundPlayer.getInstance());

        WeenieBot.getInstance().ReleaseLock();
    }
}
