package edu.pmdm.vegas_laraimdbapp;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hbb20.CountryCodePicker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import edu.pmdm.vegas_laraimdbapp.database.FavoritesManager;
import edu.pmdm.vegas_laraimdbapp.utils.KeystoreManager;

/**
 * Actividad para editar los datos del usuario.
 */
public class EditUserActivity extends AppCompatActivity {

    // Constantes para el manejo de imágenes
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAPTURE_IMAGE_REQUEST = 2;
    private static final int SELECT_LOCATION_REQUEST = 100;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    private EditText editTextName, editTextPhone, editTextEmail;
    private ImageView imageViewProfile;
    private CountryCodePicker countryCodePicker;
    private String currentPhotoPath;
    private TextView textViewAddress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        editTextName = findViewById(R.id.editTextName);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextEmail = findViewById(R.id.editTextEmail);
        imageViewProfile = findViewById(R.id.imageViewProfile);
        countryCodePicker = findViewById(R.id.countryCodePicker);
        textViewAddress= findViewById(R.id.textViewAddress);

        Button buttonSelectImage = findViewById(R.id.buttonSelectImage);
        Button buttonSave = findViewById(R.id.buttonSave);


        countryCodePicker.registerCarrierNumberEditText(editTextPhone);


        buttonSelectImage.setOnClickListener(v -> showImagePickerDialog());

        Button buttonSelectAddress = findViewById(R.id.buttonSelectLocation);
        buttonSelectAddress.setOnClickListener(v -> {
            Intent intent = new Intent(EditUserActivity.this, SelectAddressActivity.class);
            startActivityForResult(intent, 100); // Cambiado a 100 para que coincida
        });


        buttonSave.setOnClickListener(v -> saveUserData());


