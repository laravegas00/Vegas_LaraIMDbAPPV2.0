// UserSyncManager.java - Corregido

package edu.pmdm.vegas_laraimdbapp.sync;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.pmdm.vegas_laraimdbapp.database.FavoriteDatabase;
import edu.pmdm.vegas_laraimdbapp.models.Movie;
import edu.pmdm.vegas_laraimdbapp.utils.KeystoreManager;

/**
 * Clase para sincronizar los datos del usuario desde Firestore a SQLite y SharedPreferences.
 */
public class UserSyncManager {
    private final FirebaseFirestore db;
    private final Context context;
    private final FavoriteDatabase databaseHelper;
    private final KeystoreManager keystoreManager;
    public static boolean isUserLoggingOut = false; // Evitar múltiples logins/logouts

    /**
     * Constructor
     * @param context Contexto de la aplicación
     */
    public UserSyncManager(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        this.databaseHelper = new FavoriteDatabase(context);
        this.keystoreManager = new KeystoreManager(context);
    }

    /**
     * Registrar un login en Firestore y SQLite
     * @param userId
     */
    public void registerLogin(String userId) {
        if (userId == null) {
            return;
        }

        String loginTime = getCurrentDateTime();
        DocumentReference userRef = db.collection("users").document(userId);

        // Obtener el último login registrado para verificar si tiene logout_time
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists() && documentSnapshot.contains("activity_log")) {
                List<Map<String, Object>> logs = (List<Map<String, Object>>) documentSnapshot.get("activity_log");
                if (logs != null && !logs.isEmpty()) {
                    Map<String, Object> lastLog = logs.get(logs.size() - 1);
                    if (lastLog.get("logout_time") == null) {
                        //  Si la app se cerró sin logout, registrar un logout automático
                        lastLog.put("logout_time", loginTime);
                    }
                }
                //  Registrar el nuevo login
                Map<String, Object> newLogin = new HashMap<>();
                newLogin.put("login_time", loginTime);
                newLogin.put("logout_time", null);

                logs.add(newLogin);

                userRef.update("activity_log", logs)
                        .addOnSuccessListener(aVoid -> Log.d("UserSyncManager", " Login registrado en Firestore: " + loginTime))
                        .addOnFailureListener(e -> Log.e("UserSyncManager", " Error al registrar login en Firestore", e));
            }
        });

        //  Guardar en SharedPreferences
        SharedPreferences.Editor editor = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE).edit();
        editor.putString("last_login", loginTime);
        editor.apply();

        //  Guardar en SQLite
        databaseHelper.registerLogin(userId, loginTime);
    }

    /**
     * Registrar un logout en Firestore y SQLite
     * @param userId Id del usuario
     */
    public void registerLogout(String userId) {
        if (userId == null) {
            Log.e("UserSyncManager", " No se puede registrar logout: userId es null.");
            return;
        }

        String logoutTime = getCurrentDateTime();
        DocumentReference userRef = db.collection("users").document(userId);

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists() && documentSnapshot.contains("activity_log")) {
                List<Map<String, Object>> logs = (List<Map<String, Object>>) documentSnapshot.get("activity_log");
                if (logs != null && !logs.isEmpty()) {
                    Map<String, Object> lastLog = logs.get(logs.size() - 1);
                    if (lastLog.get("logout_time") == null) {
                        //  Actualizar el último login con el logout_time
                        lastLog.put("logout_time", logoutTime);
                        userRef.update("activity_log", logs)
                                .addOnSuccessListener(aVoid -> Log.d("UserSyncManager", " Logout registrado en Firestore: " + logoutTime))
                                .addOnFailureListener(e -> Log.e("UserSyncManager", " Error al registrar logout en Firestore", e));
                    }
                }
            }
        }).addOnFailureListener(e -> Log.e("UserSyncManager", "Error al obtener actividad de Firestore", e));

        // Guardar en SharedPreferences
        SharedPreferences.Editor editor = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE).edit();
        editor.putString("last_logout", logoutTime);
        editor.apply();

        // Guardar en SQLite
        databaseHelper.registerLogout(userId, logoutTime);
    }

    /**
     * Obtener la fecha y hora actual
     * @return Fecha y hora actual
     */
    private String getCurrentDateTime() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
    }

    /**
     * Sincroniza las películas desde Firestore a SQLite
     */
    public void syncMoviesFromFirestore() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e("FavoritesManager", "Usuario no autenticado. No se pueden sincronizar películas.");
            return;
        }

        String userId = user.getUid();
        db.collection("movies")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FavoriteDatabase database = new FavoriteDatabase(context);

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Movie movie = document.toObject(Movie.class);
                            database.addFavorite(movie, userId); // Guardar en SQLite
                        }

                        Log.d("FavoritesManager", "Películas sincronizadas desde Firestore a SQLite.");
                    } else {
                        Log.e("FavoritesManager", "Error al sincronizar películas desde Firestore", task.getException());
                    }
                });
    }

    /**
     * Sincroniza los datos del usuario desde Firestore a SQLite y SharedPreferences.
     */
    public void syncUserData(String userId) {
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String email = documentSnapshot.getString("email");
                String name = documentSnapshot.getString("name");

                String encryptedAddress = documentSnapshot.getString("address");
                String encryptedPhone = documentSnapshot.getString("phone");

                String decryptedAddress = keystoreManager.decrypt(encryptedAddress);
                String decryptedPhone = keystoreManager.decrypt(encryptedPhone);


                String image = documentSnapshot.getString("image");

                // Guardar en SharedPreferences
                SharedPreferences.Editor editor = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE).edit();
                editor.putString("userId", userId);
                editor.putString("email", email);
                editor.putString("name", name);
                editor.putString("address", decryptedAddress);
                editor.putString("phone", decryptedPhone);
                editor.putString("profileImagePath", image);
                editor.apply();

                // Guardar en SQLite
                databaseHelper.addUser(userId, name, email, decryptedAddress, decryptedPhone, image, null);

                Log.d("UserSyncManager", "Datos de usuario sincronizados con SQLite y SharedPreferences.");
            } else {
                Log.e("UserSyncManager", "No se encontraron datos de usuario en Firestore.");
            }
        }).addOnFailureListener(e -> Log.e("UserSyncManager", "Error al obtener datos del usuario en Firestore", e));
    }

    /**
     * Actualiza los datos del usuario en Firestore y SQLite.
     * @param userId Id del usuario
     * @param name Nombre del usuario
     * @param email Email del usuario
     * @param address Dirección
     * @param phone Teléfono
     * @param image Imagen de perfil
     */
    public void addOrUpdateUser(String userId, String name, String email, String address, String phone, String image) {
        // Cifrar dirección y teléfono antes de almacenarlos
        String encryptedAddress = keystoreManager.encrypt(address);
        String encryptedPhone = keystoreManager.encrypt(phone);

        // Guardar en SQLite con datos cifrados
        databaseHelper.updateUser(userId, name, email, null, null, encryptedAddress, encryptedPhone, image);

        // Guardar en Firestore con datos cifrados
        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", userId);
        userData.put("name", name);
        userData.put("email", email);
        userData.put("address", encryptedAddress);
        userData.put("phone", encryptedPhone);
        userData.put("image", image);

        db.collection("users").document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> Log.d("UserSyncManager", "Usuario actualizado en Firestore"))
                .addOnFailureListener(e -> Log.e("UserSyncManager", "Error al actualizar usuario en Firestore", e));
    }

}
