package edu.pmdm.vegas_laraimdbapp;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import java.util.UUID;

/**
 * Actividad de inicio de sesión de Google.
 */
public class LogInActivity extends AppCompatActivity {

    // Declaración de variables
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);


        // Verificar si hay una cuenta de Google activa
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            // Si hay una cuenta activa, redirigir automáticamente a MainActivity
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("name", account.getDisplayName());
            intent.putExtra("email", account.getEmail());
            intent.putExtra("userId", account.getId()); // Pasar el userId a MainActivity
            startActivity(intent);
            finish();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Manejar el resultado del inicio de sesión de Google
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    /**
     * Método para manejar el resultado del inicio de sesión de Google.
     */
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            // Obtener la cuenta de Google si el inicio de sesión fue exitoso
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Mostrar información del usuario en la consola
            if (account != null) {
                // Mostrar información del usuario en la consola
                String name = account.getDisplayName();
                String email = account.getEmail();
                String photoUrl = (account.getPhotoUrl() != null) ? account.getPhotoUrl().toString() : null;

                guardarCorreo(email); // Guardar el correo en SharedPreferences

                //Mandar la información a la MainActivity
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("name", name);
                intent.putExtra("email", email);
                intent.putExtra("photoUrl", photoUrl);
                startActivity(intent);
                finish();

                Log.w(TAG, "USUARIO LOGEADO");
            }
        } catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        }
    }

    /**
     * Método para guardar el correo en SharedPreferences.
     * @param email El correo a guardar.
     */
    public void guardarCorreo(String email) {

        // Obtener las preferencias compartidas
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit(); // Editar las preferencias

        // Generar un userId único basado en el email
        String userId = generateUserId(email);

        // Guardar los datos actualizados en SharedPreferences
        editor.putString("USER_EMAIL", email);
        editor.putString("USER_ID", userId);
        editor.apply();

        Log.d("LogInActivity", "Usuario guardado en SharedPreferences: " + email + " | ID: " + userId);
    }

    // Método para generar un ID único basado en el email
    private String generateUserId(String email) {
        return UUID.nameUUIDFromBytes(email.getBytes()).toString();
    }

}
