package datn.bkdn.com.saywithvideo.model;

/**
 * Created by Admin on 4/3/2016.
 */
public class AudioUser {
    private String name;
    private int plays;
    private String id;
    private String linkOnDisk;
    private String date_create;
    private boolean isPlaying;

    public AudioUser(String name, int plays, String id, String linkOnDisk, String date_create) {
        this.name = name;
        this.plays = plays;
        this.id = id;
        this.linkOnDisk = linkOnDisk;
        this.date_create = date_create;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPlays() {
        return plays;
    }

    public void setPlays(int plays) {
        this.plays = plays;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLinkOnDisk() {
        return linkOnDisk;
    }

    public void setLinkOnDisk(String linkOnDisk) {
        this.linkOnDisk = linkOnDisk;
    }

    public String getDate_create() {
        return date_create;
    }

    public void setDate_create(String date_create) {
        this.date_create = date_create;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setIsPlaying(boolean isPlaying) {
        this.isPlaying = isPlaying;
    }
}
