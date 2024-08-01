package nz.ac.canterbury.seng302.gardenersgrove.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import nz.ac.canterbury.seng302.gardenersgrove.entity.HomePageLayout;

import java.util.Optional;

@Repository
public interface HomePageLayoutRepository extends CrudRepository<HomePageLayout, Long>{
    
    /**
     * Finds a HomePageLayout object by id
     * @param id the layouts's id
     */
    Optional<HomePageLayout> findById(long id);

    /**
     * Find HomePageLayout by user id
     *
     * @param userId the user's id
     * @return optional layout
     */
    Optional<HomePageLayout> findByUserId(long userId);
}
