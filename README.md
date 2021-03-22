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
