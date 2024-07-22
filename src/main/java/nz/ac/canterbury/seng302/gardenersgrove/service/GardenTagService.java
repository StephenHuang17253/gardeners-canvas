package nz.ac.canterbury.seng302.gardenersgrove.service;

import nz.ac.canterbury.seng302.gardenersgrove.entity.GardenTag;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenTagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GardenTagService {

    /**
     * Interface for generic CRUD operations on a repository for GardenTag types.
     */
    private GardenTagRepository gardenTagRepository;

    /**
     * GardenTagService constructor with repository.
     *
     * @param gardenTagRepositoryInput the repository for GardenTags
     */
    @Autowired
    public GardenTagService(GardenTagRepository gardenTagRepositoryInput)
    {
        this.gardenTagRepository = gardenTagRepositoryInput;
    }

    public List<GardenTag> getGardenTags() {return gardenTagRepository.findAll();}

    public void addGardenTag(GardenTag gardenTag) {
        gardenTagRepository.save(gardenTag);
    }

    public Optional<GardenTag> getByName(String queryString)
    {
        return gardenTagRepository.findByTagNameIs(queryString);
    }

    public List<GardenTag> getAllSimilar(String queryString)
    {
        return gardenTagRepository.findByTagNameContains(queryString);
    }
}
