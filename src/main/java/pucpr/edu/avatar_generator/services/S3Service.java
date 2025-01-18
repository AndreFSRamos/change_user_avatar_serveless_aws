package pucpr.edu.avatar_generator.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    public boolean doesObjectExist(String bucketName, String key) {
        try {
            s3Client.headObject(builder -> builder.bucket(bucketName).key(key).build());
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }

    public void uploadObject(String bucketName, String key, byte[] data, String contentType) {
        s3Client.putObject(builder -> builder.bucket(bucketName).key(key)
                        .contentType(contentType).build(),
                RequestBody.fromBytes(data));
    }

    public void deleteObject(String bucketName, String key) {
        s3Client.deleteObject(builder -> builder.bucket(bucketName).key(key).build());
    }

    public String getObjectUrl(String bucketName, String key) {
        return s3Client.utilities().getUrl(builder -> builder.bucket(bucketName).key(key)).toExternalForm();
    }
}

