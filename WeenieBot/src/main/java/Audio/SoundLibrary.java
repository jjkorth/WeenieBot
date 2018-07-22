package Audio;

import Main.PropertyLoader;
import Main.WeenieBot;

import java.io.File;
import java.util.HashMap;
import java.util.Random;

public class SoundLibrary
{
    private static SoundLibrary instance = null;

    public static SoundLibrary getInstance()
    {
        if (instance == null)
        {
            String path = PropertyLoader.getProperties().getProperty("directory", "sounds");
            instance = new SoundLibrary(path);
        }

        return instance;
    }

    public static void ReloadLibrary()
    {
        String path = PropertyLoader.getProperties().getProperty("directory", "sounds");
        instance = new SoundLibrary(path);
    }

    private HashMap<String, Sound> library;

    private SoundLibrary(String path)
    {
        WeenieBot.log.info("Initializing Sound Library...");

        library = new HashMap<>();

        File dir = new File(path);

        if (!dir.exists())
        {
            WeenieBot.log.warn("Directory does not exist: <" + path + "> Attempting to create...");
            dir.mkdir();
        }
        else
        {
            populateLibrary(dir);
        }
    }

    private void populateLibrary(File dir)
    {
        try {
            for (File f : dir.listFiles()) {
                if (f.isDirectory())
                {
                    populateLibrary(f);
                }
                else
                    {
                    if (!f.getName().endsWith(".mp3"))
                    {
                        WeenieBot.log.warn("File: " + f.getName() + " not added. Only .mp3 files are supported.");
                    }
                    else
                    {
                        String id = f.getName().substring(0, f.getName().length() - 4);
                        library.put(id, new Sound(id, f.getAbsolutePath()));
                    }
                }
            }
        }
        catch (Exception e)
        {
            WeenieBot.log.fatal("Could not load the sound library, exiting.\r\n\t" + e.getMessage());
        }
    }

    public Sound getFile(String key)
    {
        if (!library.containsKey(key))
            return null;

        return library.get(key);
    }

    public String getRandomKey()
    {
        if (library.isEmpty())
            return null;

        Random r = new Random();

        String[] keys = new String[library.keySet().size()];
        library.keySet().toArray(keys);

        return keys[r.nextInt(keys.length)];
    }

    public HashMap<String, Sound> getLibrary()
    {
        return library;
    }
}
