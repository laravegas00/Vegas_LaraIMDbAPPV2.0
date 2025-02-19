package edu.pmdm.vegas_laraimdbapp.api;

import edu.pmdm.vegas_laraimdbapp.models.MovieOverviewResponse;
import edu.pmdm.vegas_laraimdbapp.models.MovieResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Interfaz para definir los endpoints de la API de IMDb.
 */
public interface IMDBApiService {

    // Llamada para obtener el top 10 películas de la API
    @GET("title/get-top-meter")
    Call<MovieResponse> getTopMovies(@Query("topMeterTitlesType") String type);

    // Llamada para obtener los datos de las películas del top 10
    @GET("title/get-overview")
    Call<MovieOverviewResponse> getMovieDetails(@Query("tconst") String movieId);
}
