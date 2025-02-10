package edu.pmdm.vegas_laraimdbapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import edu.pmdm.vegas_laraimdbapp.adapter.MovieAdapter;
import edu.pmdm.vegas_laraimdbapp.api.ApiClientTMDB;
import edu.pmdm.vegas_laraimdbapp.api.TMDBApiService;
import edu.pmdm.vegas_laraimdbapp.database.FavoritesManager;
import edu.pmdm.vegas_laraimdbapp.models.Movie;
import edu.pmdm.vegas_laraimdbapp.models.TMDBMovie;
import edu.pmdm.vegas_laraimdbapp.models.TMDBMovieResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Actividad para mostrar una lista de películas.
 */
public class MovieListActivity extends AppCompatActivity {

    // Declarar las variables
    private RecyclerView recyclerView;
    private MovieAdapter movieAdapter;
    private TMDBApiService apiService;
    private List<Movie> movieList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list2);

        // Inicializar las vistas
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        movieAdapter = new MovieAdapter(this, movieList, this::onMovieClick);
        movieAdapter.setOnMovieLongClickListener(this::onMovieLongClick);
        recyclerView.setAdapter(movieAdapter);

        apiService = ApiClientTMDB.getClient().create(TMDBApiService.class);

        // Obtener datos del intent
        String genreId = getIntent().getStringExtra("genreId");
        String year = getIntent().getStringExtra("year");

        // Validar datos
        if (genreId == null || year == null) {
            Toast.makeText(this, "Datos no válidos", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Realizar la búsqueda de películas
        searchMovies(genreId, year);
    }

    /**
     * Método para realizar la búsqueda de películas.
     * @param genreId El ID del género de la película.
     * @param year El año de lanzamiento de la película.
     */
    private void searchMovies(String genreId, String year) {
        apiService.discoverMovies("es-ES", false, 1, genreId, year) // Realizar la búsqueda
                .enqueue(new Callback<TMDBMovieResponse>() {
                    @Override
                    public void onResponse(Call<TMDBMovieResponse> call, Response<TMDBMovieResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            movieList.clear();
                            movieList.addAll(convertToMovieList(response.body().getResults()));
                            movieAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFailure(Call<TMDBMovieResponse> call, Throwable t) {
                        Toast.makeText(MovieListActivity.this, "Error al obtener películas", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Método para convertir una lista de películas de TMDB a una lista de películas de la aplicación.
     * @param tmdbMovies La lista de películas de TMDB.
     * @return La lista de películas de la aplicación.
     */
    private List<Movie> convertToMovieList(List<TMDBMovie> tmdbMovies) {

        List<Movie> movieList = new ArrayList<>(); // Crear una lista de películas
        for (TMDBMovie tmdbMovie : tmdbMovies) { // Recorrer la lista de películas de TMDB
            movieList.add(tmdbMovie.toMovie()); // Agregarla a la lista
        }
        return movieList; // Devolver la lista de películas
    }

    /**
     * Método para manejar el clic en una película.
     * @param movie La película seleccionada.
     */
    private void onMovieClick(Movie movie) {

        // Navegar a la actividad de detalles de la película
        Intent intent = new Intent(MovieListActivity.this, MovieDetailsActivity.class);
        intent.putExtra("id", movie.getId());
        intent.putExtra("title", movie.getTitle());
        intent.putExtra("imageUrl", movie.getImage());
        intent.putExtra("releaseDate", movie.getReleaseDate());
        intent.putExtra("plot", movie.getPlot());
        intent.putExtra("rating", movie.getRating());
        intent.putExtra("TMDB", true); // Indicar que la película proviene de TMDB
        startActivity(intent);
    }

    private void onMovieLongClick(Movie movie) {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String userId = prefs.getString("USER_ID", null);

        if (userId == null) {
            Log.e("MovieListActivity", "No se encontró un ID de usuario válido en SharedPreferences");
            Toast.makeText(this, "Error: No se ha encontrado un usuario válido", Toast.LENGTH_SHORT).show();
            return;
        }

        FavoritesManager favoritesManager = FavoritesManager.getInstance(this);
        if (favoritesManager.addFavorite(movie, userId)) {
            Log.i("MovieListActivity", "Película ya estaba en favoritos para usuario: " + userId);
            Toast.makeText(this, "Película ya estaba en favoritos", Toast.LENGTH_SHORT).show();
        } else {
            Log.i("MovieListActivity", "Película agregada a favoritos para usuario: " + userId);
            Toast.makeText(this, "Película agregada a favoritos", Toast.LENGTH_SHORT).show();
        }
    }

}
