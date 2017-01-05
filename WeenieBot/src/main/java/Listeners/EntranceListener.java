package Listeners;

import net.dv8tion.jda.core.hooks.ListenerAdapter;
import CommandStuff.Command;
import CommandStuff.CommandType;
import Main.WeenieBot;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceMoveEvent;

public class EntranceListener extends ListenerAdapter 
{
	public EntranceListener()
	{
		
	}
	
	@Override
	public void onGuildVoiceJoin(GuildVoiceJoinEvent e)
	{	
		if (e.getMember().getUser().isBot())
			return;
		
		
		if (e.getChannelJoined().getName().equalsIgnoreCase("Weenie Hut General"))
			WeenieBot.getBot().addCommand(new Command(e.getMember().getUser().getName(), "hat", e, CommandType.ENTRANCE_PLAY));
	}
	
	@Override
	public void onGuildVoiceMove(GuildVoiceMoveEvent e)
	{
		if (e.getMember().getUser().isBot())
			return;
		
		
		if (e.getChannelJoined().getName().equalsIgnoreCase("Weenie Hut General"))
			WeenieBot.getBot().addCommand(new Command(e.getMember().getUser().getName(), "hat", e, CommandType.MOVE_PLAY));
	}
}
