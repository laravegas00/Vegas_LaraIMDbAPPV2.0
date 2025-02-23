package edu.pmdm.vegas_laraimdbapp.sync;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class UserSyncManager {

    private final FirebaseFirestore db;

    public UserSyncManager() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Método para agregar un nuevo usuario a Firestore si no existe.
     */
    public void addActivityLog(String userId, String loginTime, String logoutTime, String name, String email, String eventType) {
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists()) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("user_id", userId);
                userData.put("name", name);
                userData.put("email", email);
                userData.put("activity_log", FieldValue.arrayUnion()); // Crear estructura vacía

                userRef.set(userData)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("UsersSyncManager", "Usuario creado en Firestore.");
                            registrarEvento(userRef, loginTime, logoutTime, eventType);
                        })
                        .addOnFailureListener(e -> Log.e("UsersSyncManager", "Error al crear usuario en Firestore", e));
            } else {
                // Si el usuario ya existe, directamente registramos el evento
                registrarEvento(userRef, loginTime, logoutTime, eventType);
            }
        }).addOnFailureListener(e -> Log.e("UsersSyncManager", "Error al verificar usuario en Firestore", e));
    }


    /**
     * Registra un evento de inicio/cierre de sesión en `activity_log`.
     */
    private void registrarEvento(DocumentReference userRef, String loginTime, String logoutTime, String eventType) {
        if (loginTime != null) {
            // ✅ Si es un login, añadimos un nuevo objeto en el array
            Map<String, Object> loginEvent = new HashMap<>();
            loginEvent.put("login_time", loginTime);
            loginEvent.put("logout_time", ""); // Se deja vacío hasta que ocurra un logout
            loginEvent.put("event_type", eventType);

            userRef.update("activity_log", FieldValue.arrayUnion(loginEvent))
                    .addOnSuccessListener(aVoid -> Log.d("UserSyncManager", "Login registrado en Firestore: " + loginTime))
                    .addOnFailureListener(e -> Log.e("UserSyncManager", "Error al registrar login en Firestore", e));
        }

        if (logoutTime != null) {
            // ✅ Si es un logout, actualizamos el último registro de login en lugar de crear uno nuevo
            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists() && documentSnapshot.contains("activity_log")) {
                    Object logArray = documentSnapshot.get("activity_log");
                    if (logArray instanceof java.util.List) {
                        java.util.List<Map<String, Object>> logs = (java.util.List<Map<String, Object>>) logArray;

                        if (!logs.isEmpty()) {
                            // ✅ Tomamos el último login registrado y actualizamos el logout
                            Map<String, Object> lastLog = logs.get(logs.size() - 1);
                            lastLog.put("logout_time", logoutTime);

                            userRef.update("activity_log", logs)
                                    .addOnSuccessListener(aVoid -> Log.d("UserSyncManager", "Logout registrado en Firestore: " + logoutTime))
                                    .addOnFailureListener(e -> Log.e("UserSyncManager", "Error al registrar logout en Firestore", e));
                        }
                    }
                }
            }).addOnFailureListener(e -> Log.e("UserSyncManager", "Error al obtener actividad de Firestore", e));
        }
    }
}
