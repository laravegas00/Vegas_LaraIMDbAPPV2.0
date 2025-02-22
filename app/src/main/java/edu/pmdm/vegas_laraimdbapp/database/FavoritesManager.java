package edu.pmdm.vegas_laraimdbapp.database;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.pmdm.vegas_laraimdbapp.models.Movie;

/**
 * Clase para gestionar las películas favoritas.
 */
public class FavoritesManager {

    // Instancia única de la clase
    private static FavoritesManager instance;
    private FavoriteDatabase fBD; // Base de datos de películas favoritas

    private FirebaseFirestore db; // Firestore
    private String userId; // Usuario autenticado

    /**
     * Constructor privado para evitar instanciación externa
     * @param context Contexto de la aplicación
     */
    private FavoritesManager(Context context) {
        fBD = new FavoriteDatabase(context);
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    /**
     * Obtener la instancia única de la clase
     * @param context Contexto de la aplicación
     * @return Instancia de la clase
     */
    public static synchronized FavoritesManager getInstance(Context context) {
        if (instance == null) {
            instance = new FavoritesManager(context);
        }
        return instance;
    }

    /**
     * Agregar una película a la lista de favoritos
     * @param movie Película a agregar
     * @param userId ID del usuario
     * @return True si la película ya estaba en favoritos, false en caso contrario
     */
    public boolean addFavorite(Movie movie, String userId) {

        //Comprobar que la pelicula tenga ID
        if (movie.getId() == null || movie.getId().isEmpty()) {
            Log.e("FavoritesManager", "El ID de la película es nulo o vacío.");
            return false;
        }

        // Verifica el usuario antes de agregar
        if (fBD.movieExists(movie.getId(), userId)) {
            Log.i("FavoritesManager", "Película ya en favoritos de usuario: " + userId);
            return true;
        } else {
            fBD.addFavorite(movie, userId);
            Log.i("FavoritesManager", "Película añadida a favoritos de usuario: " + userId);

            // Guardar en Firestore
            CollectionReference favoritesRef = db.collection("users").document(userId).collection("favorites");
            Map<String, Object> movieData = new HashMap<>();
            movieData.put("movieId", movie.getId());
            movieData.put("title", movie.getTitle());
            movieData.put("imageUrl", movie.getImage());
            movieData.put("releaseDate", movie.getReleaseDate());
            movieData.put("plot", movie.getPlot());
            movieData.put("rating", movie.getRating());

            favoritesRef.document(movie.getId()).set(movieData)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Película añadida a Firestore: " + movie.getTitle()))
                    .addOnFailureListener(e -> Log.e(TAG, "Error al añadir a Firestore", e));


            return false;
        }
    }

    /**
     * Eliminar una película de la lista de favoritos
     * @param movie Película a eliminar
     * @param userId ID del usuario
     */
    public void removeFavorite(Movie movie, String userId) {
        fBD.removeFavorite(movie.getId(), userId);

        // Eliminar de Firestore
        db.collection("users").document(userId).collection("favorites").document(movie.getId())
                .delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Película eliminada de Firestore"))
                .addOnFailureListener(e -> Log.e(TAG, "Error al eliminar de Firestore", e));


    }

    /**
     * Obtener todas las películas favoritas de un usuario
     * @param userId ID del usuario
     * @return Lista de películas favoritas
     */
    public List<Movie> getFavoriteMovies(String userId) {
        return fBD.getAllFavorites(userId); // Obtener las películas favoritas del usuario
    }
}
