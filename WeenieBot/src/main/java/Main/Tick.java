package Main;

import java.util.TimerTask;

public class Tick extends TimerTask
{
    @Override
    public void run()
    {
        if (!WeenieBot.getInstance().isLocked())
            WeenieBot.getInstance().processCommand();
    }
}
