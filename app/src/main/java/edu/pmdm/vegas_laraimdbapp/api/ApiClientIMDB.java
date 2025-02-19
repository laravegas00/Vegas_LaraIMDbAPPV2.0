package edu.pmdm.vegas_laraimdbapp.api;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Clase para configurar el cliente Retrofit para la API de IMDB con múltiples API Keys.
 */
public class ApiClientIMDB {

    // URL base de la API
    private static final String BASE_URL = "https://imdb-com.p.rapidapi.com";
    private static Retrofit retrofit; // Instancia de Retrofit

    /**
     * Obtiene una instancia de Retrofit configurada con el manejo de múltiples API Keys.
     * @return Instancia de Retrofit.
     */
    public static Retrofit getClient() {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request original = chain.request();
                            Response response = null;
                            int retryCount = 0;

                            while (retryCount < 2) { // Intentamos solo una vez más si falla con 429
                                String apiKey = RapidApiKeyManager.getApiKey();
                                Request request = original.newBuilder()
                                        .header("X-RapidAPI-Key", apiKey) // API Key actual
                                        .header("X-RapidAPI-Host", "imdb-com.p.rapidapi.com")
                                        .build();

                                response = chain.proceed(request);

                                if (response.code() == 429) { // Si la API Key está limitada
                                    RapidApiKeyManager.switchToNextApiKey(); // Cambia a la siguiente API Key
                                    retryCount++; // Aumentamos el contador de reintentos
                                } else {
                                    break; // Si la respuesta es válida, salimos del bucle
                                }
                            }

                            return response;
                        }
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
