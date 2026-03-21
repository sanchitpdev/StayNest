package com.staynest.controller;

import com.staynest.entity.PropertyImage;
import com.staynest.entity.User;
import com.staynest.enums.ImageType;
import com.staynest.service.PropertyImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for property image management
 */
@RestController
@RequestMapping("/images")
public class PropertyImageController {

    @Autowired
    private PropertyImageService propertyImageService;

    /**
     * Add image to property
     */
    @PostMapping("/properties/{propertyId}")
    public ResponseEntity<PropertyImage> addPropertyImage(
            @PathVariable UUID propertyId,
            @RequestBody Map<String, Object> request,
            Authentication authentication){
        User user = (User) authentication.getPrincipal();

        String imageUrl = (String) request.get("imageUrl");
        String imageTypeStr = (String) request.get("imageType");
        Boolean isPrimary = (Boolean) request.get("isPrimary");

        ImageType imageType = imageTypeStr != null
                ? ImageType.valueOf(imageTypeStr)
                : ImageType.OTHER;

        PropertyImage image = propertyImageService.addPropertyImage(
                propertyId,imageUrl,imageType,isPrimary,user.getUserId()
        );

        return new ResponseEntity<>(image, HttpStatus.CREATED);
    }

    /**
     * Add image to unit
     */
    @PostMapping("/units/{unitId}")
    public ResponseEntity<PropertyImage> addUnitImage(
            @PathVariable UUID unitId,
            @RequestBody Map<String, Object> request,
            Authentication authentication){
        User user = (User) authentication.getPrincipal();

        String imageUrl = (String) request.get("imageUrl");
        String imageTypeStr = (String) request.get("imageType");

        ImageType imageType = imageTypeStr != null
                ? ImageType.valueOf(imageTypeStr)
                : ImageType.OTHER;

        PropertyImage image = propertyImageService.addUnitImage(
                unitId,imageUrl, imageType, user.getUserId()
        );

        return new ResponseEntity<>(image, HttpStatus.CREATED);
    }

    /**
     * Delete image
     */
    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> deleteImage(
            @PathVariable UUID imageId,
            Authentication authentication){
        User user = (User) authentication.getPrincipal();
        propertyImageService.deleteImage(imageId,user.getUserId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all images for a property
     */
    @GetMapping("/properties/{propertyId}")
    public ResponseEntity<List<PropertyImage>> getPropertyImages(@PathVariable UUID propertyId){
        List<PropertyImage> images = propertyImageService.getPropertyImage(propertyId);
        return ResponseEntity.ok(images);
    }

    /**
     * Get all images for unit.
     */
    @GetMapping("/units/{unitId}")
    public ResponseEntity<List<PropertyImage>> getUnitImages(@PathVariable UUID unitId){
        List<PropertyImage> images = propertyImageService.getUnitImages(unitId);
        return ResponseEntity.ok(images);
    }
}
