package edu.pmdm.vegas_laraimdbapp;

import static android.content.ContentValues.TAG;

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

import java.util.Arrays;
import java.util.UUID;

import edu.pmdm.vegas_laraimdbapp.database.FavoriteDatabase;

/**
 * Actividad de inicio de sesión con Google, Facebook y Email.
 */
public class LogInActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;
    private CallbackManager callbackManager;
    private FavoriteDatabase databaseHelper;

    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin, buttonRegister;
    private LoginButton fbLoginButton;

    private static final int RC_SIGN_IN = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        // Inicializar Firebase y la BD local
        firebaseAuth = FirebaseAuth.getInstance();
        databaseHelper = new FavoriteDatabase(this);

        initializeUI();
        setupGoogleSignIn();
        setupFacebookSignIn();

        // Verificar si el usuario ya está autenticado
        if (firebaseAuth.getCurrentUser() != null) {
            registerLogin(firebaseAuth.getCurrentUser());
        }

        buttonLogin.setOnClickListener(v -> loginUser());
        buttonRegister.setOnClickListener(v -> registerUser());
    }

    /**
     * Inicializa los elementos de la UI
     */
    private void initializeUI() {
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonRegister = findViewById(R.id.buttonRegister);

        SignInButton googleSignInButton = findViewById(R.id.sign_in_button);
        googleSignInButton.setSize(SignInButton.SIZE_STANDARD);
        setGoogleSignInButtonText(googleSignInButton, "Sign in with Google");

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
        googleSignInClient = GoogleSignIn.getClient(this,
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestProfile()
                        .build()
        );

        findViewById(R.id.sign_in_button).setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
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
                        redirectToMainActivity(firebaseAuth.getCurrentUser());
                    } else {
                        Toast.makeText(this, "Error en autenticación con Facebook", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Inicia sesión con Firebase usando email y contraseña
     */
    private void loginUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Introduce email y contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        registerLogin(firebaseAuth.getCurrentUser());
                    } else {
                        Toast.makeText(this, "Error al iniciar sesión: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
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
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        registerLogin(firebaseAuth.getCurrentUser());
                    } else {
                        Toast.makeText(this, "Error al registrarse: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Registra el login en la base de datos y guarda en SharedPreferences
     */
    private void registerLogin(FirebaseUser user) {
        if (user != null) {
            String userId = user.getUid();
            String name = user.getDisplayName();
            String email = user.getEmail();
            String loginTime = getCurrentDateTime();

            if (!databaseHelper.userExists(userId)) {
                databaseHelper.addUser(userId, name, email, loginTime, null);
            } else {
                databaseHelper.registerLogin(userId, loginTime);
            }

            saveUserData(email);
            redirectToMainActivity(user);
        }
    }

    /**
     * Guarda el correo del usuario en SharedPreferences.
     */
    private void saveUserData(String email) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("USER_EMAIL", email);
        editor.putString("USER_ID", generateUserId(email));
        editor.apply();
    }

    private String generateUserId(String email) {
        return UUID.nameUUIDFromBytes(email.getBytes()).toString();
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
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("name", user.getDisplayName());
        intent.putExtra("email", user.getEmail());
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
}
