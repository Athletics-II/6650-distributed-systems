package org.milestone3.albumServlet;

public class AlbumDTO {
    private String name;
    private String artist;
    private String year;

    public AlbumDTO(String name, String artist, String year) {
        this.name = name;
        this.artist = artist;
        this.year = year;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }
}
