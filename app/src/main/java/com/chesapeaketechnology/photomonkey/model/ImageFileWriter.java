package com.chesapeaketechnology.photomonkey.model;

import android.graphics.ImageFormat;
import android.net.Uri;

import androidx.camera.core.ImageProxy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.chesapeaketechnology.photomonkey.PhotoMonkeyConstants.*;

import timber.log.Timber;

/**
 * Write an {@link ImageProxy} to the external media directory.  This is a file system
 * write operation that bypasses media APIs.
 *
 * @since 0.1.0
 */
public class ImageFileWriter extends AImageWriter
{
    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    private final FileNameGenerator fileNameGenerator;

    public ImageFileWriter(FileNameGenerator fileNameGenerator)
    {
        this.fileNameGenerator = fileNameGenerator;
    }

    /**
     * Asynchronously, write {@link ImageProxy} to the specified file.
     * Currently, only supports {@link ImageFormat#JPEG}
     *
     * @param image the {@link ImageProxy} to write
     * @return The URI of the file that was written to.
     */
    @Override
    public Uri write(ImageProxy image) throws FormatNotSupportedException, WriteException
    {
        if (image.getFormat() == ImageFormat.JPEG)
        {
            File toFile = fileNameGenerator.generate();
            try
            {
                Callable<Void> writeTask = () -> {
                    ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                    // Rewind to make sure it is at the beginning of the buffer
                    buffer.rewind();
                    byte[] data = new byte[buffer.capacity()];
                    buffer.get(data);
                    try (FileOutputStream output = new FileOutputStream(toFile))
                    {
                        output.write(data);
                    } catch (IOException e)
                    {
                        Timber.e(e, "Error writing image data to file");
                    } finally
                    {
                        image.close();
                    }
                    return null;
                };

                Future<Void> result = executorService.submit(writeTask);
                result.get(SINGLE_FILE_IO_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                return Uri.fromFile(toFile);
            } catch (ExecutionException | InterruptedException | TimeoutException e)
            {
                throw new WriteException("Unable to save image", e.getCause());
            }
        } else
        {
            throw new FormatNotSupportedException(String.format("Format [%d] is not supported.", image.getFormat()));
        }
    }
}
