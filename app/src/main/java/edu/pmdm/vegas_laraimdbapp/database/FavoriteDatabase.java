package edu.pmdm.vegas_laraimdbapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.pmdm.vegas_laraimdbapp.models.Movie;

/**
 * Clase para manejar la base de datos de películas favoritas.
 */
public class FavoriteDatabase extends SQLiteOpenHelper {

    private final Context context;

    //Constantes para la base de datos
    private static final String DATABASE_NAME = "favoritesmovies.db";
    private static final int DATABASE_VERSION = 5;

    //Constantes para la tabla de películas favoritas
    private static final String TABLE_FAVORITES = "favorites";

    //Constantes para las columnas de la tabla de películas favoritas
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_IMAGEURL = "imageUrl";
    private static final String COLUMN_RELEASEDATE = "releaseDate";
    private static final String COLUMN_PLOT = "plot";
    private static final String COLUMN_RATING = "rating";
    private static final String COLUMN_USERID = "userId";

    // Nueva tabla para usuarios
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "userId";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_LAST_LOGIN = "last_login";
    public static final String COLUMN_LAST_LOGOUT = "last_logout";

    /**
     * Constructor de la clase
     * @param context Contexto de la aplicación
     */
    public FavoriteDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    /**
     * Crear la tabla de películas favoritas
     * @param db Base de datos
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_FAVORITES + " (" +
                COLUMN_ID + " TEXT, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_IMAGEURL + " TEXT, " +
                COLUMN_RELEASEDATE + " TEXT, " +
                COLUMN_PLOT + " TEXT, " +
                COLUMN_RATING + " REAL, " +
                COLUMN_USERID + " TEXT, " +
                "PRIMARY KEY (" + COLUMN_ID + ", " + COLUMN_USERID + "))";
        db.execSQL(createTable);

        // Crear tabla de usuarios
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_USER_ID + " TEXT PRIMARY KEY, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_EMAIL + " TEXT, " +
                COLUMN_LAST_LOGIN + " TEXT, " +
                COLUMN_LAST_LOGOUT + " TEXT" +
                ");";
        db.execSQL(createUsersTable);

    }

    /**
     * Actualizar la estructura de la tabla de películas favoritas
     * @param db Base de datos
     * @param oldVersion Versión anterior de la base de datos
     * @param newVersion Versión actual de la base de datos
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 5) {
            String createUsersTable = "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " (" +
                    COLUMN_USER_ID + " TEXT PRIMARY KEY, " +
                    COLUMN_NAME + " TEXT, " +
                    COLUMN_EMAIL + " TEXT, " +
                    COLUMN_LAST_LOGIN + " TEXT, " +
                    COLUMN_LAST_LOGOUT + " TEXT" +
                    ");";
            db.execSQL(createUsersTable);
        }
    }

    /**
     * Obtener todas las películas favoritas de un usuario
     * @param userId ID del usuario
     * @return Lista de películas favoritas
     */
    public List<Movie> getAllFavorites(String userId) {

        List<Movie> favoriteMovies = new ArrayList<>(); // Lista para almacenar las películas favoritas
        SQLiteDatabase db = this.getReadableDatabase(); // Obtener la base de datos de lectura
        Cursor cursor = null; // Cursor para recorrer los resultados de la consulta

        // Consulta para obtener todas las películas favoritas del usuario
        try {
            String selection = COLUMN_USERID + " = ?"; // Condición de selección para un usuario específico
            String[] selectionArgs = {userId}; // Argumentos de la condición de selección

            //Ejecutar la consulta
            cursor = db.query(TABLE_FAVORITES, null, selection, selectionArgs, null, null, null);
            if (cursor != null && cursor.moveToFirst()) { // Recorrer los resultados
                do {
                    //Crear el objeto pelicula
                    Movie movie = new Movie();
                    movie.setId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                    movie.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
                    movie.setImage(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGEURL)));
                    movie.setReleaseDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RELEASEDATE)));
                    movie.setPlot(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PLOT)));
                    movie.setRating(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_RATING)));

                    favoriteMovies.add(movie); // Agregar la película a la lista

                } while (cursor.moveToNext()); // Avanzar al siguiente resultado
            }
        } catch (Exception e) {
            Log.e("FavoriteDatabase", "Error al obtener favoritos: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close(); // Cerrar el cursor si no es nulo
        }

        return favoriteMovies; // Devolver la lista de películas favoritas
    }

    /**
     * Verificar si una película ya está en la lista de favoritos
     * @param movieId ID de la película
     * @param userId ID del usuario
     * @return True si la película está en favoritos, false en caso contrario
     */
    public boolean movieExists(String movieId, String userId) {

        SQLiteDatabase db = this.getReadableDatabase(); // Obtener la base de datos de lectura
        String selection = COLUMN_ID + " = ? AND " + COLUMN_USERID + " = ?"; // Condición de selección para un usuario específico
        String[] selectionArgs = {movieId, userId}; // Argumentos de la condición de selección

        //Ejecutar la consulta
        Cursor cursor = db.query(TABLE_FAVORITES, null, selection, selectionArgs, null, null, null);
        boolean exists = (cursor != null && cursor.getCount() > 0); // Verificar si se encontraron resultados

        if (cursor != null) cursor.close(); // Cerrar el cursor si no es nulo
        return exists; // Devolver el resultado de la verificación
    }

    /**
     * Agregar una película a la lista de favoritos
     * @param movie Objeto de la película
     * @param userId ID del usuario
     */
    public void addFavorite(Movie movie, String userId) {

        // Verificar si la película ya está en favoritos
        if (movieExists(movie.getId(), userId)) {
            return;
        }

        SQLiteDatabase db = this.getWritableDatabase(); // Obtener la base de datos de escritura
        ContentValues values = new ContentValues(); // Crear un objeto ContentValues para los valores a insertar

        // Insertar los valores en los campos de la tabla
        values.put(COLUMN_ID, movie.getId());
        values.put(COLUMN_TITLE, movie.getTitle());
        values.put(COLUMN_IMAGEURL, movie.getImage());
        values.put(COLUMN_RELEASEDATE, movie.getReleaseDate());
        values.put(COLUMN_PLOT, movie.getPlot());
        values.put(COLUMN_RATING, movie.getRating());
        values.put(COLUMN_USERID, userId);

        try {
            db.insert(TABLE_FAVORITES, null, values);
            Log.d("FavoriteDatabase", "Película agregada con éxito: " + movie.getId());
        } catch (Exception e) {
            Log.e("FavoriteDatabase", "Error al agregar película: " + e.getMessage());
        }
    }

    /**
     * Eliminar una película de la lista de favoritos
     * @param movieId ID de la película
     * @param userId ID del usuario
     */
    public void removeFavorite(String movieId, String userId) {
        SQLiteDatabase db = this.getWritableDatabase(); // Obtener la base de datos de escritura
        try {
            String whereClause = COLUMN_ID + " = ? AND " + COLUMN_USERID + " = ?"; // Condición de eliminación
            String[] whereArgs = {movieId, userId}; // Argumentos de la condición de eliminación

            db.delete(TABLE_FAVORITES, whereClause, whereArgs);// Ejecutar la eliminación

        } catch (Exception e) {
            Log.e("FavoriteDatabase", "Error al eliminar película: " + e.getMessage());
        }
    }

    public void addUser(String userId, String name, String email, String lastLogin, String lastLogout) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_LAST_LOGIN, lastLogin);
        values.put(COLUMN_LAST_LOGOUT, lastLogout);

        try {
            long result = db.insert(TABLE_USERS, null, values);
            if (result == -1) {
                Log.e("FavoriteDatabase", "Error al agregar el usuario: " + userId);
            } else {
                Log.d("FavoriteDatabase", "Usuario agregado con éxito: " + userId);
            }
        } catch (Exception e) {
            Log.e("FavoriteDatabase", "Error al insertar usuario: " + e.getMessage());
        }
    }

    public void updateUser(String userId, String name, String email, String lastLogin, String lastLogout) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        if (name != null) values.put(COLUMN_NAME, name);
        if (email != null) values.put(COLUMN_EMAIL, email);
        if (lastLogin != null) values.put(COLUMN_LAST_LOGIN, lastLogin);
        if (lastLogout != null) values.put(COLUMN_LAST_LOGOUT, lastLogout);

        db.update(TABLE_USERS, values, COLUMN_USER_ID + "=?", new String[]{userId});
    }

    public boolean userExists(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_USER_ID + " = ?";
        String[] selectionArgs = {userId};

        Cursor cursor = db.query(TABLE_USERS, null, selection, selectionArgs, null, null, null);
        boolean exists = (cursor != null && cursor.getCount() > 0);

        if (cursor != null) {
            cursor.close();
        }
        return exists;
    }

    public void registerLogin(String userId, String loginTime) {
        if (userExists(userId)) {
            updateUser(userId, null, null, loginTime, null);
        } else {
            addUser(userId, null, null, loginTime, null);
        }
    }

    public void registerLogout(String userId, String logoutTime) {
        if (userExists(userId)) {
            updateUser(userId, null, null, null, logoutTime);
        } else {
            addUser(userId, null, null, null, logoutTime);
        }
    }

}
