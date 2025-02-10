package edu.pmdm.vegas_laraimdbapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.squareup.picasso.Picasso;

import android.Manifest;

import edu.pmdm.vegas_laraimdbapp.api.ApiClientIMDB;
import edu.pmdm.vegas_laraimdbapp.api.IMDBApiService;
import edu.pmdm.vegas_laraimdbapp.models.MovieOverviewResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Actividad para mostrar los detalles de una película.
 */
public class MovieDetailsActivity extends AppCompatActivity {

    // Constantes para los permisos
    private static final int REQUEST_SMS_PERMISSION = 100;
    private static final int REQUEST_CONTACT_PERMISSION = 101;

    // Variables para almacenar los detalles de la película
    private String movieDetails = "";

    //Declarar las variables
    private TextView titleTextView;
    private TextView plotTextView;
    private TextView releaseDateTextView;
    private TextView ratingTextView;
    private ImageView posterImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Inicialización de las vistas
        titleTextView = findViewById(R.id.titleTextView);
        posterImageView = findViewById(R.id.posterImageView);
        plotTextView = findViewById(R.id.plotTextView);
        releaseDateTextView = findViewById(R.id.releaseDateTextView);
        ratingTextView = findViewById(R.id.ratingTextView);

        // Recibir datos del Intent
        Intent intent = getIntent();
        String movieId = intent.getStringExtra("id");
        String title = intent.getStringExtra("title");
        String imageUrl = intent.getStringExtra("imageUrl");
        String releaseDate = intent.getStringExtra("releaseDate");
        String plot = intent.getStringExtra("plot");
        double rating = intent.getDoubleExtra("rating", -1.0);
        boolean TMDB = intent.getBooleanExtra("TMDB", false); // Recibir el valor de TMDB

        // Validaciones iniciales
        if (movieId == null || movieId.isEmpty()) {
            showError("No se recibió un ID de película válido.");
            finish();
            return;
        }

        // Mostrar detalles de la película
        titleTextView.setText(title != null ? title : "Título no disponible");
        releaseDateTextView.setText("Fecha de lanzamiento: " + (releaseDate != null ? releaseDate : "No disponible"));

        // Mostrar imagen de la película
        if (imageUrl == null || imageUrl.isEmpty()) {
            Picasso.get().load(R.drawable.googlelogo).into(posterImageView);
        } else {
            Picasso.get().load(imageUrl).into(posterImageView);
        }

        plotTextView.setText(plot != null ? plot : "Descripción no disponible");
        ratingTextView.setText(String.format("Puntuación: %.1f", rating));

        // Llamada a la API para obtener los detalles
        if (!TMDB) {
            fetchMovieDetails(movieId);
        }

        // Configurar el botón para enviar SMS
        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        Button sendSmsButton = findViewById(R.id.btn_send_sms);
        sendSmsButton.setOnClickListener(v -> checkContactPermission()); // Llamada al método de envío de SMS

    }

    /**
     * Método para obtener los detalles de una película a través de la API.
     * @param movieId El ID de la película.
     */
    private void fetchMovieDetails(String movieId) {

        // Crear una instancia del servicio de la API
        IMDBApiService apiService = ApiClientIMDB.getClient().create(IMDBApiService.class);

        apiService.getMovieDetails(movieId).enqueue(new Callback<MovieOverviewResponse>() {
            @Override
            public void onResponse(Call<MovieOverviewResponse> call, Response<MovieOverviewResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MovieOverviewResponse.Title movieTitle = response.body().getData().getTitle(); // Obtener el título de la película
                    if (movieTitle != null) { // Verificar si el título no es nulo
                        String plot = movieTitle.getPlotText(); // Obtener la descripción de la película
                        double rating = movieTitle.getRating(); // Obtener la puntuación de la película

                        // Actualizar las vistas con los detalles
                        plotTextView.setText(plot != null ? plot : "Descripción no disponible");
                        ratingTextView.setText(String.format("Puntuación: %.1f", rating));

                        // Construir el mensaje de detalles de la película para enviar SMS
                        movieDetails = "¡NOVEDAD!\n¡No te pierdas esta película!\n" +
                                "Título: " + movieTitle.getTitleText() + "\n" +
                                "Descripción: " + (plot != null ? plot : "No disponible") + "\n" +
                                "Fecha de lanzamiento: " + movieTitle.getReleaseDateString() + "\n" +
                                "Puntuación: " + String.format("%.1f", rating);
                    } else {
                        showError("Error al cargar los detalles de la película.");
                    }
                } else {
                    showError("No se pudo obtener los detalles de la película. Código: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<MovieOverviewResponse> call, Throwable t) {
                showError("Error de conexión: " + t.getMessage());
            }
        });
    }

    /**
     * Método para verificar el permiso de lectura de contactos.
     */
    private void checkContactPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) // Verificar el permiso
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, // Solicitar el permiso
                    new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_CONTACT_PERMISSION);
        } else {
            pickContact();
        }
    }

    /**
     * Método para seleccionar un contacto.
     */
    private void pickContact() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI); // Crear un intent para seleccionar un contacto
        pickContactLauncher.launch(intent); // Iniciar la actividad para seleccionar un contacto
    }

    /**
     * Launcher para la actividad de selección de contacto.
     */
    private final ActivityResultLauncher<Intent> pickContactLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        // Manejar el resultado de la actividad de selección de contacto
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            Uri contactUri = result.getData().getData();
                            String contactNumber = getContactNumber(contactUri);

                            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                                    != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(this,
                                        new String[]{Manifest.permission.SEND_SMS}, REQUEST_SMS_PERMISSION);
                            } else {
                                sendSMS(contactNumber, movieDetails); // Enviar SMS con los detalles de la película
                            }
                        } else {
                            showError("No se seleccionó ningún contacto.");
                        }
                    });

    /**
     * Método para obtener el número de contacto.
     * @param contactUri La URI del contacto.
     * @return El número de contacto.
     */
    private String getContactNumber(Uri contactUri) {
        String number = ""; // Inicializar la variable

        // Realizar una consulta para obtener el número de contacto
        try (Cursor cursor = getContentResolver().query(contactUri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                number = cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            showError("Error al obtener el número de contacto: " + e.getMessage());
        }
        return number;
    }

    /**
     * Método para enviar un SMS.
     * @param phoneNumber El número de teléfono del contacto.
     * @param message El mensaje a enviar.
     */
    private void sendSMS(String phoneNumber, String message) {

        // Validar el número de teléfono
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            showError("Número de teléfono no válido.");
            return;
        }

        // Construir el intent para enviar SMS
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("smsto:" + phoneNumber));
        intent.putExtra("sms_body", message);

        // Iniciar la actividad para enviar SMS
        try {
            startActivity(intent);
        } catch (Exception e) {
            showError("Error al enviar SMS: " + e.getMessage());
        }
    }

    /**
     * Método para mostrar un mensaje de error.
     * @param message El mensaje de error.
     */
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Método para manejar la respuesta de los permisos.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Manejar la respuesta de los permisos según el código de solicitud
        if (requestCode == REQUEST_CONTACT_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            pickContact();
        } else if (requestCode == REQUEST_SMS_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permiso de SMS concedido. Selecciona un contacto nuevamente.", Toast.LENGTH_SHORT).show();
        } else {
            showError("Permiso denegado.");
        }
    }
}
