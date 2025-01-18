package pucpr.edu.avatar_generator.services;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.constant.Constable;
import java.math.BigInteger;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
@RequiredArgsConstructor
public class AvatarService {
    Logger logger = LogManager.getLogger(this.getClass().getName());

    private final S3Service s3Service;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${aws.name}")
    private String BUCKET_NAME;

    @Value("${gravatar.default.email}")
    private String EMAIL_DEFAULT;

    @Value("${gravatar.default.url}")
    private String URL_DEFAULT;

    @Value("${gravatar.default.image}")
    private String IMG_DEFAULT;

    public String getOrCreateAvatar(String userId, String email, String name) {

        String avatarKey = String.format("avatars/%s.png", userId);

        if (s3Service.doesObjectExist(BUCKET_NAME, avatarKey)) {
            logger.info("Avatar already exists in S3 ({})", avatarKey);
            return s3Service.getObjectUrl(BUCKET_NAME, avatarKey);
        }

        String uriGravatar = URL_DEFAULT+ md5(email) + "?d=none";

        if(email.equals(EMAIL_DEFAULT)) uriGravatar = URL_DEFAULT + IMG_DEFAULT;

        HttpResponse<byte[]> response = fetchAvatar(uriGravatar);

        byte[] avatarData;

        if (response.statusCode() == 200) {
            logger.info("Avatar found in Gravatar ({})", uriGravatar);
            avatarData = response.body();
        } else {
            final String uiAvatarUrl = "https://ui-avatars.com/api/?name=" + URLEncoder.encode(name, StandardCharsets.UTF_8) + "&background=random&format=png";
            response = fetchAvatar(uiAvatarUrl);
            if (response.statusCode() == 200) {
                logger.info("Avatar found in UI ({})", uiAvatarUrl);
                avatarData = response.body();
            } else {
                throw new RuntimeException("Unable to get avatar image.");
            }
        }

        s3Service.uploadObject(BUCKET_NAME, avatarKey, avatarData, "image/png");

        logger.info("Avatar uploaded to S3 ({})", avatarKey);

        return s3Service.getObjectUrl(BUCKET_NAME, avatarKey);
    }

    public String deleteAndRecreateAvatar(String userId) {
        final String avatarKey = String.format("avatars/%s.png", userId);

        s3Service.deleteObject(BUCKET_NAME, avatarKey);

        logger.info("Avatar deleted from S3 ({})", avatarKey);

        return getOrCreateAvatar(userId, EMAIL_DEFAULT, "Default User");
    }

    private HttpResponse<byte[]> fetchAvatar(String url) {
        try {
            return httpClient.send(HttpRequest.newBuilder().uri(URI.create(url)).build(), HttpResponse.BodyHandlers.ofByteArray());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error during get avatar: " + e.getMessage(), e);
        }
    }

    private String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(input.trim().toLowerCase().getBytes(StandardCharsets.UTF_8));
            return String.format("%032x", new BigInteger(1, hash));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed process MD5", e);
        }
    }
}


