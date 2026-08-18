package com.runningolle.domain.community.storage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LocalFileStorageService implements FileStorageService {

    private final Path rootPath;
    private final String publicBasePath;

    public LocalFileStorageService(
            @Value("${app.storage.local-dir:uploads}") String localDir,
            @Value("${app.storage.public-base-path:/uploads}") String publicBasePath
    ) throws IOException {
        this.rootPath = Paths.get(localDir).toAbsolutePath().normalize();
        this.publicBasePath = publicBasePath.startsWith("/") ? publicBasePath : "/" + publicBasePath;
        Files.createDirectories(this.rootPath);
    }

    @Override
    public List<String> store(List<MultipartFile> files) throws IOException {
        List<String> storedUrls = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }

            String fileName = UUID.randomUUID() + extractExtension(file.getOriginalFilename());
            Path destination = rootPath.resolve(fileName).normalize();

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
            }

            storedUrls.add(publicBasePath + "/" + fileName);
        }

        return storedUrls;
    }

    @Override
    public void deleteByUrl(String fileUrl) {
        if (!StringUtils.hasText(fileUrl)) {
            return;
        }

        String pathValue = fileUrl;
        try {
            if (fileUrl.startsWith("http://") || fileUrl.startsWith("https://")) {
                pathValue = URI.create(fileUrl).getPath();
            }
        } catch (RuntimeException ignored) {
            return;
        }

        if (!pathValue.startsWith(publicBasePath + "/")) {
            return;
        }

        String fileName = pathValue.substring((publicBasePath + "/").length());
        Path target = rootPath.resolve(fileName).normalize();
        if (!target.startsWith(rootPath)) {
            return;
        }

        try {
            Files.deleteIfExists(target);
        } catch (IOException ignored) {
        }
    }

    private String extractExtension(String fileName) {
        if (!StringUtils.hasText(fileName) || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.'));
    }
}
