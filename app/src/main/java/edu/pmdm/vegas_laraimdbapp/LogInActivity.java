package edu.pmdm.vegas_laraimdbapp;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.UUID;

import edu.pmdm.vegas_laraimdbapp.database.FavoriteDatabase;

/**
 * Actividad de inicio de sesión con Google y Facebook.
 */
public class LogInActivity extends AppCompatActivity {

    // Google Sign-In
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 100;

    // Facebook Sign-In
    private CallbackManager callbackManager;
    private FirebaseAuth firebaseAuth;
    private LoginButton fbLoginButton;

    private FavoriteDatabase databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        // Inicializar FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance();

        // Inicializar la base de datos local
        databaseHelper = new FavoriteDatabase(this);

        // Verificar si hay una cuenta de Google activa
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            // Si hay una cuenta activa, redirigir automáticamente a MainActivity
            String name = account.getDisplayName();
            String email = account.getEmail();
            String photoUrl = (account.getPhotoUrl() != null) ? account.getPhotoUrl().toString() : null;

            redirigirAMainActivity(name, email, photoUrl);
        }

        // Configurar el cliente de autenticación de Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso); // Inicializar el cliente

        // Configurar el botón de inicio de sesión de Google
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        setGoogleSignInButtonText(signInButton, "Sign in with Google");
        signInButton.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });

        //Verificar si hay una sesión activa de Facebook en Firebase
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            String name = firebaseUser.getDisplayName();
            String email = firebaseUser.getEmail();
            String photoUrl = (firebaseUser.getPhotoUrl() != null) ? firebaseUser.getPhotoUrl().toString() : null;

            redirigirAMainActivity(name, email, photoUrl);
            return;
        }

        //Configurar Facebook Sign-In sin cambiar el botón
        callbackManager = CallbackManager.Factory.create();
        fbLoginButton = findViewById(R.id.fb_login_button);
        fbLoginButton.setPermissions(Arrays.asList("email", "public_profile"));
        fbLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "Facebook login exitoso: " + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Toast.makeText(LogInActivity.this, "Inicio de sesión con Facebook cancelado", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Log.e(TAG, "Error en el inicio de sesión con Facebook", error);
                Toast.makeText(LogInActivity.this, "Error en Facebook Sign-In", Toast.LENGTH_SHORT).show();
            }
        });

        // Adaptar UI para evitar solapamientos con barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }



    /**
     * Método para establecer el texto del botón de inicio de sesión de Google.
     * @param signInButton El botón de inicio de sesión de Google.
     * @param buttonText El texto que se mostrará en el botón.
     */
    private void setGoogleSignInButtonText(SignInButton signInButton, String buttonText) {
        for (int i = 0; i < signInButton.getChildCount(); i++) {
            View view = signInButton.getChildAt(i);
            if (view instanceof TextView) {
                ((TextView) view).setText(buttonText);
                return;
            }
        }
    }

    /**
     * Maneja el token de acceso de Facebook y autentica en Firebase.
     */
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            // Extraer datos del usuario de Facebook
                            String userId = user.getUid();
                            String name = user.getDisplayName();
                            String email = user.getEmail();
                            String photoUrl = (user.getPhotoUrl() != null) ? user.getPhotoUrl().toString() : null;

                            // Registrar el login en la base de datos
                            String loginTime = getCurrentDateTime();
                            if (!databaseHelper.userExists(userId)) {
                                databaseHelper.addUser(userId, name, email, loginTime, null);
                            } else {
                                databaseHelper.registerLogin(userId, loginTime);
                            }

                            // Guardar los datos en SharedPreferences
                            guardarCorreo(email);

                            // Redirigir a MainActivity con los datos
                            redirigirAMainActivity(name, email, photoUrl);
                        }
                    } else {
                        Log.w(TAG, "Error en autenticación Firebase con Facebook", task.getException());
                        Toast.makeText(LogInActivity.this, "Error al autenticar con Firebase", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Manejar resultado de Google Sign-In
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }

        // Manejar resultado de Facebook Sign-In
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Manejo del inicio de sesión de Google.
     */
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                String userId = account.getId();
                String name = account.getDisplayName();
                String email = account.getEmail();
                String photoUrl = (account.getPhotoUrl() != null) ? account.getPhotoUrl().toString() : null;

                // Registrar el login en la base de datos
                String loginTime = getCurrentDateTime();
                if (!databaseHelper.userExists(userId)) {
                    databaseHelper.addUser(userId, name, email, loginTime, null);
                } else {
                    databaseHelper.registerLogin(userId, loginTime);
                }

                // Guardar los datos en SharedPreferences
                guardarCorreo(email);

                // Redirigir a MainActivity con los datos
                redirigirAMainActivity(name, email, photoUrl);
            }
        } catch (ApiException e) {
            Log.w(TAG, "Error en Google Sign-In: " + e.getStatusCode());
        }
    }

    private String getCurrentDateTime() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new java.util.Date());
    }

    /**
     * Redirige a MainActivity después de un inicio de sesión exitoso.
     */
    private void redirigirAMainActivity(String name, String email, String photoUrl) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("name", name);
        intent.putExtra("email", email);
        intent.putExtra("photoUrl", photoUrl);
        startActivity(intent);
        finish();
    }


    /**
     * Guarda el correo del usuario en SharedPreferences.
     */
    public void guardarCorreo(String email) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("USER_EMAIL", email);
        editor.putString("USER_ID", generateUserId(email));
        editor.apply();
        Log.d("LogInActivity", "Usuario guardado: " + email);
    }

    private String generateUserId(String email) {
        return UUID.nameUUIDFromBytes(email.getBytes()).toString();
    }
}
