package edu.pmdm.vegas_laraimdbapp.api;

import edu.pmdm.vegas_laraimdbapp.models.TMDBGenreResponse;
import edu.pmdm.vegas_laraimdbapp.models.TMDBMovieResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;


/**
 * Interfaz para definir los endpoints de la API de TMDB.
 */
public interface TMDBApiService {

    // Clave de API de TMDB
    String API_KEY = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIyZjM3OWY4NjY0YmQxNjY4MzVlODg4YzA5OWFlNjMxZCIsIm5iZiI6MTczODE3NDczOC44MzM5OTk5LCJzdWIiOiI2NzlhNzExMjQ0NDhkYTNkMmFiZDY4NmEiLCJzY29wZXMiOlsiYXBpX3JlYWQiXSwidmVyc2lvbiI6MX0.t0Rc1FqF5gRlDF9pCa86NUIR4G2f7GmDXOpzsOGH_Hw";

    // Obtener la lista de géneros de películas
    @Headers({"Authorization: " + API_KEY, "accept: application/json"})
    @GET("genre/movie/list")
    Call<TMDBGenreResponse> getGenres(@Query("language") String language);

    // Buscar películas con filtros de género y año
    @Headers({"Authorization: " + API_KEY, "accept: application/json"})
    @GET("discover/movie")
    Call<TMDBMovieResponse> discoverMovies(
            @Query("language") String language,
            @Query("include_adult") boolean includeAdult,
            @Query("page") int page,
            @Query("with_genres") String genreId,
            @Query("primary_release_year") String year
    );
}
