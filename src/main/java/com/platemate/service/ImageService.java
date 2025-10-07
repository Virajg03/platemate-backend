package com.platemate.service;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.platemate.enums.ImageType;
import com.platemate.model.Image;
import com.platemate.repository.ImageRepository;
import com.platemate.utils.ImageResponse;
import com.platemate.utils.ImageUtils;

@Service
public class ImageService {

    private final ImageRepository imageRepository;

    public ImageService(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    public Image saveImage(MultipartFile file, ImageType imageType, Long ownerId) throws IOException {
        String base64 = ImageUtils.toBase64(file);

        Image image = new Image(
            file.getOriginalFilename(),
            file.getContentType(),
            base64,
            imageType,
            ownerId
        );

        return imageRepository.save(image);
    }

    public ImageResponse getImage(Long imageId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found"));

        return new ImageResponse(
                image.getFileType(),
                ImageUtils.fromBase64(image.getBase64Data())
        );
    }
}

