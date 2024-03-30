package org.milestone3.albumServlet;

public class AlbumMessagePayload {
    private String albumDetailsJson; // AlbumDto serialized as JSON
    private byte[] imageData;

    public AlbumMessagePayload(String albumDetailsJson, byte[] imageData) {
        this.albumDetailsJson = albumDetailsJson;
        this.imageData = imageData;
    }

    public String getAlbumDetailsJson() {
        return albumDetailsJson;
    }

    public void setAlbumDetailsJson(String albumDetailsJson) {
        this.albumDetailsJson = albumDetailsJson;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }
}
