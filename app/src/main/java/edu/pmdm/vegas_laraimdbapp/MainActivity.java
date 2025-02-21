package edu.pmdm.vegas_laraimdbapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import edu.pmdm.vegas_laraimdbapp.databinding.ActivityMainBinding;

/**
 * Actividad principal de la aplicación.
 */
public class MainActivity extends AppCompatActivity {

    // Declaración de variables
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar); // Configurar la Toolbar

        // Configurar el Navigation Drawer
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        // Configurar la navegación
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Obtener datos del intent
        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String email = intent.getStringExtra("email");
        String photoUrl = intent.getStringExtra("photoUrl");

        // Actualizar UI del encabezado del Navigation Drawer
        View headerView = navigationView.getHeaderView(0);
        TextView nameTextView = headerView.findViewById(R.id.nav_header_name);
        TextView emailTextView = headerView.findViewById(R.id.nav_header_email);
        ImageView photoImageView = headerView.findViewById(R.id.nav_header_image);

        // Mostrar datos del usuario en el encabezado
        nameTextView.setText(name);
        emailTextView.setText(email);

        Glide.with(this)
                .load(photoUrl != null ? photoUrl : "https://lh3.googleusercontent.com/a/default-user")
                .into(photoImageView);

        // Configurar el botón de logout
        Button logoutButton = headerView.findViewById(R.id.nav_header_logout_button);
        logoutButton.setOnClickListener(v -> cerrarSesion()); // Llamada al método de cierre de sesión

    }

    /**
     * Método para cerrar la sesión del usuario.
     */
    private void cerrarSesion() {
        // Cerrar sesión de Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInClient.signOut().addOnCompleteListener(this, task -> {
            // Cerrar sesión de Firebase (si lo usas)
            FirebaseAuth.getInstance().signOut();

            // Cerrar sesión de Facebook (🔹 Esta es la única línea agregada)
            LoginManager.getInstance().logOut();

            // Eliminar datos de SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear(); // Elimina todos los datos guardados
            editor.apply();

            // Mostrar mensaje de cierre de sesión
            Toast.makeText(this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show();

            // Redirigir al usuario a la pantalla de inicio de sesión
            Intent intent = new Intent(this, LogInActivity.class);
            startActivity(intent);
            finish(); // Finalizar MainActivity
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}