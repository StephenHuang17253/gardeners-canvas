package nz.ac.canterbury.seng302.gardenersgrove.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * Entity class of a relation between Garden and a Tag
 */
@Entity
@Table(name = "garden_tag_relation")
public class GardenTagRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_relation_id")
    private Long tagRelationId;

    @ManyToOne
    @JoinColumn(name = "garden_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Garden garden;

    @ManyToOne
    @JoinColumn(name = "tag_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private GardenTag tag;

    /**
     * JPA required no-args constructor
     */
    protected GardenTagRelation() {
    }

    /**
     * Creates a new Garden-Tag relation
     */
    public GardenTagRelation(Garden gardenInput, GardenTag tagInput) {
        garden = gardenInput;
        tag = tagInput;
    }

    /**
     * Get a Garden-Tag relation's id
     * @return the id
     */
    public Long getId() {
        return tagRelationId;
    }

    /**
     * Get the garden in the Garden-Tag relation
     * @return the garden
     */
    public Garden getGarden() {
        return garden;
    }

    /**
     * Get the tag in the Garden-Tag relation
     * @return the tag
     */
    public GardenTag getTag() {
        return tag;
    }

    @Override
    public String toString() {
        return "Tag{" +
                "id=" + tagRelationId +
                " tagName=" + tag.getTagName() +
                " gardenName=" + garden.getGardenName() +
                '}';
    }

}
