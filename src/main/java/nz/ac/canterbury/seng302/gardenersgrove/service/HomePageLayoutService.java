package nz.ac.canterbury.seng302.gardenersgrove.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.entity.HomePageLayout;
import nz.ac.canterbury.seng302.gardenersgrove.repository.HomePageLayoutRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;

/**
 * Service class for HomePageLayout objects.
 */
@Service
public class HomePageLayoutService {

    /**
     * Interface for generic CRUD operations on a repository for home page layouts.
     */
    private HomePageLayoutRepository homePageLayoutRepository;
    private UserRepository userRepository;

    @Autowired
    public HomePageLayoutService(HomePageLayoutRepository homePageLayoutRepository, UserRepository userRepository) {
        this.homePageLayoutRepository = homePageLayoutRepository;
        this.userRepository = userRepository;
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
     * Retrieves a HomePageLayout object by users id, if the user is not found this
     * returns null, if the user has no layout this creates a new layout for the
     * user and returns it
     *
     * @param userId the user's id
     */
    public HomePageLayout getLayoutByUserId(long userId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            return null;
        }

        HomePageLayout layout = homePageLayoutRepository.findByUserId(userId).orElse(null);
        if (layout != null) {
            return layout;
        }

        HomePageLayout newLayout = new HomePageLayout(user);
        return updateLayout(newLayout);
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
