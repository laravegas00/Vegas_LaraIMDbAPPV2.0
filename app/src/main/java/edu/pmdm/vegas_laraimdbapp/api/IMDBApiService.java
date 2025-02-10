package edu.pmdm.vegas_laraimdbapp.api;

import edu.pmdm.vegas_laraimdbapp.models.MovieOverviewResponse;
import edu.pmdm.vegas_laraimdbapp.models.MovieResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

/**
 * Interfaz para definir los endpoints de la API de IMDb.
 */
public interface IMDBApiService {

    //Llamada para obtener el top 10 peliculas de la API
    @Headers({
            "x-rapidapi-host: imdb-com.p.rapidapi.com",
            "x-rapidapi-key: 9c04b1a854msh2056acaabc5ce24p142c9djsnbdd53d066b65"
    })
    @GET("title/get-top-meter")
    Call<MovieResponse> getTopMovies(@Query("topMeterTitlesType") String type);

    //Llamada para obtener los datos de las peliculas del top 10
    @Headers({
            "x-rapidapi-host: imdb-com.p.rapidapi.com",
            "x-rapidapi-key: 9c04b1a854msh2056acaabc5ce24p142c9djsnbdd53d066b65"
    })
    @GET("title/get-overview")
    Call<MovieOverviewResponse> getMovieDetails(@Query("tconst") String movieId);

}
