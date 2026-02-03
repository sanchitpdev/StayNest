package com.staynest.repository;

import com.staynest.entity.PropertyImage;
import com.staynest.enums.ImageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PropertyImageRepository extends JpaRepository<PropertyImage, UUID> {

    //Find all images for property
    List<PropertyImage> findByProperty_PropertyIdOrderByDisplayOrder(UUID propertyId);

    //Find all images for unit
    List<PropertyImage> findByUnit_UnitIdOrderByDisplayOrder(UUID unitId);

    //Find Primary images for property
    Optional<PropertyImage> findByProperty_PropertyIdAndIsPrimaryTrue(UUID primaryId);

    //Find image by type for a property
    List<PropertyImage> findByProperty_PropertyIdAndImageType(
            UUID propertyId,
            ImageType imageType
    );

    //Count images for property
    long countByProperty_PropertyId(UUID propertyId);

    //Count images for unit
    long countByUnit_UnitId(UUID unitId);

    //Delete all images for a property
    void deleteByProperty_PropertyId(UUID propertyId);

    //Delete all images for a unit
    void deleteByUnit_UnitId(UUID unitId);
}
