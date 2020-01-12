package org.spectralmemories.bloodmoon;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

public class ConfigReader implements Closeable
{
    //Todo: allow for flexible delays
    File configFile;

    public ConfigReader (File file)
    {
        configFile = file;
    }

    @Override
    public void close() throws IOException
    {

    }
}
