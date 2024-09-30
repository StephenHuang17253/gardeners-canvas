package nz.ac.canterbury.seng302.gardenersgrove.service;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Decoration;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.repository.DecorationRepository;
import nz.ac.canterbury.seng302.gardenersgrove.util.DecorationCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for Decoration objects.
 */
@Service
public class DecorationService {

    /**
     * Interface for generic CRUD operations on a repository for Decorations.
     */
    private final DecorationRepository decorationRepository;

    /**
     * DecorationService constructor with repository
     * @param decorationRepository
     */
    @Autowired
    public DecorationService(DecorationRepository decorationRepository) {
        this.decorationRepository = decorationRepository;
    }

    /**
     * Retrieves a decoration from the decoration repository by the id of the decoration
     * @param id id of the decoration
     * @return an optional containing the decoration if it exists.
     */
    public Optional<Decoration> getById(Long id)
    {
        return decorationRepository.findById(id);
    }

    public List<Decoration> getDecorations() {
        return decorationRepository.findAll();
    }

    /**
     * Retrieves all decorations belonging to a particular category
     *
     * @param decorationCategory    the category used in the query
     * @return a list of decorations belonging to that category
     */
    public List<Decoration> getDecorationsByCategory(DecorationCategory decorationCategory) {
        return decorationRepository.findDecorationsByDecorationCategoryIs(decorationCategory);
    }

    /**
     * Retrieves all decoration objects belonging to a particular garden.
     *
     * @param garden the garden the decoration belongs to
     * @return list of decoration objects belonging to that garden.
     */
    public List<Decoration> getDecorationsByGarden(Garden garden) {
        List<Decoration> checkDecorations = decorationRepository.findDecorationsByGardenIs(garden);
        if (checkDecorations.isEmpty()) {
            this.addDecoration(new Decoration(garden, DecorationCategory.ROCK));
            this.addDecoration(new Decoration(garden, DecorationCategory.TABLE));
            this.addDecoration(new Decoration(garden, DecorationCategory.POND));
            this.addDecoration(new Decoration(garden, DecorationCategory.GNOME));
            this.addDecoration(new Decoration(garden, DecorationCategory.FOUNTAIN));
        }
        return decorationRepository.findDecorationsByGardenIs(garden);
    }

    /**
     * Retrieves all decoration objects belonging to a particular category and garden.
     *
     * @param garden                the garden the decoration belongs to
     * @param decorationCategory    the category the decoration belongs to
     * @return list of decoration objects belonging to that garden and category.
     */
    public List<Decoration> getDecorationsByGardenAndCategory(Garden garden, DecorationCategory decorationCategory) {
        return decorationRepository.findDecorationsByGardenIsAndDecorationCategoryIs(garden, decorationCategory);
    }

    /**
     * Saves a new decoration to the repository
     *
     * @param decoration the Decoration to persist
     * @return the Decoration being persisted
     */
    public Decoration addDecoration(Decoration decoration) {
        boolean notAlreadyAdded = this.getDecorationsByGardenAndCategory(decoration.getGarden(), decoration.getDecorationCategory()).isEmpty();
        if (!notAlreadyAdded) {
            throw new IllegalArgumentException("Decoration already in garden");
        }
        return decorationRepository.save(decoration);
    }
}
