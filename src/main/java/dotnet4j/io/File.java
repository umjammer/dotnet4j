package dotnet4j.io;

import org.jetbrains.annotations.NotNull;

/**
 * Created by FT on 28.11.14.
 */
public final class File
{
    @NotNull
    public static FileStream create(String fname)
    {
        return new FileStream(fname,FileMode.Create);
    }

    public static void delete(String fname)
    {
        java.io.File target = new java.io.File(fname);
        if (!target.exists())
        {
            throw new FileNotFoundException(fname);
        }

        if (target.isDirectory())
        {
            throw new IOException(fname + " is not a file!");
        }

        target.delete();
    }

    public static boolean exists(String fname)
    {
        java.io.File target = new java.io.File(fname);
        if (target.isDirectory())
        {
            return false;
        }
        return target.exists();
    }

    @NotNull
    public static FileStream openRead(String fname)
    {
        return new FileStream(fname,FileMode.Open,FileAccess.Read,FileShare.Read);
    }

    @NotNull
    public static FileStream openWrite(String fname)
    {
        return new FileStream(fname,FileMode.OpenOrCreate,FileAccess.Write,FileShare.None);
    }

    public static byte[] readAllBytes(String fname)
    {
        FileStream source = openRead(fname);
        byte[] result = new byte[(int)source.getLength()];
        if (source.read(result,0,(int)source.getLength()) != source.getLength())
        {
            throw new IOException("Could not fully read " + fname + ".");
        }
        source.close();
        return result;

    }

    public static void writeAllBytes(String fname,byte[] data)
    {
        FileStream target = openWrite(fname);
        target.write(data,0,data.length);
        target.flush();
        target.close();
    }

    @NotNull
    public static FileStream open(String path, FileMode fm, FileAccess fa)
    {
        return new FileStream(path,fm,fa);
    }
}
