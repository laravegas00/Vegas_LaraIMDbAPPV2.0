package edu.pmdm.vegas_laraimdbapp.models;

/**
 * Clase que representa una película.
 */
public class Movie{

    // Atributos de la película
    private String id;
    private String image;
    private String title;
    private String plot;
    private double rating;
    private String releaseDate;

    /**
     * Constructor de la clase
     * @param id Identificador único de la película
     * @param image URL de la imagen de la película
     * @param title Título de la película
     * @param plot Descripción de la pelicula
     * @param rating Puntuación de la película
     * @param releaseDate Fecha de estreno de la película
     */
    public Movie(String id, String image, String title, String plot, double rating, String releaseDate) {
        this.id = id;
        this.image = image;
        this.title = title;
        this.plot = plot;
        this.rating = rating;
        this.releaseDate = releaseDate;
    }

    /**
     * Constructor vacío de la clase
     */
    public Movie() {
    }

    // Métodos de acceso para los atributos (getters y setters)
    public String getId() {
        return id;
    }

    public String getImage() {
        return image;
    }

    public String getTitle() {
        return title;
    }

    public String getPlot() {
        return plot;
    }

    public double getRating() {
        return rating;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPlot(String plot) {
        this.plot = plot;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    /**
     * Método para comparar dos películas
     * @param obj Objeto
     * @return Devuelve si las peliculas son iguales
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Movie movie = (Movie) obj;
        return id.equals(movie.id);
    }

    /**
     * Método que genera un ID para la pelicula
     * @return ID generado
     */
    @Override
    public int hashCode() {
        return id.hashCode();
    }

}