package nz.ac.canterbury.seng302.gardenersgrove.entity;

import jakarta.persistence.*;
import nz.ac.canterbury.seng302.gardenersgrove.util.DecorationCategory;

/**
 * Entity class of a Decoration, contains its id, category, and the Garden it belongs to.
 */
@Entity
public class Decoration {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "garden_id")
    private Garden garden;

    @Enumerated(EnumType.STRING)
    @Column
    private DecorationCategory decorationCategory;

    /**
     * JPA required no-args constructor
     */
    protected Decoration() {
    }

    /**
     * Creates a new decoration object.
     * @param decorationCategory        the category of decoration
     * @param garden                    the Garden object that the plant belongs to
     */
    public Decoration(Garden garden,
                      DecorationCategory decorationCategory) {
        this.garden = garden;
        this.decorationCategory = decorationCategory;
    }

    public Long getId() {
        return id;
    }

    public Garden getGarden() {
        return garden;
    }

    public DecorationCategory getDecorationCategory() {
        if (this.decorationCategory == null) {
            this.decorationCategory = DecorationCategory.GNOME;
        }
        return this.decorationCategory;
    }

}
