package nz.ac.canterbury.seng302.gardenersgrove.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import nz.ac.canterbury.seng302.gardenersgrove.entity.HomePageLayout;
import nz.ac.canterbury.seng302.gardenersgrove.repository.HomePageLayoutRepository;

/**
 * Service class for HomePageLayout objects.
 */
@Service
public class HomePageLayoutService {

    /**
     * Interface for generic CRUD operations on a repository for home page layouts.
     */
    private HomePageLayoutRepository homePageLayoutRepository;

    @Autowired
    public HomePageLayoutService(HomePageLayoutRepository homePageLayoutRepository) {
        this.homePageLayoutRepository = homePageLayoutRepository;
    }

    /**
     * Retrieves a HomePageLayout object by id
     * If no layout exists returns null
     *
     * @param id the layout's id
     */
    public HomePageLayout getLayoutById(long id) {
        return homePageLayoutRepository.findById(id).orElse(null);
    }

    /**
     * Retrieves a HomePageLayout object by owner id
     * If no layout exists returns null
     *
     * @param userId the user's id
     */
    public HomePageLayout getLayoutByOwnerId(long userId) {
        return homePageLayoutRepository.findByOwnerId(userId).orElse(null);
    }

    /**
     * Saves a HomePageLayout object to the repository
     * 
     * @param homePageLayout updated layout to save
     * @return the updated layout
     */
    public HomePageLayout updateLayout(HomePageLayout homePageLayout) {
        return homePageLayoutRepository.save(homePageLayout);
    }

}
