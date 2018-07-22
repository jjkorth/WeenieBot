package Main;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;

public class Utilities
{
    public static Guild getGuild(User user)
    {
        for (Guild g : WeenieBot.getInstance().getBot().getGuilds())
        {
            for (VoiceChannel v : g.getVoiceChannels())
            {
                for (Member m : v.getMembers())
                {
                    if (m.getUser().getId().equalsIgnoreCase(user.getId()))
                    {
                        return g;
                    }
                }
            }
        }

        return null;
    }

    public static VoiceChannel getVoiceChannel(User user, Guild guild)
    {
        for (VoiceChannel v : guild.getVoiceChannels())
        {
            for (Member m : v.getMembers())
            {
                if (m.getUser().getId().equalsIgnoreCase(user.getId()))
                {
                    return v;
                }
            }
        }

        return null;
    }
}
