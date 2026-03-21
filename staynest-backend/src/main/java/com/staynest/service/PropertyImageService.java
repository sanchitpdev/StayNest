package com.staynest.service;

import com.staynest.entity.Property;
import com.staynest.entity.PropertyImage;
import com.staynest.entity.Unit;
import com.staynest.enums.ImageType;
import com.staynest.exception.ResourceNotFoundException;
import com.staynest.exception.UnauthorizedException;
import com.staynest.repository.PropertyImageRepository;
import com.staynest.repository.PropertyRepository;
import com.staynest.repository.UnitRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service for property image management
 */
@Service
public class PropertyImageService {

    private static final Logger logger = LoggerFactory.getLogger(PropertyImageService.class);

    @Autowired
    private PropertyImageRepository propertyImageRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private UnitRepository unitRepository;

    /**
     * Add image to property
     * @param propertyId - PropertyId
     * @param imageUrl - Image URL
     * @param imageType - Image Type
     * @param isPrimary - Is primary image
     * @param userId - User adding image (must be property owner)
     * @return PropertyImage
     */
    @Transactional
    public PropertyImage addPropertyImage(
            UUID propertyId,
            String imageUrl,
            ImageType imageType,
            Boolean isPrimary,
            UUID userId
    ){
        logger.info("Adding image to property {} by user {}",propertyId, userId);

        //Find Property
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with ID: "+ propertyId));

        //Check authorization
        if (!property.getHost().getUserId().equals(userId)){
            throw new UnauthorizedException("You can only add images to your own properties");
        }

        //If setting as primary, unset current primary
        if (isPrimary != null && isPrimary){
            List<PropertyImage> currentPrimary = propertyImageRepository
                    .findByProperty_PropertyIdAndIsPrimaryTrue(propertyId);
            currentPrimary.forEach(img ->{
                img.setIsPrimary(false);
                propertyImageRepository.save(img);
            });
        }

        //Calculate display order
        long currentImageCount = propertyImageRepository.countByProperty_PropertyId(propertyId);

        //Create image
        PropertyImage image = PropertyImage.builder()
                .property(property)
                .unit(null)
                .imageUrl(imageUrl)
                .imageType(imageType)
                .isPrimary(isPrimary != null ? isPrimary : false)
                .displayOrder((int)currentImageCount+1)
                .build();

        PropertyImage savedImage  = propertyImageRepository.save(image);
        logger.info("Image added successfully with ID: {}",savedImage.getImageId());

        return savedImage;
    }

    /**
     * Add image to unit
     * @param unitId - Unit ID
     * @param imageUrl - Image URl
     * @param imageType - Image Type
     * @param userId - User adding image(must be property owner)
     * @return PropertyImage
     */
    @Transactional
    public PropertyImage addUnitImage(
            UUID unitId,
            String imageUrl,
            ImageType imageType,
            UUID userId
    ){
        logger.info("Adding image to unit {} by user {}", unitId, userId);

        //Find unit
        Unit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found with ID: "+ unitId));

        //Check authorization
        if (!unit.getProperty().getHost().getUserId().equals(userId)){
            throw new UnauthorizedException("You can only add images to units in your own properties");
        }

        //Calculate display order
        long currentImageCount = propertyImageRepository.countByUnit_UnitId(unitId);

        //Create image
        PropertyImage image = PropertyImage.builder()
                .property(unit.getProperty())
                .unit(unit)
                .imageUrl(imageUrl)
                .imageType(imageType)
                .isPrimary(false)
                .displayOrder((int) (currentImageCount +1))
                .build();

        PropertyImage savedImage = propertyImageRepository.save(image);
        logger.info("Image added to unit successfully with ID: {}", savedImage.getImageId());

        return savedImage;
    }

    /**
     * Delete image
     * @param imageId - Image ID
     * @param userId - User deleting image(must be property owner)
     */
    @Transactional
    public void deleteImage(UUID imageId, UUID userId){
        logger.info("Deleting image {} by user {}", imageId,userId);

        //Find image
        PropertyImage image = propertyImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found with ID: "+ imageId));

        //check authorization
        if (!image.getProperty().getHost().getUserId().equals(userId)){
            throw new UnauthorizedException("You can only delete images from your own properties");
        }

        //Delete image
        propertyImageRepository.delete(image);
        logger.info("Image {} deleted successfully", imageId);
    }

    /**
     * Get all images for a property
     * @param propertyId - PropertyID
     * @return List of image
     */
    @Transactional(readOnly = true)
    public List<PropertyImage> getPropertyImage(UUID propertyId){
        return propertyImageRepository.findByProperty_PropertyIdOrderByDisplayOrder(propertyId);
    }

    /**
     * Get all image for a unit
     * @param unitId - Unit ID
     * @return List of images
     */
    @Transactional(readOnly = true)
    public List<PropertyImage> getUnitImages(UUID unitId){
        return propertyImageRepository.findByProperty_PropertyIdOrderByDisplayOrder(unitId);
    }

}
