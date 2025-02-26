package edu.pmdm.vegas_laraimdbapp.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import edu.pmdm.vegas_laraimdbapp.sync.UserSyncManager;

/**
 * Clase para manejar el ciclo de vida de la aplicación.
 */
public class AppLifecycleManager implements Application.ActivityLifecycleCallbacks {

    // Variables para rastrear el estado de la app
    private static int activityCount = 0;
    private static boolean isAppInForeground = false;
    private final UserSyncManager userSyncManager;
    private final Context context;

    /**
     * Constructor
     * @param context Contexto de la aplicación
     */
    public AppLifecycleManager(Context context) {
        this.context = context;
        this.userSyncManager = new UserSyncManager(context);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        // Si hay datos temporales almacenados, los sincronizamos al abrir la app
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userSyncManager.syncMoviesFromFirestore();
        }
    }

    @Override
    public void onActivityStarted(Activity activity) {
        activityCount++;
        if (!isAppInForeground) {
            isAppInForeground = true;

            // Registrar login cuando la app se abre
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                userSyncManager.registerLogin(user.getUid());
            }
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
        // Sincronizar datos del usuario al volver a la app
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userSyncManager.syncMoviesFromFirestore();
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        // Guardar en SQLite los cambios antes de que la app quede en segundo plano
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userSyncManager.syncMoviesFromFirestore();
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
        activityCount--;
        if (activityCount == 0) {
            isAppInForeground = false;
            // Registrar logout cuando la app se cierra
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                userSyncManager.registerLogout(user.getUid());
            }
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        // Guardar datos temporales antes de que la actividad sea destruida
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userSyncManager.syncMoviesFromFirestore();
        }
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        // Verificar que los datos críticos se han guardado antes de destruir la actividad
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userSyncManager.syncMoviesFromFirestore();
        }
    }
}
