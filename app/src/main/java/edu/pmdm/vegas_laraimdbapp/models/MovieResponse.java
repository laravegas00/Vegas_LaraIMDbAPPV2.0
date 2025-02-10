package edu.pmdm.vegas_laraimdbapp.models;

import java.util.List;

/**
 * Clase que representa la respuesta de la API cuando se solicitan las películas más populares.
 */
public class MovieResponse {
    private Data data; // Contiene toda la información de la respuesta

    /**
     * Obtiene los datos de la respuesta de la API.
     * @return Objeto con la información de las películas populares.
     */
    public Data getData() {
        return data;
    }

    /**
     * Clase interna que almacena la información de la respuesta.
     */
    public static class Data {
        private TopMeterTitles topMeterTitles; // Contiene la lista de películas populares

        /**
         * Obtiene la lista de películas populares.
         * @return Lista de películas populares.
         */
        public TopMeterTitles getTopMeterTitles() {
            return topMeterTitles;
        }
    }

    /**
     * Clase que representa la lista de películas populares.
     */
    public static class TopMeterTitles {
        private List<Edge> edges; // Lista de películas

        /**
         * Obtiene la lista de películas populares en forma de nodos.
         * @return Lista de objetos con la información de cada película.
         */
        public List<Edge> getEdges() {
            return edges;
        }
    }

    /**
     * Clase que representa un nodo individual en la lista de películas populares.
     */
    public static class Edge {
        private Node node; // Nodo que representa una película individual

        /**
         * Obtiene el nodo con los detalles de una película.
         * @return Objeto con la información de la película.
         */
        public Node getNode() {
            return node;
        }
    }

    /**
     * Clase que almacena los detalles de una película individual.
     */
    public static class Node {
        private String id; // Identificador único de la película
        private TitleText titleText; // Título de la película
        private Plot plot; // Sinopsis o descripción de la película
        private ReleaseDate releaseDate; // Fecha de estreno de la película
        private RatingsSummary ratingsSummary; // Puntuación de la película
        private PrimaryImage primaryImage; // Imagen principal de la película

        /**
         * Obtiene el ID de la película.
         * @return ID de la película.
         */
        public String getId() {
            return id;
        }

        /**
         * Obtiene el título de la película.
         * @return Título de la película.
         */
        public String getTitleText() {
            return titleText.getText();
        }

        /**
         * Obtiene la sinopsis de la película.
         * @return Sinopsis de la película o un mensaje indicando que no está disponible.
         */
        public String getPlotText() {
            return plot == null ? "No hay sinopsis disponible" : plot.getPlainText();
        }

        /**
         * Obtiene la fecha de estreno de la película.
         * @return Fecha de estreno formateada como "YYYY-MM-DD".
         */
        public String getReleaseDateString() {
            return releaseDate.getYear() + "-" + releaseDate.getMonth() + "-" + releaseDate.getDay();
        }

        /**
         * Obtiene la puntuación de la película.
         * @return Puntuación promedio de la película o 0.0 si no está disponible.
         */
        public double getRating() {
            return ratingsSummary == null ? 0.0 : ratingsSummary.getAggregateRating();
        }

        /**
         * Obtiene la URL de la imagen de la película.
         * @return URL de la imagen o cadena vacía si no hay imagen disponible.
         */
        public String getImageUrl() {
            return primaryImage != null ? primaryImage.getUrl() : "";
        }
    }

    /**
     * Clase que almacena el título de la película.
     */
    public static class TitleText {
        private String text; // Texto del título

        /**
         * Obtiene el título de la película.
         * @return Texto del título.
         */
        public String getText() {
            return text;
        }
    }

    /**
     * Clase que almacena la sinopsis de la película.
     */
    public static class Plot {
        private String plainText; // Descripción de la película en texto plano

        /**
         * Obtiene la sinopsis de la película.
         * @return Texto de la sinopsis.
         */
        public String getPlainText() {
            return plainText;
        }
    }

    /**
     * Clase que almacena la fecha de estreno de la película.
     */
    public static class ReleaseDate {
        private int year; // Año de estreno
        private int month; // Mes de estreno
        private int day; // Día de estreno

        /**
         * Obtiene el año de estreno de la película.
         * @return Año de estreno como entero.
         */
        public int getYear() {
            return year;
        }

        /**
         * Obtiene el mes de estreno de la película.
         * @return Mes de estreno como entero.
         */
        public int getMonth() {
            return month;
        }

        /**
         * Obtiene el día de estreno de la película.
         * @return Día de estreno como entero.
         */
        public int getDay() {
            return day;
        }
    }

    /**
     * Clase que almacena la puntuación de la película.
     */
    public static class RatingsSummary {
        private double aggregateRating; // Puntuación promedio de la película

        /**
         * Obtiene la puntuación promedio de la película.
         * @return Puntuación promedio como número decimal.
         */
        public double getAggregateRating() {
            return aggregateRating;
        }
    }

    /**
     * Clase que almacena la imagen principal de la película.
     */
    public static class PrimaryImage {
        private String url; // URL de la imagen de la película

        /**
         * Obtiene la URL de la imagen de la película.
         * @return URL de la imagen.
         */
        public String getUrl() {
            return url;
        }
    }
}
