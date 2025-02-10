package edu.pmdm.vegas_laraimdbapp.models;

import java.util.List;

/**
 * Clase que representa la respuesta de la API de TMDB cuando se solicitan géneros de películas.
 */
public class TMDBGenreResponse {

    // Lista de géneros obtenidos de la API.
    private List<Genre> genres;

    /**
     * Obtiene la lista de géneros disponibles.
     * @return Lista de géneros.
     */
    public List<Genre> getGenres() {
        return genres;
    }

    /**
     * Establece la lista de géneros.
     * @param genres Lista de géneros a establecer.
     */
    public void setGenres(List<Genre> genres) {
        this.genres = genres;
    }

    /**
     * Clase interna que representa un género individual de película.
     */
    public static class Genre {

        //Identificador único del género.
        private int id;

        // Nombre del género.
        private String name;

        /**
         * Obtiene el ID del género.
         * @return ID del género.
         */
        public int getId() {
            return id;
        }

        /**
         * Establece el ID del género.
         * @param id ID del género a establecer.
         */
        public void setId(int id) {
            this.id = id;
        }

        /**
         * Obtiene el nombre del género.
         * @return Nombre del género.
         */
        public String getName() {
            return name;
        }

        /**
         * Establece el nombre del género.
         * @param name Nombre del género a establecer.
         */
        public void setName(String name) {
            this.name = name;
        }
    }
}
