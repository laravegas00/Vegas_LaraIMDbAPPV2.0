package edu.pmdm.vegas_laraimdbapp.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Clase para configurar el cliente Retrofit para la API de TMDB.
 */
public class ApiClientTMDB {

    // URL base de la API
    private static final String BASE_URL = "https://api.themoviedb.org/3/";
    private static Retrofit retrofit; // Instancia de Retrofit

    /**
     * Obtiene una instancia de Retrofit configurada.
     * @return Instancia de Retrofit.
     */
    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

}
