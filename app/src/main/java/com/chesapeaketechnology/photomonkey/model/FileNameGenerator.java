package com.chesapeaketechnology.photomonkey.model;

import android.content.Context;

import com.chesapeaketechnology.photomonkey.PhotoMonkeyApplication;
import com.chesapeaketechnology.photomonkey.PhotoMonkeyConstants;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

import timber.log.Timber;

/**
 * Generates file names and determines the path where images are stored if we are not using
 * the MediaStore. MediaStore usage is configured in {@link com.chesapeaketechnology.photomonkey.PhotoMonkeyFeatures}.
 *
 * @since 0.2.0
 */
public class FileNameGenerator
{
    public static final String PREFIX = PhotoMonkeyConstants.PHOTO_MONKEY_PHOTO_NAME_PREFIX;
    public static final String FILE_NAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS";
    public static final String VOLUME = "PhotoMonkey";
    public static final String EXTENSION = ".jpg";
    private final File outputDirectory;

    public FileNameGenerator()
    {
        outputDirectory = getOutputDirectory();
    }

    private File getOutputDirectory()
    {
        Context appContext = PhotoMonkeyApplication.getContext();
        File[] mediaDirs = appContext.getExternalMediaDirs();
        if (mediaDirs != null && mediaDirs.length > 0)
        {
            File mediaDir = Arrays.stream(mediaDirs).filter(Objects::nonNull).findFirst().orElse(null);
            if (mediaDir != null && mediaDir.exists())
            {
                return mediaDir;
            }
        }
        return appContext.getFilesDir();
    }

    /**
     * Get the directory where images are stored.
     *
     * @return
     */
    public File getRootDirectory()
    {
        Path outputPath = Paths.get(String.valueOf(outputDirectory.toPath()), VOLUME);
        File outputFolder = outputPath.toFile();
        boolean outputFolderExists = outputFolder.exists();
        if (!outputFolderExists)
        {
            outputFolderExists = outputFolder.mkdirs();
            if (!outputFolderExists)
            {
                outputFolder = outputDirectory;
            }
        }
        return outputFolder;
    }

    /**
     * Generate a unique, timestamped, file name and a {@link File} object with the full path to the file.
     *
     * @return
     */
    public File generate()
    {
        File outputFolder = getRootDirectory();
        File fileName = new File(outputFolder, PREFIX + new SimpleDateFormat(FILE_NAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + EXTENSION);
        Timber.d("Generated path: %s", fileName.getAbsolutePath());
        return fileName;
    }
}
