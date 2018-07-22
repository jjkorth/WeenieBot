package Audio;

public class Sound
{
    private String id;
    private String path;

    public Sound(String id, String path)
    {
        this.id     = id;
        this.path   = path;
    }

    public String getId()
    {
        return this.id;
    }

    public String getPath()
    {
        return this.path;
    }
}
