package io.minio.s3tar;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.List;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.IOUtils;

public class TarInputStream extends InputStream
{
    private Path source;

    private TarArchiveOutputStream tarArchiveOutputStream;

    private PipedInputStream pipedInputStream;

    private Thread thread;

    public TarInputStream(Path source)
    {
        this.source = source;
    }

    @Override
    public int read() throws IOException
    {
        if (tarArchiveOutputStream == null)
        {
            pipedInputStream = new PipedInputStream();
            PipedOutputStream pipedOutputStream = new PipedOutputStream(pipedInputStream);
            tarArchiveOutputStream = new TarArchiveOutputStream(pipedOutputStream);

            thread = new Thread(() -> {
                try
                {
                    Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
                            @Override
                            public FileVisitResult visitFile(Path file,
                                                             BasicFileAttributes attributes) {

                                // only copy files, no symbolic links
                                if (attributes.isSymbolicLink()) {
                                    return FileVisitResult.CONTINUE;
                                }

                                try {
                                    TarArchiveEntry tarEntry = new TarArchiveEntry(file.toFile(),
                                                                                   file.toString());

                                    tarArchiveOutputStream.putArchiveEntry(tarEntry);

                                    Files.copy(file, tarArchiveOutputStream);;

                                    tarArchiveOutputStream.closeArchiveEntry();

                                    System.out.printf("file : %s%n", file);

                                } catch (IOException e) {
                                    System.err.printf("Unable to tar.gz : %s%n%s%n", file, e);
                                }

                                return FileVisitResult.CONTINUE;
                            }

                            @Override
                            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                                System.err.printf("Unable to tar.gz : %s%n%s%n", file, exc);
                                return FileVisitResult.CONTINUE;
                            }

                        });
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
                finally
                {
                    try
                    {
                        tarArchiveOutputStream.finish();
                        tarArchiveOutputStream.close();
                    }
                    catch (Exception e)
                    {
                        throw new RuntimeException(e);
                    }
                }
            });
            thread.start();
        }

        return pipedInputStream.read();
    }

    @Override
    public void close() throws IOException
    {
        try
        {
            thread.join();
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }
}
