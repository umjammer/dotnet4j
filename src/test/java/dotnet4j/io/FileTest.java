package dotnet4j.io;

import java.util.Random;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

import dotnet4j.io.Directory;
import dotnet4j.io.File;
import dotnet4j.io.FileAccess;
import dotnet4j.io.FileMode;
import dotnet4j.io.FileNotFoundException;
import dotnet4j.io.FileStream;
import dotnet4j.io.IOException;

/**
 * Created by ft on 28.03.17.
 */
public class FileTest
{
    private final String filename = "test.bin";
    private final String dirname = "testdir";

    @Test
    public void createAndDeleteFile() throws Exception {
        FileStream fileStream = File.create(filename);
        fileStream.close();
        File.delete(filename);
    }

    @Test
    public void deleteFileThatDoesNotExistsShouldFail()
    {
        try
        {
            File.delete("inexistant.file");
            fail();
        }
        catch(FileNotFoundException fnfe)
        {
            return;
        }
    }

    @Test
    public void fileClassShouldNotDeleteDirectories()
    {
        Directory.createDirectory(dirname);
        try
        {
            File.delete(dirname);
            fail();
        }
        catch(IOException ioe)
        {
            if (ioe instanceof FileNotFoundException) fail("Got FileNotFound instead of IO!");
            Directory.deleteDirectory(dirname);
            return;
        }
    }

    @Test
    public void fileExists()
    {
        File.create(filename).close();
        if (!File.exists(filename)) fail("File.exists reported false even though it was created.");

        File.delete(filename);
        if (File.exists(filename)) fail("The file exists even though it was deleted.");
    }

    @Test
    public void fileDoesNotExistIfItIsADirectory()
    {
        Directory.createDirectory(dirname);

        boolean fileExists = File.exists(dirname);

        Directory.deleteDirectory(dirname);

        assertFalse(fileExists);
    }

    @Test
    public void openRead()
    {
        byte[] buffer = new byte[1024];
        new Random().nextBytes(buffer);

        File.writeAllBytes(filename,buffer);

        FileStream fs = File.openRead(filename);
        byte[] cmpBuffer = new byte[1024];
        assert fs.read(cmpBuffer,0,1024) == 1024;
        fs.close();

        File.delete(filename);

        assertArrayEquals(buffer,cmpBuffer);
    }

    @Test
    public void readAllBytes()
    {
        byte[] buffer = new byte[1024];
        new Random().nextBytes(buffer);

        File.writeAllBytes(filename,buffer);

        byte[] cmpBuffer = File.readAllBytes(filename);

        File.delete(filename);

        assertArrayEquals(buffer,cmpBuffer);
    }

    @Test
    public void open()
    {
        File.openWrite(filename).close();
        File.open(filename,FileMode.OpenOrCreate,FileAccess.Read).close();

        File.delete(filename);
    }
}
