package edu.pmdm.vegas_laraimdbapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import edu.pmdm.vegas_laraimdbapp.models.Movie;

/**
 * Clase para manejar la base de datos de películas favoritas.
 */
public class FavoriteDatabase extends SQLiteOpenHelper {

    //Constantes para la base de datos
    private static final String DATABASE_NAME = "favoritesmovies.db";
    private static final int DATABASE_VERSION = 3;

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

    /**
     * Constructor de la clase
     * @param context Contexto de la aplicación
     */
    public FavoriteDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
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
    }

    /**
     * Actualizar la estructura de la tabla de películas favoritas
     * @param db Base de datos
     * @param oldVersion Versión anterior de la base de datos
     * @param newVersion Versión actual de la base de datos
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_FAVORITES + " ADD COLUMN " + COLUMN_USERID + " TEXT DEFAULT 'unknown_user'");
                Log.d("Database Upgrade", "Columna 'userid' añadida.");
            } catch (Exception e) {
                Log.e("Database Upgrade", "Error al agregar 'userid': " + e.getMessage());
            }
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
}
