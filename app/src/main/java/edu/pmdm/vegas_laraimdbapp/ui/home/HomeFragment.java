package edu.pmdm.vegas_laraimdbapp.ui.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import edu.pmdm.vegas_laraimdbapp.MovieDetailsActivity;
import edu.pmdm.vegas_laraimdbapp.R;
import edu.pmdm.vegas_laraimdbapp.adapter.MovieAdapter;
import edu.pmdm.vegas_laraimdbapp.api.ApiClientIMDB;
import edu.pmdm.vegas_laraimdbapp.api.IMDBApiService;
import edu.pmdm.vegas_laraimdbapp.database.FavoritesManager;
import edu.pmdm.vegas_laraimdbapp.models.Movie;
import edu.pmdm.vegas_laraimdbapp.models.MovieResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Fragmento que muestra las películas más populares.
 */
public class HomeFragment extends Fragment {

    // Declaración de variables
    private RecyclerView recyclerView;
    private MovieAdapter movieAdapter;
    private IMDBApiService apiService;
    private List<Movie> movieList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Infla el diseño del fragmento
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = root.findViewById(R.id.recyclerView); // Asignamos el RecyclerView
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2)); // Asignamos el LayoutManager en 2 columnas
        movieAdapter = new MovieAdapter(getContext(), movieList, this::onMovieClick); // Asignamos el adaptador
        movieAdapter.setOnMovieLongClickListener(this::onMovieLongClick); // Asignamos el listener para añadir a favoritos
        recyclerView.setAdapter(movieAdapter); // Asignamos el adaptador al RecyclerView

        // Inicializa el servicio de la API
        apiService = ApiClientIMDB.getClient().create(IMDBApiService.class);

        // Carga las películas
        loadTop10Movies();

        return root; // Devolvemos la vista
    }

    /**
     * Método que carga las películas más populares.
     */
    private void loadTop10Movies() {
        Call<MovieResponse> call = apiService.getTopMovies("ALL"); // Llamada a la API para obtener las películas más populares
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) { // Si la respuesta es exitosa y contiene datos
                    List<MovieResponse.Edge> edges = response.body().getData().getTopMeterTitles().getEdges(); // Obtenemos las películas
                    movieList.clear(); // Limpiamos la lista de películas

                    int limit = Math.min(edges.size(), 10); // Limitar a las 10 primeras películas con mejor ranking
                    for (int i = 0; i < limit; i++) { // Recorremos las películas
                        MovieResponse.Node node = edges.get(i).getNode(); // Obtenemos el nodo de la película
                        if (node != null) { // Si el nodo no es nulo
                            movieList.add(new Movie( // Agregamos la película a la lista
                                    node.getId(),
                                    node.getImageUrl(),
                                    node.getTitleText(),
                                    node.getPlotText(),
                                    node.getRating(),
                                    node.getReleaseDateString()
                            ));
                        }
                    }
                    movieAdapter.notifyDataSetChanged(); // Notificamos al adaptador que los datos han cambiado
                } else {
                    Log.e("API_ERROR", "Código de respuesta: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                Log.e("API_ERROR", "Error al cargar las películas", t);
            }
        });
    }


    /**
     * Método que se llama cuando se hace clic en una película.
     * @param movie Película seleccionada.
     */
    private void onMovieClick(Movie movie) {
        Intent intent = new Intent(getContext(), MovieDetailsActivity.class);
        intent.putExtra("id", movie.getId());
        intent.putExtra("title", movie.getTitle());
        intent.putExtra("imageUrl", movie.getImage());
        intent.putExtra("releaseDate", movie.getReleaseDate());
        intent.putExtra("plot", movie.getPlot());
        intent.putExtra("TMDB", false);
        startActivity(intent);
    }

    /**
     * Método que se llama cuando se hace un clic largo en una película.
     * @param movie
     */
    private void onMovieLongClick(Movie movie) {
        // Obtener el ID del usuario actual
        SharedPreferences prefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String userId = prefs.getString("USER_ID", null);

        // Verificar que el ID del usuario no sea nulo
        if (userId == null) {
            Log.e("HomeFragment", "No se encontró un ID de usuario válido en SharedPreferences");
            Toast.makeText(getContext(), "Error: No se ha encontrado un usuario válido", Toast.LENGTH_SHORT).show();
            return;
        }

        // Agregar la película a favoritos
        FavoritesManager favoritesManager = FavoritesManager.getInstance(getContext());

        // Verificar si la película ya está en favoritos
        if (favoritesManager.addFavorite(movie, userId)) {
            Log.i("HomeFragment", "Película ya estaba en favoritos para usuario: " + userId);
            Toast.makeText(getContext(), "Película ya estaba en favoritos", Toast.LENGTH_SHORT).show();
        } else {
            Log.i("HomeFragment", "Película agregada a favoritos para usuario: " + userId);
            Toast.makeText(getContext(), "Película agregada a favoritos", Toast.LENGTH_SHORT).show();
        }
    }



}
