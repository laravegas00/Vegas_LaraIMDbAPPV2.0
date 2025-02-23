package edu.pmdm.vegas_laraimdbapp.sync;

import android.content.Context;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.pmdm.vegas_laraimdbapp.database.FavoritesManager;
import edu.pmdm.vegas_laraimdbapp.models.Movie;

public class FavoritesSyncManager {
    private static final String TAG = "FavoritesSyncManager";
    private final FavoritesManager favoritesManager;
    private final FirebaseFirestore db;
    private final String userId;

    public FavoritesSyncManager(Context context) {
        this.favoritesManager = FavoritesManager.getInstance(context);
        this.db = FirebaseFirestore.getInstance();
        this.userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    private CollectionReference getFavoritesCollection() {
        return db.collection("favorites").document(userId).collection("movies");
    }

    public void addFavoriteToFirestore(Movie movie) {
        CollectionReference favoritesRef = getFavoritesCollection();
        Map<String, Object> movieData = new HashMap<>();
        movieData.put("movieId", movie.getId());
        movieData.put("title", movie.getTitle());
        movieData.put("imageUrl", movie.getImage());
        movieData.put("releaseDate", movie.getReleaseDate());
        movieData.put("plot", movie.getPlot());
        movieData.put("rating", movie.getRating());

        favoritesRef.document(movie.getId()).set(movieData)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Película sincronizada en Firestore: " + movie.getTitle()))
                .addOnFailureListener(e -> Log.e("Firestore", "Error al subir película", e));
    }

    public void syncLocalWithFirestore(Context context) {
        CollectionReference favoritesRef = getFavoritesCollection();

        List<Movie> localFavorites = favoritesManager.getFavoriteMovies(userId);
        List<String> localMovieIds = new ArrayList<>();
        for (Movie movie : localFavorites) {
            localMovieIds.add(movie.getId());
        }

        List<Movie> firestoreMovies = new ArrayList<>();
        favoritesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<String> remoteMovieIds = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String id = document.getString("movieId");
                    String title = document.getString("title");
                    String posterUrl = document.getString("imageUrl");
                    String releaseDate = document.getString("releaseDate");
                    String overview = document.getString("plot");
                    double rating = document.getDouble("rating");
                    Movie movie = new Movie(id, posterUrl, title, overview, rating, releaseDate);
                    firestoreMovies.add(movie);
                    remoteMovieIds.add(id);
                }

                for (Movie movie : localFavorites) {
                    if (!remoteMovieIds.contains(movie.getId())) {
                        addFavoriteToFirestore(movie);
                    }
                }

                for (Movie movie : firestoreMovies) {
                    if (!localMovieIds.contains(movie.getId())) {
                        favoritesManager.addFavorite(movie, userId);
                    }
                }

                Log.d(TAG, "Sincronización completada entre SQLite y Firestore.");
            } else {
                Log.e(TAG, "Error al obtener películas de Firestore", task.getException());
            }
        });
    }

}