package edu.pmdm.vegas_laraimdbapp;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import edu.pmdm.vegas_laraimdbapp.database.FavoriteDatabase;
import edu.pmdm.vegas_laraimdbapp.database.FavoritesManager;
import edu.pmdm.vegas_laraimdbapp.sync.UserSyncManager;
import edu.pmdm.vegas_laraimdbapp.utils.KeystoreManager;

/**
 * Actividad de inicio de sesión con Google, Facebook y Email.
 */
public class LogInActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient googleSignInClient;
    private CallbackManager callbackManager;
    private FavoriteDatabase databaseHelper;
    private UserSyncManager userSyncManager;
    private FavoritesManager favoritesManager;

    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin, buttonRegister;
    private LoginButton fbLoginButton;

    private static final int RC_SIGN_IN = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        // Inicializar Firebase y la BD local
        favoritesManager = FavoritesManager.getInstance(this);
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        databaseHelper = new FavoriteDatabase(this);
        userSyncManager = new UserSyncManager(this);

        initializeUI();
        setupGoogleSignIn();
        setupFacebookSignIn();

        checkIfUserIsAlreadyLoggedIn();

        buttonLogin.setOnClickListener(v -> loginUser());
        buttonRegister.setOnClickListener(v -> registerUser());

    }

    /**
     * Comprueba si el usuario ya está autenticado y redirige a MainActivity
     */
    private void checkIfUserIsAlreadyLoggedIn() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            Log.d("LogInActivity", "Usuario ya autenticado. No se ejecutará nuevo login.");
            redirectToMainActivity(user);
        }
    }

    /**
     * Inicializa los elementos de la UI
     */
    private void initializeUI() {
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonRegister = findViewById(R.id.buttonRegister);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Configura Google Sign-In
     */
    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Usa el valor del archivo google-services.json
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        SignInButton signInButton = findViewById(R.id.sign_in_button);
        setGoogleSignInButtonText(signInButton, "Sign in with Google");
        signInButton.setOnClickListener(v -> signInWithGoogle());
    }

    /**
     * Inicia sesión con Google
     */
    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /**
     * Configura Facebook Sign-In
     */
    private void setupFacebookSignIn() {
        callbackManager = CallbackManager.Factory.create();
        fbLoginButton = findViewById(R.id.fb_login_button);
        fbLoginButton.setPermissions(Arrays.asList("email", "public_profile"));
        fbLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Toast.makeText(LogInActivity.this, "Inicio de sesión con Facebook cancelado", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(LogInActivity.this, "Error en Facebook Sign-In", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Maneja el token de acceso de Facebook y autentica en Firebase.
     */
    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            saveUserData(user);
                            registerLogin(user.getUid());
                            redirectToMainActivity(user);
                        }
                    }
                });
    }

    /**
     * Inicia sesión con Firebase usando email y contraseña
     */
    private void loginUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) return;
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            saveUserData(user);
                            registerLogin(user.getUid());
                            redirectToMainActivity(user);
                        }
                    }
                });
    }
    /**
     * Registra un usuario en Firebase con email y contraseña
     */
    private void registerUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Introduce email y contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            saveUserData(user);
                            registerLogin(user.getUid());
                            redirectToMainActivity(user);
                        }
                    }
                });
    }

    /**
     * Registra el login en la base de datos y guarda en SharedPreferences
     */
    private void registerLogin(String userId) {
        String loginTime = getCurrentDateTime();

        // Guardar en Firestore
        Map<String, Object> logEntry = new HashMap<>();
        logEntry.put("login_time", loginTime);
        logEntry.put("logout_time", null);

        db.collection("users").document(userId)
                .collection("activity_log")
                .add(logEntry);

        // Guardar en SharedPreferences
        SharedPreferences.Editor editor = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE).edit();
        editor.putString("last_login", loginTime);
        editor.apply();

        // Guardar en SQLite
        databaseHelper.registerLogin(userId, loginTime);
    }

    /**
     * Guarda los datos del usuario en Firestore y SQLite
     * @param user Usuario autenticado
     */
    private void saveUserData(FirebaseUser user) {
        String userId = user.getUid();
        String email = user.getEmail();
        String name = user.getDisplayName();
        String address = "Sin dirección"; // Se puede actualizar en el perfil
        String phone = "Sin teléfono"; // Se puede actualizar en el perfil

        KeystoreManager keystoreManager = new KeystoreManager(this);
        String encryptedAddress = keystoreManager.encrypt(address);
        String encryptedPhone = keystoreManager.encrypt(phone);

        // Guardar en Firestore
        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", userId);
        userData.put("email", email);
        userData.put("name", name);
        userData.put("address", encryptedAddress);
        userData.put("phone", encryptedPhone);

        db.collection("users").document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> Log.d("LogInActivity", "Usuario guardado en Firestore"))
                .addOnFailureListener(e -> Log.e("LogInActivity", "Error al guardar usuario en Firestore", e));

        // Guardar en SharedPreferences
        SharedPreferences.Editor editor = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE).edit();
        editor.putString("userId", userId);
        editor.putString("email", email);
        editor.putString("name", name);
        editor.putString("address", address);
        editor.putString("phone", phone);
        editor.apply();

        // Guardar en SQLite
        databaseHelper.addUser(userId, name, email, address, phone, null, null);
    }


    /**
     * Obtiene la fecha y hora actual
     */
    private String getCurrentDateTime() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
    }

    /**
     * Redirige a MainActivity tras un inicio de sesión exitoso.
     */
    private void redirectToMainActivity(FirebaseUser user) {
        if (user == null) {
            Log.e("LogInActivity", "Usuario es null, no se puede redirigir.");
            return;
        }

        Log.d("LogInActivity", "Redirigiendo a MainActivity con usuario: " + user.getUid());

        userSyncManager.registerLogin(user.getUid());

        //Inicializar FavoritesManager si es null
        if (favoritesManager == null) {
            favoritesManager = FavoritesManager.getInstance(this);
        }

        // Asegurar que favoritesManager no sea null antes de llamar a listenForMovieUpdates()
        if (favoritesManager != null) {
            favoritesManager.listenForMovieUpdates();
        } else {
            Log.e("LogInActivity", "FavoritesManager es null, no se puede escuchar actualizaciones.");
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("name", user.getDisplayName());
        intent.putExtra("email", user.getEmail());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


    /**
     * Personaliza el texto del botón de Google Sign-In.
     */
    private void setGoogleSignInButtonText(SignInButton signInButton, String text) {
        for (int i = 0; i < signInButton.getChildCount(); i++) {
            View view = signInButton.getChildAt(i);
            if (view instanceof TextView) {
                ((TextView) view).setText(text);
                return;
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleGoogleSignInResult(task);
        }
        callbackManager.onActivityResult(requestCode, resultCode, data); // Facebook login
    }

    /**
     * Maneja el resultado del inicio de sesión con Google
     */
    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                firebaseAuth.signInWithCredential(credential)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = firebaseAuth.getCurrentUser();
                                if (user != null) {
                                    saveUserData(user);
                                    registerLogin(user.getUid());
                                    redirectToMainActivity(user);
                                }
                            } else {
                                Toast.makeText(this, "Autenticación fallida", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        } catch (ApiException e) {
            Toast.makeText(this, "Error en inicio de sesión", Toast.LENGTH_SHORT).show();
        }
    }


}
