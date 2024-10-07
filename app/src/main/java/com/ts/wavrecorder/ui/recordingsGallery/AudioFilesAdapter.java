package com.ts.wavrecorder.ui.recordingsGallery;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ts.wavrecorder.R;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class AudioFilesAdapter extends RecyclerView.Adapter<AudioFilesAdapter.AudioFileViewHolder> {
    private List<File> audioFiles;
    private Context context;
    private MediaPlayer mediaPlayer;

    public AudioFilesAdapter(Context context, List<File> audioFiles, MediaPlayer mediaPlayer) {
        this.context = context;
        this.audioFiles = audioFiles;
        this.mediaPlayer = mediaPlayer;
    }

    @NonNull
    @Override
    public AudioFileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_audio_file, parent, false);
        return new AudioFileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AudioFileViewHolder holder, int position) {
        File audioFile = audioFiles.get(position);
        holder.tvFileName.setText(audioFile.getName());

        holder.btnPlay.setOnClickListener(v -> {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.reset();
            }

            try {
                mediaPlayer.setDataSource(audioFile.getAbsolutePath());
                mediaPlayer.prepare();
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mediaPlayer.stop();
                        mediaPlayer.reset(); // Reset the MediaPlayer after playing
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public int getItemCount() {
        return audioFiles.size();
    }

    public static class AudioFileViewHolder extends RecyclerView.ViewHolder {
        TextView tvFileName;
        Button btnPlay;

        public AudioFileViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFileName = itemView.findViewById(R.id.tvFileName);
            btnPlay = itemView.findViewById(R.id.btnPlay);
        }
    }
}