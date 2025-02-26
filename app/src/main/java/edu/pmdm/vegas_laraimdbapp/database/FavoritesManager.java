package edu.pmdm.vegas_laraimdbapp.database;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.pmdm.vegas_laraimdbapp.models.Movie;

/**
 * Clase para gestionar las películas favoritas.
 */
public class FavoritesManager {

    private static final String DEFAULT_IMAGE = "android.resource://edu.pmdm.vegas_laraimdbapp/drawable/ic_android";
    private Context context;

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
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            userId = auth.getCurrentUser().getUid();
        }
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

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //Comprobar que la pelicula tenga ID
        if (movie.getId() == null || movie.getId().isEmpty()) {
            return false;
        }

        // Verifica el usuario antes de agregar
        if (fBD.movieExists(movie.getId(), userId)) {
            return true;
        } else {
            fBD.addFavorite(movie, userId);

            // Guardar en Firestore
            CollectionReference favoritesRef = db.collection("favorites").document(userId).collection("movies");
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
        if (userId == null || movie.getId() == null) {
            return;
        }

        fBD.removeFavorite(movie.getId(), userId);

        // Eliminar de Firestore
        db.collection("favorites").document(userId).collection("movies").document(movie.getId())
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


    public void registerLogin(String userId, String loginTime) {
        fBD.registerLogin(userId, loginTime);
    }

    public void registerLogout(String userId, String logoutTime) {
        fBD.registerLogout(userId, logoutTime);
    }

    /**
     * Agregar o actualizar información del usuario en la base de datos local.
     */
    public void addOrUpdateUser(String userId, String name, String email, String loginTime, String logoutTime, String address, String phone, String image) {
        Map<String, String> userData = fBD.getUser(userId);

        // Usamos valores existentes si no se pasan nuevos
        String finalName = name != null ? name : userData.getOrDefault("name", "Usuario");
        String finalEmail = email != null ? email : userData.getOrDefault("email", "Sin Email");
        String finalLoginTime = loginTime != null ? loginTime : userData.get("last_login");
        String finalLogoutTime = logoutTime != null ? logoutTime : userData.get("last_logout");
        String finalAddress = address != null ? address : userData.getOrDefault("address", "Sin Dirección");
        String finalPhone = phone != null ? phone : userData.getOrDefault("phone", "Sin Teléfono");
        String finalImage = image != null ? image : userData.getOrDefault("image", DEFAULT_IMAGE);

        fBD.updateUser(userId, finalName, finalEmail, finalLoginTime, finalLogoutTime, finalAddress, finalPhone, finalImage);
    }

    /**
     * Obtener los detalles del usuario desde la base de datos local.
     */
    public Map<String, String> getUserDetails(String userId) {
        return fBD.getUser(userId);
    }

    /**
     * Sincronizar las películas favoritas de Firestore con la base de datos local.
     */
    public void syncFavoritesFromFirestore() {
        if (userId == null) {
            Log.e("FavoritesManager", "No hay usuario autenticado.");
            return;
        }

        CollectionReference favoritesRef = db.collection("favorites").document(userId).collection("movies");

        favoritesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<Movie> moviesFromFirestore = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Movie movie = new Movie();
                    movie.setId(document.getString("movieId"));
                    movie.setTitle(document.getString("title"));
                    movie.setImage(document.getString("imageUrl"));
                    movie.setReleaseDate(document.getString("releaseDate"));
                    movie.setPlot(document.getString("plot"));
                    movie.setRating(document.getDouble("rating"));

                    moviesFromFirestore.add(movie);
                }

                // Guardar en SQLite
                for (Movie movie : moviesFromFirestore) {
                    if (!fBD.movieExists(movie.getId(), userId)) {
                        fBD.addFavorite(movie, userId);
                    }
                }

                Log.d(TAG, "Sincronización de favoritos completada con éxito.");
            } else {
                Log.e(TAG, "Error al recuperar favoritos de Firestore", task.getException());
            }
        });
    }

    /**
     * Escuchar cambios en las películas en tiempo real desde Firestore.
     */
    public void listenForMovieUpdates() {
        db.collection("movies").addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Log.e("FavoritesManager", "Error al escuchar cambios en películas", e);
                return;
            }

            if (snapshots != null) {
                FavoriteDatabase database = new FavoriteDatabase(context);

                for (DocumentSnapshot document : snapshots.getDocuments()) {
                    Movie movie = document.toObject(Movie.class);
                    database.addFavorite(movie, userId); // Guardar en SQLite
                }

                Log.d("FavoritesManager", "Películas actualizadas en tiempo real desde Firestore.");
            }
        });
    }


}