        loadUserData();
        checkAndRequestCameraPermission();

    }

    /**
     * Solicitar permiso de cámara si no se ha otorgado.
     */
    private void checkAndRequestCameraPermission() {
        List<String> permissionsToRequest = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.CAMERA);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[0]), CAMERA_PERMISSION_REQUEST_CODE);
        }
    }


    /**
     * Mostrar diálogo para seleccionar imagen.
     */
    private void showImagePickerDialog() {
        String[] options = {"Tomar foto", "Elegir de la galería"};
        new AlertDialog.Builder(this)
                .setTitle("Seleccionar Imagen")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openCamera();
                    } else {
                        openGallery();
                    }
                })
                .show();
    }


    /**
     * Abrir la cámara para capturar una imagen.
     */
    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            return;
        }

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                File photoFile = createImageFile();
                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, CAPTURE_IMAGE_REQUEST);
                }
            } catch (IOException ex) {
                Toast.makeText(this, "Error al crear archivo de imagen", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Si no hay cámara, mostrar un selector de aplicaciones
            Intent pickAppIntent = new Intent(Intent.ACTION_MAIN);
            pickAppIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            Intent chooserIntent = Intent.createChooser(pickAppIntent, "Selecciona una aplicación de cámara");
            startActivity(chooserIntent);
        }
    }


    /**
     * Crear un archivo temporal para guardar la imagen capturada.
     * @return Archivo temporal
     * @throws IOException Si hay un error al crear el archivo
     */
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(null);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    /**
     * Abrir la galería para seleccionar una imagen.
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        // Verificar si hay una aplicación que maneje la acción
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        } else {
            // Si no hay una galería, usar el selector de archivos
            Intent fileIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            fileIntent.addCategory(Intent.CATEGORY_OPENABLE);
            fileIntent.setType("image/*");
            startActivityForResult(fileIntent, PICK_IMAGE_REQUEST);
        }
    }

    /**
     * Manejar la selección de una imagen.
     * @param imageUri Uri de la imagen seleccionada
     */
    private void handleImageSelection(Uri imageUri) {
        if (imageUri != null) {
            String localPath = saveGalleryImageToLocalFile(imageUri);
            if (localPath != null) {
                imageViewProfile.setImageURI(Uri.fromFile(new File(localPath))); // Mostrar la imagen
                saveImageToPreferences(localPath); // Guardar la ruta del archivo local
            } else {
                Toast.makeText(this, "Error al guardar la imagen seleccionada", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Guardar la imagen seleccionada en el almacenamiento interno.
     * @param imageUri Uri de la imagen seleccionada
     * @return Ruta del archivo local
     */
    private String saveGalleryImageToLocalFile(Uri imageUri) {
        try {
            // Crear un archivo temporal para guardar la imagen
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = getExternalFilesDir(null);
            File imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);

            // Abrir un flujo de entrada desde la galería
            InputStream inputStream = getContentResolver().openInputStream(imageUri);

            // Abrir un flujo de salida para el archivo local
            OutputStream outputStream = new FileOutputStream(imageFile);

            // Copiar el contenido del flujo de entrada al flujo de salida
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            inputStream.close();
            outputStream.close();

            // Retornar la ruta del archivo guardado
            return imageFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al guardar la imagen seleccionada", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    /**
     * Guardar la ruta de la imagen seleccionada en SharedPreferences.
     * @param imagePath Ruta de la imagen
     */
    private void saveImageToPreferences(String imagePath) {
        if (imagePath != null && !imagePath.isEmpty()) {
            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("profileImagePath", imagePath);
            editor.apply();
        } else {
            Toast.makeText(this, "Error: No se pudo guardar la imagen.", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Manejar la selección de una imagen
        if (resultCode == RESULT_OK) {
            Uri imageUri = null;

            // Imagen seleccionada de la galería
            if (requestCode == PICK_IMAGE_REQUEST && data != null) {
                imageUri = data.getData();
            } else if (requestCode == CAPTURE_IMAGE_REQUEST) { // Imagen capturada por la cámara
                File file = new File(currentPhotoPath);
                if (file.exists()) {
                    imageUri = Uri.fromFile(file);
                    Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
                    if (bitmap != null) {
                        imageViewProfile.setImageBitmap(bitmap);
                        saveImageToPreferences(currentPhotoPath);
                    }
                }
            }

            if (imageUri != null) {
                handleImageSelection(imageUri);
            }


            // Manejo de la dirección seleccionada desde SelectLocationActivity
            if (requestCode == SELECT_LOCATION_REQUEST && data != null) {
                String selectedAddress = data.getStringExtra("selectedAddress");
                if (selectedAddress != null) {
                    TextView textViewAddress = findViewById(R.id.textViewAddress);
                    textViewAddress.setText(selectedAddress);

                    // Guardar dirección en SharedPreferences
                    SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("userAddress", selectedAddress);
                    editor.apply();

                    Log.d("direccion", "Dirección guardada en SharedPreferences: " + selectedAddress);
                }
            }
        }
    }

    /**
     * Cargar datos del usuario desde Firestore.
     */
    private void loadUserData() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", null);

        if (userId == null) {
            Toast.makeText(this, "Error: No se encontró información del usuario.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Cargar datos desde Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Obtener datos desde Firestore
                        String name = documentSnapshot.getString("name");
                        String email = documentSnapshot.getString("email");

                        KeystoreManager keystoreManager = new KeystoreManager(this);

                        String encryptedPhone = documentSnapshot.getString("phone");
                        String encryptedAddress = documentSnapshot.getString("address");
                        String decryptedPhone = keystoreManager.decrypt(encryptedPhone);
                        String decryptedAddress = keystoreManager.decrypt(encryptedAddress);


                        String imagePath = documentSnapshot.getString("image");

                        // Actualizar los campos en la interfaz de usuario
                        editTextName.setText(name != null ? name : "");
                        editTextEmail.setText(email != null ? email : "");
                        editTextEmail.setEnabled(false); // No permitir editar el correo
                        editTextPhone.setText(decryptedPhone != null ? decryptedPhone : "");
                        textViewAddress.setText(decryptedAddress != null ? decryptedAddress : "");

                        // Manejar la imagen del perfil
                        if (imagePath != null && !imagePath.isEmpty()) {
                            Glide.with(this).load(imagePath).circleCrop().into(imageViewProfile);
                        } else {
                            Glide.with(this).load("android.resource://" + getPackageName() + "/drawable/logoandroid").circleCrop().into(imageViewProfile);
                        }
                    } else {
                        Toast.makeText(this, "No se encontraron datos del usuario en la nube.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar datos del usuario.", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Guardar datos del usuario en SQLite.
     */
    private void saveUserData() {
        String name = editTextName.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String address = textViewAddress.getText().toString().trim();

        if (!validateName(name)) return;
        if (!validatePhone()) return;

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", null);
        String email = sharedPreferences.getString("email", "Correo no disponible");

        // Verificar si ya hay una imagen guardada
        String imagePath = sharedPreferences.getString("profileImagePath", null);

        if (imagePath == null || imagePath.isEmpty()) {
            FavoritesManager favoritesManager = FavoritesManager.getInstance(this);
            Map<String, String> userData = favoritesManager.getUserDetails(userId);

            // Intenta obtener la imagen desde SQLite
            imagePath = userData != null ? userData.get("image") : null;

            // Si aún es nulo, usa la imagen predeterminada
            if (imagePath == null || imagePath.isEmpty()) {
                imagePath = "android.resource://" + getPackageName() + "/drawable/logoandroid";
            }
        }

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Error al guardar: Usuario no válido.", Toast.LENGTH_SHORT).show();
            return;
        }

        KeystoreManager keystoreManager = new KeystoreManager(this);
        String encryptedPhone = keystoreManager.encrypt(phone);
        String encryptedAddress = keystoreManager.encrypt(address);


        // Guardar en SQLite
        FavoritesManager favoritesManager = FavoritesManager.getInstance(this);
        favoritesManager.addOrUpdateUser(userId, name, email, null, null, encryptedAddress, encryptedPhone, imagePath);

        // Guardar en SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name", name);
        editor.putString("phone", encryptedPhone);
        editor.putString("userAddress", encryptedAddress);
        editor.putString("profileImagePath", imagePath); // Guardar correctamente la imagen actual
        editor.apply();

        // Guardar en Firestore
        saveUserToFirestore(userId, name, email, encryptedPhone, encryptedAddress, imagePath);

        Toast.makeText(this, "Datos guardados correctamente", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(EditUserActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    /**
     * Guardar datos del usuario en Firestore.
     * @param userId Id del usuario
     * @param name Nombre del usuario
     * @param email Email del usuario
     * @param encryptedPhone Número de teléfono encriptado
     * @param encryptedAddress Dirección encriptada
     * @param imagePath Ruta de la imagen
     */
    public void saveUserToFirestore(String userId, String name, String email, String encryptedPhone, String encryptedAddress, String imagePath) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", userId);
        userData.put("name", name != null ? name : "Usuario");
        userData.put("email", email);
        userData.put("phone", encryptedPhone);
        userData.put("address", encryptedAddress);
        userData.put("image", imagePath != null ? imagePath : "android.resource://" + getPackageName() + "/drawable/logoandroid");

        // Comprobamos si el usuario ya existe
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Si el usuario ya existe, actualizamos los datos
                        db.collection("users").document(userId)
                                .update(userData)
                                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Usuario actualizado en Firestore"))
                                .addOnFailureListener(e -> Log.e("Firestore", "Error al actualizar usuario en Firestore", e));
                    } else {
                        // Si el usuario no existe, lo creamos
                        db.collection("users").document(userId)
                                .set(userData)
                                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Usuario creado en Firestore"))
                                .addOnFailureListener(e -> Log.e("Firestore", "Error al crear usuario en Firestore", e));
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error al comprobar existencia del usuario", e));
    }


    /**
     * Validar el nombre del usuario.
     * @param name Nombre del usuario
     * @return Si el nombre es válido
     */
    private boolean validateName(String name) {
        if (name.isEmpty() || name.length() > 20 || !name.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$")) {
            Toast.makeText(this, "El nombre debe ser válido y contener solo letras", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * Validar el número de teléfono.
     * @return Si el número de teléfono es válido
     */
    private boolean validatePhone() {
        if (!countryCodePicker.isValidFullNumber()) {
            Toast.makeText(this, "Número de teléfono no válido según el país seleccionado", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

}
