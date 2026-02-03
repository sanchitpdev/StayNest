package com.staynest.repository;

import com.staynest.entity.Property;
import com.staynest.enums.PropertyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PropertyRepository extends JpaRepository<Property, UUID> {

    //Find  all property by host
    List<Property> findByHost_UserId(UUID hostId);

    //Find property by city
    List<Property> findByCity(String city);

    //find property by city and country
    List<Property> findByCityAndCountry(String city,String country);

    //find property by type
    List<Property> findByPropertyType(PropertyType propertyType);

    //find property by city and type
    List<Property> findByCityAndPropertyType(String city,PropertyType propertyType);

    //Search property by name(partial name)
    List<Property> findByPropertyNameContainingIgnoreCase(String name);

    //Custom query : Find properties within a city at least one available unit
    @Query("SELECT DISTINCT p FROM Property p " +
            "JOIN p.units u " +
            "WHERE p.city = :city AND u.isAvailable = true")
    List<Property> findAvailablePropertiesByCity(@Param("city") String city);

    //Count properties by host
    long countByHost_UserId(UUID hostId);


}
