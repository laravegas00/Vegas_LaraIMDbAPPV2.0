package edu.pmdm.vegas_laraimdbapp.ui.slideshow;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import edu.pmdm.vegas_laraimdbapp.MovieListActivity;
import edu.pmdm.vegas_laraimdbapp.api.ApiClientTMDB;
import edu.pmdm.vegas_laraimdbapp.api.TMDBApiService;
import edu.pmdm.vegas_laraimdbapp.databinding.FragmentSlideshowBinding;
import edu.pmdm.vegas_laraimdbapp.models.TMDBGenreResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Fragmento que muestra la pantalla de búsqueda de películas.
 */
public class SearchFragment extends Fragment {

    // Declaración de variables
    private FragmentSlideshowBinding binding;
    private Spinner spinnerGenres;
    private EditText etYear;
    private Button btnSearch;
    private TMDBApiService apiService;
    private List<TMDBGenreResponse.Genre> genreList = new ArrayList<>();
    private String selectedGenreId = "";

    // Constantes para el rango de años
    private final int MIN_YEAR = 1900;
    private final int MAX_YEAR = Calendar.getInstance().get(Calendar.YEAR); // Año actual dinámico

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSlideshowBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        spinnerGenres = binding.spinnerGenres; // Asignamos el Spinner
        etYear = binding.etYear; // Asignamos el EditText
        btnSearch = binding.btnSearch; // Asignamos el botón

        apiService = ApiClientTMDB.getClient().create(TMDBApiService.class); // Inicializamos el servicio de la API

        // Cargar géneros desde la API de TMDB
        loadGenres();

        // Configurar el botón de búsqueda con validación del año
        btnSearch.setOnClickListener(v -> {
            String yearInput = etYear.getText().toString().trim(); // Obtener el año ingresado

            // Validar que el campo no esté vacío y que sea un número válido dentro del rango
            if (!isValidYear(yearInput)) {
                Toast.makeText(getContext(), "Ingrese un año válido entre " + MIN_YEAR + " y " + MAX_YEAR, Toast.LENGTH_SHORT).show();
                return;
            }

            // Validar que se haya seleccionado un género
            if (selectedGenreId.isEmpty()) {
                Toast.makeText(getContext(), "Seleccione un género", Toast.LENGTH_SHORT).show();
                return;
            }

            // Iniciar MovieListActivity con los datos seleccionados
            Intent intent = new Intent(getContext(), MovieListActivity.class);
            intent.putExtra("genreId", selectedGenreId);
            intent.putExtra("year", yearInput);
            startActivity(intent);
        });

        return root;
    }

    /**
     * Método para cargar los géneros de películas desde la API de TMDB y llenar el Spinner.
     */
    private void loadGenres() {
        apiService.getGenres("es-US").enqueue(new Callback<TMDBGenreResponse>() {
            @Override
            public void onResponse(Call<TMDBGenreResponse> call, Response<TMDBGenreResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    genreList = response.body().getGenres(); // Obtener la lista de géneros
                    List<String> genreNames = new ArrayList<>(); // Crear una lista de nombres de géneros
                    // Llenar la lista con los nombres de los géneros
                    for (TMDBGenreResponse.Genre genre : genreList) {
                        genreNames.add(genre.getName());
                    }

                    // Configurar el Spinner con los nombres de los géneros
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, genreNames);
                    spinnerGenres.setAdapter(adapter); // Asignar el adaptador al Spinner

                    // Configurar el listener para obtener el ID del género seleccionado
                    spinnerGenres.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedGenreId = String.valueOf(genreList.get(position).getId());
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) { }
                    });
                }
            }

            @Override
            public void onFailure(Call<TMDBGenreResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Error al cargar géneros", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Método para validar si el año ingresado es un número válido dentro del rango permitido.
     * @param yearInput Cadena de texto ingresada por el usuario.
     * @return True si el año es válido, False si no lo es.
     */
    private boolean isValidYear(String yearInput) {
        if (yearInput.isEmpty()) {
            return false;
        }

        try {
            int year = Integer.parseInt(yearInput);
            return year >= MIN_YEAR && year <= MAX_YEAR; // Validar el rango
        } catch (NumberFormatException e) {
            return false; // No es un número válido
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
