package edu.pmdm.vegas_laraimdbapp.models;

import java.util.List;

public class TMDBMovieResponse {


    private List<TMDBMovie> results;

    // Método para obtener la lista de películas de la respuesta
    public List<TMDBMovie> getResults() {
        return results;
    }

    // Método para establecer una nueva lista de películas en la respuesta
    public void setResults(List<TMDBMovie> results) {
        this.results = results;
    }
}