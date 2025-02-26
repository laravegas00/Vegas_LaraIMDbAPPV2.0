package edu.pmdm.vegas_laraimdbapp.sync;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.pmdm.vegas_laraimdbapp.database.FavoriteDatabase;
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
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userId = user.getUid();
        } else {
            Log.e(TAG, "El usuario no está autenticado. No se puede sincronizar favoritos.");
            userId = null;
        }    }

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
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e(TAG, "Sincronizacion cancelada. No hay usuario autenticado.");
            return;
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", null);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Obtener las películas desde Firestore
        db.collection("favorites").document(userId).collection("movies")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d(TAG, "No hay favoritos almacenados en Firestore.");
                        return;
                    }

                    FavoriteDatabase database = new FavoriteDatabase(context);
                    //database.clearFavorites(userId); // Eliminar los favoritos locales antes de sincronizar

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Movie movie = document.toObject(Movie.class);
                        if (movie != null) {
                            database.addFavorite(movie, userId);
                        }
                    }

                })
                .addOnFailureListener(e -> Log.e(TAG, "Error al obtener favoritos de Firestore", e));
    }


}