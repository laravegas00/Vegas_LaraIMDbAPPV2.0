package edu.pmdm.vegas_laraimdbapp.api;

import java.util.ArrayList;
import java.util.List;

public class RapidApiKeyManager {

    private static final List<String> apiKeys = new ArrayList<>();
    private static int currentKeyIndex = 0;

    public RapidApiKeyManager() {
        // Añade aquí tus claves de RapidAPI
        apiKeys.add("9c04b1a854msh2056acaabc5ce24p142c9djsnbdd53d066b65");
        apiKeys.add("e5ee42f022msh9ad047aadbacaf3p1149a6jsne6411b65c0ea");
        apiKeys.add("TU_API_KEY_3");
    }

    /**
     * Obtiene la API Key actual.
     * @return La API Key actual.
     */
    public static String getApiKey() {
        return apiKeys.get(currentKeyIndex);
    }

    /**
     * Cambia a la siguiente API Key en caso de que la actual haya alcanzado su límite.
     */
    public static void switchToNextApiKey() {
        currentKeyIndex = (currentKeyIndex + 1) % apiKeys.size();
    }

}
