package com.ts.wavrecorder.ui.recordingsGallery;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ts.wavrecorder.R;
import com.ts.wavrecorder.databinding.FragmentGalleryBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GalleryFragment extends Fragment {
    private RecyclerView recyclerView;
    private AudioFilesAdapter adapter;
    private FragmentGalleryBinding binding;
    private MediaPlayer mediaPlayer;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        recyclerView = view.findViewById(R.id.recyclerViewAudioFiles);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Get the list of .wav files from internal storage
        List<File> audioFiles = getAudioFiles();
        mediaPlayer = new MediaPlayer();
        // Set the adapter with the list of files
        adapter = new AudioFilesAdapter(getContext(), audioFiles, mediaPlayer);
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        binding = null;
    }

    // Function to get all .wav files from the internal storage
    private List<File> getAudioFiles() {
        List<File> audioFiles = new ArrayList<>();
        File directory = getActivity().getFilesDir();

        // Filter only .wav files
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".wav"));
        if (files != null) {
            for (File file : files) {
                audioFiles.add(file);
            }
        }
        return audioFiles;
    }

}