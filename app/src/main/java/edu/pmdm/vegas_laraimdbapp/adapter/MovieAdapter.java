package edu.pmdm.vegas_laraimdbapp.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import edu.pmdm.vegas_laraimdbapp.R;
import edu.pmdm.vegas_laraimdbapp.models.Movie;

/**
 * Adaptador para mostrar la lista de películas organizada.
 */
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    //Declarar las variables
    private List<Movie> movieList;
    private final Context context;
    private OnMovieLongClickListener mlcl;
    private final OnMovieClickListener mcl;

    /**
     * Constructor con clic.
     * @param context Contexto de la aplicación.
     * @param movieList Lista de películas.
     * @param mcl Listener de clic.
     */
    public MovieAdapter(Context context, List<Movie> movieList, OnMovieClickListener mcl) {
        this.context = context;
        this.movieList = movieList;
        this.mcl = mcl;
    }

    /**
     * Constructor sin clic.
     * @param context Contexto de la aplicación.
     * @param movieList Lista de películas.
     */
    public MovieAdapter(Context context, List<Movie> movieList) {
        this.context = context;
        this.movieList = movieList;
        this.mcl = null;  // No se maneja clic en esta versión
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movieList.get(position);

        // Mostrar el título de la película
        holder.imageView.setContentDescription(movie.getTitle());

        // Cargar la imagen de la película con Picasso
        String imageUrl = movie.getImage();

        // Validar si hay una imagen
        if (imageUrl == null || imageUrl.isEmpty()) {
            holder.imageView.setImageResource(R.drawable.error);
            Log.w("MOVIE", "No hay imagen para mostrar");
        } else {
            Picasso.get()
                    .load(imageUrl)
                    .into(holder.imageView);
        }

        // Manejar clics (solo si el listener no es nulo)
        if (mcl != null) {
            holder.itemView.setOnClickListener(v -> mcl.onMovieClick(movie));
        }

        // Manejar clics largos (solo si el listener no es nulo)
        if (mlcl != null) {
            holder.itemView.setOnLongClickListener(v -> {
                mlcl.onMovieLongClick(movie);
                return true;
            });
        }
    }

    /**
     * Interfaz para manejar clics en las películas.
     */
    public interface OnMovieClickListener {
        void onMovieClick(Movie movie);
    }

    /**
     * Interfaz para manejar clics largos en las películas.
     */
    public interface OnMovieLongClickListener {
        void onMovieLongClick(Movie movie);
    }

    public void setOnMovieLongClickListener(OnMovieLongClickListener listener) {
        this.mlcl = listener;
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    /**
     * Actualizar la lista de películas.
     * @param newMovies Nueva lista de películas.
     */
    public void updateMovies(List<Movie> newMovies) {
        movieList.clear();
        movieList.addAll(newMovies);
        notifyDataSetChanged();
    }

    /**
     * ViewHolder para las películas.
     */
    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView; // Declarar el ImageView

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.moviePoster); // Enlazar el ImageView con su ID
        }
    }
}
