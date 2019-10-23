package dotnet4j.io;

/**
 * Created by ft on 28.03.17.
 */
public final class Directory
{
    //TODO: Make this return a DirectoryInfo
    public static void createDirectory(String name)
    {
        new java.io.File(name).mkdir();
    }

    public static void deleteDirectory(String name)
    {
        java.io.File victim = new java.io.File(name);
        if(!victim.isDirectory())
        {
            throw new IOException(name + " is not a directory!");
        }
        boolean result = victim.delete();
        if (result == false)
        {
            throw new IOException("Delete directory failed.");
        }
    }
}
