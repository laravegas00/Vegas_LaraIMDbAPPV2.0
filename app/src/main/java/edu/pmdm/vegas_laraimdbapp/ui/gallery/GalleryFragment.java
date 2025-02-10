package edu.pmdm.vegas_laraimdbapp.ui.gallery;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import edu.pmdm.vegas_laraimdbapp.LogInActivity;
import edu.pmdm.vegas_laraimdbapp.MovieDetailsActivity;
import edu.pmdm.vegas_laraimdbapp.R;
import edu.pmdm.vegas_laraimdbapp.adapter.MovieAdapter;
import edu.pmdm.vegas_laraimdbapp.bluetooth.BluetoothSimulator;
import edu.pmdm.vegas_laraimdbapp.database.FavoritesManager;
import edu.pmdm.vegas_laraimdbapp.models.Movie;

/**
 * Fragmento que muestra las películas favoritas del usuario.
 */
public class GalleryFragment extends Fragment {

    //Declarar variables
    private RecyclerView recyclerView;
    private MovieAdapter movieAdapter;
    private BluetoothAdapter bluetoothAdapter;
    private ActivityResultLauncher<Intent> enableBluetoothLauncher;
    private FavoritesManager favoritesManager;
    private String userId;
    private List<Movie> favoriteMovies = new ArrayList<>();

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);

        recyclerView = root.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        enableBluetoothLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Toast.makeText(getContext(), "Bluetooth habilitado.", Toast.LENGTH_SHORT).show();
                        shareFavoritesViaBluetooth(); // Compartir automáticamente si Bluetooth se habilita
                    } else {
                        Toast.makeText(getContext(), "No se puede compartir sin Bluetooth.", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Obtener el ID del usuario actual
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("USER_ID", null);

        if (userId == null) {
            Toast.makeText(getContext(), "Usuario no autenticado. Por favor, inicia sesión.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getContext(), LogInActivity.class);
            startActivity(intent);
            return root;
        }

        Log.d("GalleryFragment", "Obtenido userId: " + userId);

        // Cargar películas favoritas desde la base de datos
        favoritesManager = FavoritesManager.getInstance(getContext());
        favoriteMovies = favoritesManager.getFavoriteMovies(userId);

        movieAdapter = new MovieAdapter(getContext(), favoriteMovies, this::onMovieClick);
        movieAdapter.setOnMovieLongClickListener(this::onMovieLongClick);
        recyclerView.setAdapter(movieAdapter);
        movieAdapter.notifyDataSetChanged();

        Button shareButton = root.findViewById(R.id.btnShare);
        shareButton.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                requestBluetoothPermissions();
            } else {
                shareFavoritesViaBluetooth();
            }
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        favoriteMovies = favoritesManager.getFavoriteMovies(userId);
        movieAdapter.updateMovies(favoriteMovies);
    }

    /**
     * Maneja el clic en una película.
     * @param movie Película seleccionada.
     */
    private void onMovieClick(Movie movie) {
        Intent intent = new Intent(getContext(), MovieDetailsActivity.class);
        intent.putExtra("id", movie.getId());
        intent.putExtra("title", movie.getTitle());
        intent.putExtra("imageUrl", movie.getImage());
        intent.putExtra("releaseDate", movie.getReleaseDate());
        intent.putExtra("plot", movie.getPlot());
        startActivity(intent);
    }

    /**
     * Maneja el clic largo en una película.
     * @param movie Película seleccionada.
     */
    private void onMovieLongClick(Movie movie) {
        favoritesManager.removeFavorite(movie, userId);
        movieAdapter.updateMovies(favoritesManager.getFavoriteMovies(userId));

        Toast.makeText(getContext(), "Película eliminada de favoritos: " + movie.getTitle(), Toast.LENGTH_SHORT).show();
    }

    /**
     * Verifica si el dispositivo tiene Bluetooth y si está habilitado antes de compartir.
     */
    private void shareFavoritesViaBluetooth() {
        if (favoriteMovies.isEmpty()) {
            Toast.makeText(getContext(), "No hay películas favoritas para compartir.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (bluetoothAdapter == null) {
            Toast.makeText(getContext(), "Este dispositivo no soporta Bluetooth.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            requestEnableBluetooth();
            return;
        }

        BluetoothSimulator bluetoothSimulator = new BluetoothSimulator(requireActivity());
        bluetoothSimulator.simulateBluetoothConnection(favoriteMovies);
    }

    /**
     * Solicita permisos de Bluetooth
     */
    private void requestBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{
                    android.Manifest.permission.BLUETOOTH,
                    android.Manifest.permission.BLUETOOTH_ADMIN,
                    android.Manifest.permission.BLUETOOTH_CONNECT,
                    android.Manifest.permission.BLUETOOTH_SCAN
            }, 100);
        }
    }

    /**
     * Solicita la activación del Bluetooth.
     */
    private void requestEnableBluetooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        enableBluetoothLauncher.launch(enableBtIntent);
    }
}
