package edu.pmdm.vegas_laraimdbapp.api;

import android.util.Log;
import java.util.ArrayList;
import java.util.List;

/**
 * Manager para gestionar las API Keys de RapidAPI.
 */
public class RapidApiKeyManager {

    // Lista de API Keys
    private static final List<String> apiKeys = new ArrayList<>();
    private static int currentKeyIndex = 0;

    // Bloque estático para inicializar API Keys automáticamente
    static {
        initializeKeys();
    }

    /**
     * Inicializa las API Keys.
     */
    private static void initializeKeys() {
        if (apiKeys.isEmpty()) {
            apiKeys.add("9c04b1a854msh2056acaabc5ce24p142c9djsnbdd53d066b65");
            apiKeys.add("e5ee42f022msh9ad047aadbacaf3p1149a6jsne6411b65c0ea");

            Log.d("RapidApiKeyManager", "API Keys inicializadas: " + apiKeys.size());
        }
    }

    /**
     * Obtiene la API Key actual.
     * @return La API Key actual.
     */
    public static String getApiKey() {
        if (apiKeys.isEmpty()) {
            Log.e("RapidApiKeyManager", "No hay API Keys disponibles.");
            throw new IllegalStateException("No hay API Keys disponibles. Verifica RapidApiKeyManager.");
        }
        Log.d("RapidApiKeyManager", "Usando API Key: " + apiKeys.get(currentKeyIndex));
        return apiKeys.get(currentKeyIndex);
    }

    /**
     * Cambia a la siguiente API Key en caso de que la actual haya alcanzado su límite.
     */
    public static void switchToNextApiKey() {
        if (apiKeys.isEmpty()) {
            Log.e("RapidApiKeyManager", "No hay API Keys disponibles para cambiar.");
            throw new IllegalStateException("No hay API Keys disponibles para cambiar.");
        }
        currentKeyIndex = (currentKeyIndex + 1) % apiKeys.size();
        Log.d("RapidApiKeyManager", "Cambiando a nueva API Key: " + apiKeys.get(currentKeyIndex));
    }
}
