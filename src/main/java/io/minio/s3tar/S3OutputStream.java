package io.minio.s3tar;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Phaser;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;

public class S3OutputStream extends OutputStream
{
    private AmazonS3 s3;

    private String bucketName;

    private String objectName;

    private ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    public S3OutputStream(AmazonS3 s3, String bucketName, String objectName)
    {
        this.s3 = s3;
        this.bucketName = bucketName;
        this.objectName = objectName;
    }

    @Override
    public void write(int b) throws IOException
    {
        buffer.write(b);
    }

    @Override
    public void close() throws IOException
    {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.addUserMetadata("snowball-auto-extract", "true");
        s3.putObject(new PutObjectRequest(bucketName, objectName, new ByteArrayInputStream(buffer.toByteArray()), objectMetadata));
        buffer.reset();
    }
}
