package com.wookie.demo.profile;

import com.amazonaws.services.licensemanager.model.Metadata;
import com.wookie.demo.bucket.BucketName;
import com.wookie.demo.filestore.FileStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
public class UserProfileService {
    private final UserProfileDataAccessService userProfileDataAccessService;
    private final FileStore fileStore;

    @Autowired
    public UserProfileService(UserProfileDataAccessService userProfileDataAccessService, FileStore fileStore) {
        this.userProfileDataAccessService = userProfileDataAccessService;
        this.fileStore = fileStore;
    }

    List<UserProfile> getUserProfiles() {
        return userProfileDataAccessService.getUserProfiles();
    }

    public void uploadProfileImage(UUID userProfileId, MultipartFile file) {
        if (!file.isEmpty()) {
            if (file.getContentType().split("/")[0].equals("image")) {
                UserProfile user = getUserProfile(userProfileId);

                Map<String, String> optionalmetadata = new HashMap<>();
                optionalmetadata.put("Content-Type", file.getContentType());
                optionalmetadata.put("Content-Length", String.valueOf(file.getSize()));
                String pathToSave = BucketName.PROFILE_IMAGE.getBucketName() + "/app/resources/images" + user.getUserProfileId();
                String filename = file.getOriginalFilename() + UUID.randomUUID().toString();
                try {
                    fileStore.save(pathToSave, filename, Optional.of(optionalmetadata), file.getInputStream());
                    user.setUserProfileImageLink(filename);
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }

            }
            else throw new IllegalStateException("Cannot upload not a image file");
        }
        else throw new IllegalStateException("File cannot be empty");
    }

    public byte[] downloadUserProfileImage(UUID userProfileId) {
        UserProfile user = getUserProfile(userProfileId);
        String path = BucketName.PROFILE_IMAGE.getBucketName() + "/app/resources/images" + user.getUserProfileId();
        return user.getUserProfileImageLink()
                .map(key -> fileStore.download(path, key))
                .orElse(new byte[0]);
    }

    private UserProfile getUserProfile(UUID userProfileId) {
        List<UserProfile> remp = userProfileDataAccessService.getUserProfiles();
        return userProfileDataAccessService.getUserProfiles().stream()
                .filter(userProfile -> userProfile.getUserProfileId().equals(userProfileId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Bad user id, cannot find in DB"));
    }


}
