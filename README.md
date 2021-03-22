# s3tar

Tar gzipped uploader example using MinIO extended "PutObjectExtract" API

Following example tar gzips content from `/usr/sbin` to configured MinIO
endpoint for auto extraction of the uploaded content. This example shows
how batching lots of objects can be uploaded at once in a single archive.
```
mvn compile exec:java -Dexec.mainClass="io.minio.s3upload.Upload" -Dexec.arguments="/usr/sbin"
```

PutObjectExtract supports tar stream upto 5TiB in size, but users are
advised to use upto let's say 10,000 objects per tar stream for optimal
i/o. The stream is extracted as the stream is read from the client.

Following code provides an example of how an Input/Output stream implemented
in this project can be used to upload to MinIO. Following example hardcodes
most of the values - you may need to change these locally to test.
```java
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
```
