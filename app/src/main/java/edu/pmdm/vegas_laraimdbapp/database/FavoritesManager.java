package edu.pmdm.vegas_laraimdbapp.database;

import static android.content.ContentValues.TAG;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.pmdm.vegas_laraimdbapp.models.Movie;
import edu.pmdm.vegas_laraimdbapp.models.User;

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



    // NUEVOS MÉTODOS PARA GESTIÓN DE USUARIOS

    // Registra o actualiza un usuario en la base de datos local
    public boolean addOrUpdateUser(User user) {
        SQLiteDatabase db = fBD.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FavoriteDatabase.COLUMN_USER_ID, user.getId());
        values.put(FavoriteDatabase.COLUMN_NAME, user.getName());
        values.put(FavoriteDatabase.COLUMN_EMAIL, user.getEmail());
        values.put(FavoriteDatabase.COLUMN_LAST_LOGIN, user.getLastLogin());
        values.put(FavoriteDatabase.COLUMN_LAST_LOGOUT, user.getLastLogout());

        // Verificar si el usuario ya existe
        Cursor cursor = db.query(FavoriteDatabase.TABLE_USERS, null,
                FavoriteDatabase.COLUMN_USER_ID + "=?", new String[]{user.getId()},
                null, null, null);
        boolean exists = (cursor.getCount() > 0);
        cursor.close();

        if (exists) {
            int rows = db.update(FavoriteDatabase.TABLE_USERS, values,
                    FavoriteDatabase.COLUMN_USER_ID + "=?", new String[]{user.getId()});
            db.close();
            return rows > 0;
        } else {
            long result = db.insert(FavoriteDatabase.TABLE_USERS, null, values);
            db.close();
            return result != -1;
        }
    }

    // Recupera un usuario dado su ID
    public User getUser(String userId) {
        SQLiteDatabase db = fBD.getReadableDatabase();
        Cursor cursor = db.query(FavoriteDatabase.TABLE_USERS, null,
                FavoriteDatabase.COLUMN_USER_ID + "=?", new String[]{userId},
                null, null, null);
        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            String id = cursor.getString(cursor.getColumnIndexOrThrow(FavoriteDatabase.COLUMN_USER_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(FavoriteDatabase.COLUMN_NAME));
            String email = cursor.getString(cursor.getColumnIndexOrThrow(FavoriteDatabase.COLUMN_EMAIL));
            String lastLogin = cursor.getString(cursor.getColumnIndexOrThrow(FavoriteDatabase.COLUMN_LAST_LOGIN));
            String lastLogout = cursor.getString(cursor.getColumnIndexOrThrow(FavoriteDatabase.COLUMN_LAST_LOGOUT));
            user = new User(id, name, email, lastLogin, lastLogout);
            cursor.close();
        }
        db.close();
        return user;
    }

    // Actualizar último login del usuario
    public boolean updateLastLogin(String userId, String lastLogin) {
        SQLiteDatabase db = fBD.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FavoriteDatabase.COLUMN_LAST_LOGIN, lastLogin);
        int rows = db.update(FavoriteDatabase.TABLE_USERS, values,
                FavoriteDatabase.COLUMN_USER_ID + "=?", new String[]{userId});
        db.close();
        return rows > 0;
    }

    // Actualizar último logout del usuario
    public boolean updateLastLogout(String userId, String lastLogout) {
        SQLiteDatabase db = fBD.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FavoriteDatabase.COLUMN_LAST_LOGOUT, lastLogout);
        int rows = db.update(FavoriteDatabase.TABLE_USERS, values,
                FavoriteDatabase.COLUMN_USER_ID + "=?", new String[]{userId});
        db.close();
        return rows > 0;
    }

    // ✅ Registrar en Firestore
    public void registrarFirestore(String userId, String login, String logout) {
        CollectionReference usersRef = db.collection("users");
        Map<String, Object> userData = new HashMap<>();
        userData.put("lastLogin", login);
        userData.put("lastLogout", logout);

        usersRef.document(userId).set(userData)
                .addOnSuccessListener(aVoid -> Log.d("FavoritesManager", "Usuario sincronizado con Firestore"))
                .addOnFailureListener(e -> Log.e("FavoritesManager", "Error al sincronizar con Firestore", e));
    }

    public void registerLogin(String userId, String loginTime) {
        SQLiteDatabase db = fBD.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FavoriteDatabase.COLUMN_LAST_LOGIN, loginTime);
        db.update(FavoriteDatabase.TABLE_USERS, values, FavoriteDatabase.COLUMN_USER_ID + "=?", new String[]{userId});
        db.close();
    }

    public void registerLogout(String userId, String logoutTime) {
        SQLiteDatabase db = fBD.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FavoriteDatabase.COLUMN_LAST_LOGOUT, logoutTime);
        db.update(FavoriteDatabase.TABLE_USERS, values, FavoriteDatabase.COLUMN_USER_ID + "=?", new String[]{userId});
        db.close();
    }

}
