package SoundStuff;
import java.io.File;
import java.util.HashMap;
import java.util.Random;

public class SoundLibrary 
{
	private HashMap<String, Sound> library;
	
	public SoundLibrary(String directory)
	{
		this.library = new HashMap<String, Sound>();
		
		File dir = new File(directory);
		
		if (!dir.exists())
		{
			System.out.println("Directory does not exist.");
			dir.mkdir();
		}
		else
			PopulateLibrary(dir);
	}
	
	public void PopulateLibrary(File dir)
	{
		for (File f : dir.listFiles())
		{
			if (f.isDirectory())
			{
				PopulateLibrary(f);
			}
			else
			{
				if (!f.getName().endsWith(".mp3"))
				{
					System.out.println("File: " + f.getName() + " not added. Only .mp3 files are supported.");
				}
				else
				{
					String tempID = f.getName().substring(0, f.getName().length() - 4);
					library.put(tempID, new Sound(tempID, f.getAbsolutePath()));
				}
			}
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
		return this.library;
	}
}
