package edu.pmdm.vegas_laraimdbapp.models;

import com.google.gson.annotations.SerializedName;

/**
 * Clase que modela una película obtenida desde la API de TMDB.
 */
public class TMDBMovie {

    ///Identificador único de la película
    private String id;

    //Título de la película
    @SerializedName("title")
    private String title;

    //Ruta del póster de la película
    @SerializedName("poster_path")
    private String posterPath;

    // Sinopsis de la película
    @SerializedName("overview")
    private String overview;

    //Fecha de estreno de la película
    @SerializedName("release_date")
    private String releaseDate;

    // Calificación promedio de la película.
    @SerializedName("vote_average")
    private double rating;

    /**
     * Constructor vacío de la clase.
     */
    public TMDBMovie() {}

    /**
     * Constructor que inicializa los atributos de la película
     * @param id Identificador de la película.
     * @param title Título de la película.
     * @param posterPath Ruta del póster de la película.
     * @param overview Sinopsis de la película.
     * @param releaseDate Fecha de estreno de la película.
     * @param rating Calificación promedio de la película.
     */
    public TMDBMovie(String id, String title, String posterPath, String overview, String releaseDate, double rating) {
        this.id = id;
        this.title = title;
        this.posterPath = posterPath;
        this.overview = overview;
        this.releaseDate = releaseDate;
        this.rating = rating;
    }

    /**
     * Obtiene el identificador de la película.
     * @return ID de la película.
     */
    public String getId() {
        return id;
    }

    /**
     * Establece el identificador de la película.
     * @param id ID de la película.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Obtiene el título de la película.
     * @return Título de la película.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Establece el título de la película.
     * @param title Título de la película.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Obtiene la ruta del póster de la película.
     * @return Ruta del póster.
     */
    public String getPosterPath() {
        return posterPath;
    }

    /**
     * Establece la ruta del póster de la película.
     * @param posterPath Ruta del póster.
     */
    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    /**
     * Obtiene la sinopsis de la película.
     * @return Sinopsis de la película.
     */
    public String getOverview() {
        return overview;
    }

    /**
     * Establece la sinopsis de la película.
     * @param overview Sinopsis de la película.
     */
    public void setOverview(String overview) {
        this.overview = overview;
    }

    /**
     * Obtiene la fecha de estreno de la película.
     * @return Fecha de estreno.
     */
    public String getReleaseDate() {
        return releaseDate;
    }

    /**
     * Establece la fecha de estreno de la película
     * @param releaseDate Fecha de estreno.
     */
    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    /**
     * Obtiene la calificación promedio de la película.
     * @return Calificación de la película.
     */
    public double getRating() {
        return rating;
    }

    /**
     * Establece la calificación promedio de la película.
     * @param rating Calificación de la película.
     */
    public void setRating(double rating) {
        this.rating = rating;
    }

    /**
     * Obtiene la URL completa de la imagen del póster de la película
     * @return URL del póster de la película.
     */
    public String getFullImageUrl() {
        return "https://image.tmdb.org/t/p/w500" + this.posterPath;
    }

    /**
     * Convierte un objeto TMDBMovie en un objeto Movie
     * para poder almacenarlo en la base de datos o en la lista de favoritos.
     * @return Objeto Movie con los datos de la película.
     */
    public Movie toMovie() {
        Movie movie = new Movie();
        movie.setId(this.id);
        movie.setTitle(this.title);
        movie.setImage(getFullImageUrl());
        movie.setReleaseDate(this.releaseDate);
        movie.setPlot(this.overview != null ? this.overview : "Descripción no disponible");
        movie.setRating(this.rating > 0 ? this.rating : -1.0);
        return movie;
    }
}
