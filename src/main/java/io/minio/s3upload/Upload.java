package io.minio.s3upload;

import org.apache.commons.io.IOUtils;

import io.minio.s3tar.TarInputStream;
import io.minio.s3tar.S3OutputStream;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;

import java.nio.file.*;

import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

public class Upload {
    private static String bucketName = "testbucket";
    private static String objectName = "testobject.tar.gz";

    public static void main(String[] args) throws IOException {
        AWSCredentials credentials = new BasicAWSCredentials("minio", "minio123");
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setSignerOverride("AWSS3V4SignerType");

        AmazonS3 s3 = AmazonS3ClientBuilder
            .standard()
            .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:9000", Regions.US_EAST_1.name()))
            .withPathStyleAccessEnabled(true)
            .withClientConfiguration(clientConfiguration)
            .withCredentials(new AWSStaticCredentialsProvider(credentials))
            .build();

        // Wrap the S3OutputStream in Apache Commons Compress' GzipCompressorOutputStream
        try (OutputStream os = new GzipCompressorOutputStream(new S3OutputStream(s3, bucketName, objectName)))
            {
                // The input stream from S3 is already a .tar
                try (InputStream is = new TarInputStream(Paths.get(args[0])))
                    {
                        // Using Apache Commons IO to stream IS to an OS
                        IOUtils.copy(is, os);
                    }
            }
    }
}
