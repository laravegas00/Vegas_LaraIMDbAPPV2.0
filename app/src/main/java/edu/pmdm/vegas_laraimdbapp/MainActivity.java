package edu.pmdm.vegas_laraimdbapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Map;

import edu.pmdm.vegas_laraimdbapp.database.FavoriteDatabase;
import edu.pmdm.vegas_laraimdbapp.database.FavoritesManager;
import edu.pmdm.vegas_laraimdbapp.databinding.ActivityMainBinding;
import edu.pmdm.vegas_laraimdbapp.sync.UserSyncManager;
import edu.pmdm.vegas_laraimdbapp.utils.AppLifecycleManager;

/**
 * Actividad principal de la aplicación.
 */
public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private FavoriteDatabase databaseHelper;
    private FirebaseFirestore db;
    private NavigationView navigationView;
    private String userId;
    private boolean isUserLoggingOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);

        UserSyncManager userSyncManager = new UserSyncManager(this);

        databaseHelper = new FavoriteDatabase(this);
        FavoritesManager favoritesManager = FavoritesManager.getInstance(this);
        db = FirebaseFirestore.getInstance();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            registerActivityLifecycleCallbacks(new AppLifecycleManager(this));
        }

        // Obtener el usuario actual de Firebase
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            // Sincronizar datos del usuario y favoritos
            userSyncManager.syncUserData(userId);
            favoritesManager.syncFavoritesFromFirestore();
        }


        // Configuración del Navigation Drawer
        DrawerLayout drawer = binding.drawerLayout;
        navigationView = binding.navView;

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Sincronizar favoritos de Firestore a SQLite al iniciar sesión
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            favoritesManager.syncFavoritesFromFirestore();
        }, 3000);

        // Configurar el botón de logout
        View headerView = navigationView.getHeaderView(0);
        Button logoutButton = headerView.findViewById(R.id.nav_header_logout_button);
        logoutButton.setOnClickListener(v -> cerrarSesion());

        updateNavigationHeader(); // Carga datos de usuario desde SharedPreferences
    }

    @Override
    protected void onResume() {
        super.onResume();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            updateNavigationHeader();
        }
    }


    /**
     * Método para actualizar los datos del usuario en el Navigation Drawer
     */
    private void updateNavigationHeader() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView navUsername = headerView.findViewById(R.id.nav_header_name);
        TextView navEmail = headerView.findViewById(R.id.nav_header_email);
        ImageView navProfileImage = headerView.findViewById(R.id.nav_header_image);

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", null);

        if (userId == null) {
            return;
        }

        // Obtener datos de Firestore y actualizar SQLite y SharedPreferences
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> userData = documentSnapshot.getData();
                        if (userData != null) {
                            String name = documentSnapshot.getString("name");
                            String email = documentSnapshot.getString("email");
                            String imagePath = documentSnapshot.getString("profileImage");
                            String lastLogin = documentSnapshot.getString("lastLogin");
                            String lastLogout = documentSnapshot.getString("lastLogout");
                            String address = documentSnapshot.getString("address");
                            String phone = documentSnapshot.getString("phone");


                            navUsername.setText(name);
                            navEmail.setText(email);
                            Glide.with(this).load(imagePath).circleCrop().into(navProfileImage);

                            // Guardar en SharedPreferences
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("name", name);
                            editor.putString("email", email);
                            editor.putString("profileImagePath", imagePath);
                            editor.putString("lastLogin", lastLogin);
                            editor.putString("lastLogout", lastLogout);
                            editor.putString("address", address);
                            editor.putString("phone", phone);
                            editor.apply();

                            // Guardar en SQLite
                            databaseHelper.updateUser(userId, name, email, imagePath, lastLogin, lastLogout, address, phone);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("MainActivity", "Error al obtener datos de Firestore", e));

        Log.d("MainActivity", "Datos de usuario actualizados en Navigation Drawer.");
    }

    /**
     * Cierra la sesión del usuario y redirige a LogInActivity
     */
    private void cerrarSesion() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            String logoutTime = getCurrentDateTime();

            // Crear instancia de UserSyncManager
            UserSyncManager userSyncManager = new UserSyncManager(this);

            // Registrar logout en Firestore, SQLite y SharedPreferences
            userSyncManager.registerLogout(user.getUid());
        }

        // Cerrar sesión en Firebase y Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
        googleSignInClient.signOut();

        FirebaseAuth.getInstance().signOut();
        LoginManager.getInstance().logOut();

        // Limpiar SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();

        Toast.makeText(this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show();

        // Redirigir a LogInActivity
        startActivity(new Intent(this, LogInActivity.class));
        finish();
    }

    /**
     * Carga la imagen de perfil del usuario
     * @param imageUrl URL de la imagen de perfil
     */
    private void loadUserProfileImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            //Establecer una imagen por defecto si la URL es nula
            imageUrl = "android.resource://" + getPackageName() + "/drawable/default_user_icon";
        }

        ImageView imageView = findViewById(R.id.nav_header_image);

        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_android) // Imagen de carga
                .error(R.drawable.error) // Imagen de error
                .into(imageView);
    }

    /**
     * Obtiene la fecha y hora actual
     * @return Fecha y hora actual en formato String
     */
    private String getCurrentDateTime() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new java.util.Date());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String logoutTime = getCurrentDateTime();
            databaseHelper.registerLogout(userId, logoutTime);
        }
    }

    /**
     * Maneja la selección de un elemento del menú
     * @param item Elemento del menú seleccionado
     * @return True si se ha manejado la selección, false en caso contrario
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_edit_user) { // Detecta la opción "Editar Usuario"
            // Abre la actividad EditUserActivity
            Intent intent = new Intent(MainActivity.this, EditUserActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
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
