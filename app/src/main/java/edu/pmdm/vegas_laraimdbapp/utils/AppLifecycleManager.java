package edu.pmdm.vegas_laraimdbapp.utils;

import android.content.Context;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.DateFormat;
import java.util.Date;

import edu.pmdm.vegas_laraimdbapp.database.FavoritesManager;
import edu.pmdm.vegas_laraimdbapp.sync.UserSyncManager;

/**
 * Gestiona el ciclo de vida de la aplicación y registra eventos de actividad del usuario.
 */
public class AppLifecycleManager implements LifecycleObserver {

    private final Context context;
    private final FavoritesManager favoritesManager;
    private final UserSyncManager userSyncManager;

    // Constructor: recibe el contexto y obtiene la instancia de FavoritesManager
    public AppLifecycleManager(Context context) {
        this.context = context;
        this.favoritesManager = FavoritesManager.getInstance(context);
        this.userSyncManager = new UserSyncManager();
    }

    // Evento cuando la app se crea
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void onAppCreated() {
        registrarEvento("App Created", false);
    }

    // Evento cuando la app pasa a primer plano (ON_START)
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onAppForegrounded() {
        registrarEvento("App Foregrounded", false);
    }

    // Evento cuando la app pasa a segundo plano (ON_PAUSE)
    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onAppPaused() {
        registrarEvento("App Paused", false);
    }

    // Evento cuando la app pasa a segundo plano (ON_STOP)
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onAppBackgrounded() {
        registrarEvento("App Backgrounded", false);
    }

    // Evento cuando la app se destruye (ON_DESTROY)
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onAppDestroyed() {
        registrarEvento("App Destroyed", false);
    }

    // Evento cuando la app se destruye (ON_DESTROY)
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onAppResume() {
        registrarEvento("App Resume", true);
    }

    /**
     * Registra un evento en la base de datos local y lo sincroniza con Firestore.
     *
     * @param evento Nombre del evento a registrar (Created, Paused, Backgrounded, etc.)
     */
    public void registrarEvento(String evento, boolean isLogin) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            String name = user.getDisplayName() != null ? user.getDisplayName() : "Usuario";
            String email = user.getEmail() != null ? user.getEmail() : "No Email";

            String currentTime = DateFormat.getDateTimeInstance().format(new Date());

            if (isLogin) {
                favoritesManager.registerLogin(userId, currentTime);
            } else {
                favoritesManager.registerLogout(userId, currentTime);
            }

            userSyncManager.addActivityLog(userId, isLogin ? currentTime : null, isLogin ? null : currentTime, name, email, evento);        }
    }

}
