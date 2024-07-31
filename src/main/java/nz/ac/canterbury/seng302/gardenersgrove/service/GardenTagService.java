package nz.ac.canterbury.seng302.gardenersgrove.service;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.GardenTag;
import nz.ac.canterbury.seng302.gardenersgrove.entity.GardenTagRelation;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenTagRelationRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenTagRepository;
import nz.ac.canterbury.seng302.gardenersgrove.util.TagStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service class that manages Garden tags
 */
@Service
public class GardenTagService {

    /**
     * Interface for generic CRUD operations on a repository for GardenTag types.
     */
    private final GardenTagRepository gardenTagRepository;

    private final GardenTagRelationRepository gardenTagRelationRepository;

    /**
     * GardenTagService constructor with repository.
     *
     * @param gardenTagRepositoryInput the repository for GardenTags
     */
    @Autowired
    public GardenTagService(GardenTagRepository gardenTagRepositoryInput, GardenTagRelationRepository gardenTagRelationRepository)
    {
        this.gardenTagRepository = gardenTagRepositoryInput;
        this.gardenTagRelationRepository = gardenTagRelationRepository;
    }

    /**
     * Gets all Garden tags currently stored in the underlying repository
     * @return all tags in underlying repository as a List of tags
     */
    public List<GardenTag> getAllGardenTags() {return gardenTagRepository.findAll();}

    /**
     * Saves a garden tag to the repository
     * @param gardenTag the tag to save
     * @return the tag to save (with filled in id field)
     */
    public GardenTag addGardenTag(GardenTag gardenTag) throws IllegalArgumentException {
        boolean nameInUse = this.getByName(gardenTag.getTagName()).isPresent();
        if (nameInUse)
        {
            throw new IllegalArgumentException("A Tag with this name already exists");
        }
        return gardenTagRepository.save(gardenTag);

    }

    /**
     * Gets a tag from the database with a matching name, returns an empty Optional if not found
     * @param queryString the name of the target tag
     * @return An empty optional or one containing a matching tag
     */
    public Optional<GardenTag> getByName(String queryString)
    {
        return gardenTagRepository.findByTagNameIs(queryString);
    }

    /**
     * Gets a tag from the database with a matching id, returns an empty Optional if not found
     * @param id the id of the target tag
     * @return An empty optional or one containing a matching tag
     */
    public Optional<GardenTag> getGardenTabById(Long id)
    {
        return gardenTagRepository.findById(id);
    }

    /**
     * Returns a list of all tags that contain the query string regardless of case
     * Only return Appropriate tags and not tags that are pending or inappropriate
     * @param queryString the query string to match
     * @return a list of all matching tags
     */
    public List<GardenTag> getAllSimilar(String queryString)
    {
        return gardenTagRepository.findByTagNameContainsIgnoreCaseAndTagStatus(queryString, TagStatus.APPROPRIATE);
    }

    /**
     * Saves a gardenTagRelation to the repository
     * @param gardenTagRelation the gardenTagRelation to save
     * @return the GardenTagRelation
     */
    public GardenTagRelation addGardenTagRelation(GardenTagRelation gardenTagRelation) {
        return gardenTagRelationRepository.save(gardenTagRelation);
    }

    /**
     * Remove a gardenTagRelation to the repository
     * @param gardenTagRelation the gardenTagRelation to remove
     */
    public void removeGardenTagRelation(GardenTagRelation gardenTagRelation)
    {
        gardenTagRelationRepository.delete(gardenTagRelation);
    }

    /**
     * Get gardenTagRelation by Id
     * @param id in the relation
     * @return list of gardenTagRelations with that tag
     */
    public Optional<GardenTagRelation> getGardenTagRelationById(Long id) {
        return gardenTagRelationRepository.findById(id);
    }

    /**
     * Get gardenTagRelation by garden
     * @param garden in the relation
     * @return list of gardenTagRelations with that garden
     */
    public List<GardenTagRelation> getGardenTagRelationByGarden(Garden garden) {
        return gardenTagRelationRepository.findGardenTagRelationsByGardenIs(garden);
    }

    /**
     * Get gardenTagRelation by tag
     * @param tag in the relation
     * @return list of gardenTagRelations with that tag
     */
    public List<GardenTagRelation> getGardenTagRelationByTag(GardenTag tag) {
        return gardenTagRelationRepository.findGardenTagRelationsByTagIs(tag);
    }

    /**
     * Get gardenTagRelation by Garden and Tag.
     * Helpful for checking if a specific relation already exists.
     * @param garden in the relation
     * @param tag in the relation
     * @return list of gardenTagRelations with that tag
     */
    public Optional<GardenTagRelation> getGardenTagRelationByGardenAndTag(Garden garden, GardenTag tag) {
        return gardenTagRelationRepository.findGardenTagRelationsByGardenIsAndTagIs(garden, tag);
    }


    /**
     * Updates all tags that are similar to have the same tag status
     * @param tagName name to match tag
     * @param tagStatus new tag status
     */
    public void updateGardenTagStatus(String tagName, TagStatus tagStatus) {
        List<GardenTag>  tagList = gardenTagRepository.findByTagNameIgnoreCase(tagName);
        tagList.forEach(item -> item.setTagStatus(tagStatus));
        gardenTagRepository.saveAll(tagList);
    }

    /**
     * Delete all tag relations by name (case insensitive)
     * To be used when tags in determined to be inappropriate
     * @param tagName tagName to delete
     */
    public void deleteRelationByTagName(String tagName)
    {
        List<GardenTag> inappropriateTags = gardenTagRepository.findByTagNameIgnoreCase(tagName).stream().toList();
        List<GardenTagRelation> inappropriateTagRelations = new ArrayList<>();
        for (GardenTag tag: inappropriateTags)
        {
            inappropriateTagRelations.addAll(gardenTagRelationRepository.findGardenTagRelationsByTagIs(tag));
        }
        gardenTagRelationRepository.deleteAll(inappropriateTagRelations);
    }


}
