package edu.pmdm.vegas_laraimdbapp.models;

/**
 * Clase que representa los datos obtenidos de la API
 */
public class MovieOverviewResponse {

    // Objeto que contiene los datos de la película
    private Data data;

    /**
     * Obtiene los datos de la respuesta de la API.
     * @return Objeto con la información de las películas populares.
     */
    public Data getData() {
        return data;
    }

    /**
     * Clase interna que almacena la información principal de la película
     */
    public static class Data {
        private Title title; // Objeto con los detalles del título de la película

        /**
         * Método para obtener los detalles del título
         * @return Titulo de la pelicula
         */
        public Title getTitle() {
            return title;
        }
    }

    /**
     * Clase interna que representa los detalles de una película
     */
    public static class Title {
        private String id; // Identificador único de la película
        private TitleText titleText; // Título de la película
        private Plot plot; // Descripción de la película
        private ReleaseDate releaseDate; // Fecha de estreno
        private RatingsSummary ratingsSummary; // Puntuación de la película
        private PrimaryImage primaryImage; // Imagen principal de la película

        /**
         * Método que recoge el ID de la pelicula
         * @return ID de la película
         */
        public String getId() {
            return id;
        }

        /**
         * Método que recoge el titulo de la pelicula
         * @return Título de la película o "Sin título" si no está disponible
         */
        public String getTitleText() {
            return titleText != null ? titleText.getText() : "Sin título";
        }

        /**
         * Método que recoge la descripción de la pelicula
         * @return Sinopsis de la película o "No hay sinopsis disponible" si no está disponible
         */
        public String getPlotText() {
            if (plot == null) {
                return "No hay sinopsis disponible";
            }

            return plot.getPlotText().getPlainText();

        }

        /**
         * Método que recoge la fecha de estreno de la pelicula y la formatea
         * @return Fecha de la pelicula formateada o "Fecha no disponible"
         */
        public String getReleaseDateString() {
            return releaseDate != null ? releaseDate.getYear() + "-" + releaseDate.getMonth() + "-" + releaseDate.getDay() : "Fecha no disponible";
        }

        /**
         * Método que recoge la puntuación de la pelicula
         * @return Rating de la pelicula o 0.0 si no está disponible
         */
        public double getRating() {
            return ratingsSummary != null ? ratingsSummary.getAggregateRating() : 0.0;
        }

        /**
         * Método para coger la imagen de la pelicula
         * @return URL de la imagen de la película o una cadena vacía si no está disponible
         */
        public String getImageUrl() {
            return primaryImage != null ? primaryImage.getUrl() : "";
        }
    }

    //

    /**
     * Clase interna que almacena el título de la película
     */
    public static class TitleText {
        private String text; // Título de la película

        /**
         * Método que recoge el titulo de la pelicula
         * @return Título de la película
         */
        public String getText() {
            return text;
        }
    }

    /**
     * Clase interna que representa la sinopsis de la película
     */
    public static class Plot {
        private PlotText plotText; // Texto de la sinopsis

        /**
         * Método que recoge la sinopsis de la película
         * @return Sinopsis de la película
         */
        public PlotText getPlotText() {
            return plotText;
        }

        /**
         * Clase interna que almacena la sinopsis en formato de texto plano
         */
        public static class PlotText {
            private String plainText; // Texto de la sinopsis

            /**
             * Método que recoge la sinopsis
             * @return Sinopsis en texto plano
             */
            public String getPlainText() {
                return plainText;
            }
        }
    }

    /**
     * Clase que almacena la fecha de estreno de la película
     */
    public static class ReleaseDate {
        private int year; // Año de estreno
        private int month; // Mes de estreno
        private int day; // Día de estreno

        /**
         * Método que recoge el año
         * @return Devuelve el año de estreno
         */
        public int getYear() {
            return year; // Devuelve el año de estreno
        }

        /**
         * Método que recoge el mes
         * @return Devuelve el mes de estreno
         */
        public int getMonth() {
            return month; // Devuelve el mes de estreno
        }

        /**
         * Método que recoge el dia
         * @return Devuelve el día de estreno
         */
        public int getDay() {
            return day; // Devuelve el día de estreno
        }
    }

    /**
     * Clase que almacena la puntuación de la película
     */
    public static class RatingsSummary {
        private double aggregateRating; // Puntuación promedio de la película

        /**
         * Método que recoge el rating de la pelicula
         * @return Puntuacion promedio
         */
        public double getAggregateRating() {
            return aggregateRating;
        }
    }

    /**
     * Clase que almacena la imagen principal de la película
     */
    public static class PrimaryImage {
        private String url; // URL de la imagen de la película

        /**
         * Método que recoge la URL de la imagen
         * @return URL de la imagen
         */
        public String getUrl() {
            return url;
        }
    }
}
