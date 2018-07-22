package Main;

import java.io.*;
import java.util.Properties;

public class PropertyLoader
{
    private static Properties properties = null;

    public static Properties getProperties()
    {
        if (properties == null)
        {
            properties = PropertyLoader.LoadProperties();
        }

        return properties;
    }

    private static Properties LoadProperties()
    {
        WeenieBot.log.info("Loading Properties from \"prop.properties\"...");

        Properties _Properties = new Properties();

        InputStream inStream;

        try
        {
            inStream = new FileInputStream("prop.properties");
            _Properties.load(inStream);
            inStream.close();
        }
        catch (FileNotFoundException fnfe)
        {
            WeenieBot.log.warn("Could not find the prop.properties file, creating a template file, please edit the file and try again.");

            try
            {
                PrintStream propWriter = new PrintStream(new File("prop.properties"));

                propWriter.println("token=");
                propWriter.println("admin=");
                propWriter.println("directory=sounds");
                propWriter.println("toggle_char=!");
                propWriter.println("wait_time=100");
                propWriter.println("max_connection_attempts=20");

                propWriter.close();

                WeenieBot.log.warn("prop.properties file successfully created.");
            }
            catch (IOException ie)
            {
                WeenieBot.log.fatal("I/O error while trying to write to the prop.properties file, exiting: \r\n\t" + ie.getMessage());
                System.exit(-2);
            }
        }
        catch (IOException ie)
        {
            WeenieBot.log.fatal("I/O error while trying to read the prop.properties file, exiting: \r\n\t" + ie.getMessage());
            System.exit(-1);
        }

        return _Properties;
    }
}
