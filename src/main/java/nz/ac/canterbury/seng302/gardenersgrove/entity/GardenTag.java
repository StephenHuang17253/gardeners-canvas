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
    private Long tagId;


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

    public Long getId() {
        return tagId;
    }
    public String getTagName() {return tagName;}
    public void setTagName(String newTagName) {this.tagName = newTagName;}


    @Override
    public String toString() {
        return "Tag{" +
                "id=" + tagId +
                ", TagName='" + tagName +
                '}';
    }

}
