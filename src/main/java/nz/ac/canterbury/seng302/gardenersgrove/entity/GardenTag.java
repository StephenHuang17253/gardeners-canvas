package nz.ac.canterbury.seng302.gardenersgrove.entity;

import jakarta.persistence.*;

/**
 * Entity class of a Tag for a garden
 */
@Entity
@Table(name = "garden_tag")
public class GardenTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Long tagId;

    /**
     * The name on the tag
     */
    @Column(columnDefinition = "TEXT", unique = true)
    private String tagName;

    /**
     * JPA required no-args constructor
     */
    protected GardenTag() {
    }

    /**
     * Creates a new Tag Object
     */
    public GardenTag(String tagNameInput) {
        tagName = tagNameInput;
    }

    /**
     * get a tag object's id
     * @return the id
     */
    public Long getId() {
        return tagId;
    }

    /**
     * get a tag objects name
     * @return the name
     */
    public String getTagName() { return tagName; }

    /**
     * change a tag objects name
     * @param newTagName new name
     */
    public void setTagName(String newTagName) { this.tagName = newTagName; }


    @Override
    public String toString() {
        return "Tag{" +
                "id=" + tagId +
                ", TagName='" + tagName +
                '}';
    }

}
