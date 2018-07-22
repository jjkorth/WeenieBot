package Main;

import java.util.Properties;

public class Main
{
    private static WeenieBot instance;

    public static void main(String[] args)
    {
        Properties props = System.getProperties();
        props.setProperty("log4j.configurationFile", "log4j2.properties");
        instance = WeenieBot.getInstance();
    }
}
