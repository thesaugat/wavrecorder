package com.ts.wavrecorder.ui.home;

import android.Manifest;

import android.content.pm.PackageManager;

import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ts.wavrecorder.R;
import com.ts.wavrecorder.databinding.FragmentHomeBinding;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {


    private ExecutorService executorService;
    private Button playButton;
    private FloatingActionButton startRecordButton;
    private TextView recordingTimeText;
    private boolean isRecording = false;
    private String wavFilePath;
    private String lastRecord;
    private MediaPlayer mediaPlayer;
    //    private MediaRecorder mediaRecorder;
    // Timer variables
    private long startTime = 0L;
    private Handler timerHandler = new Handler();
    private long timeInMilliseconds = 0L;
    private long updatedTime = 0L;
    private long timeSwapBuff = 0L;

    private WavAudioRecorder mRecorder;


    private ActivityResultLauncher<String> requestPermissionLauncher;

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        initalizeViewElements(root);
        initPermission();
        executorService = Executors.newSingleThreadExecutor();

        return root;
    }

    private void initPermission() {
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                checkPermissionAndRun();
            } else {
                Toast.makeText(getActivity(), "Permission denied", Toast.LENGTH_SHORT).show();

                // Permission denied, handle accordingly
            }
        });
//        checkPermissionAndRun();
    }

    private void checkPermissionAndRun() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            // Permission is granted, safe to call the function
            startRecording();
        } else {
            // Request the permission
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
        }
    }


    private void initalizeViewElements(View root) {
        startRecordButton = root.findViewById(R.id.start_record_button);

        recordingTimeText = root.findViewById(R.id.recording_time);
        playButton = root.findViewById(R.id.play_button);

        startRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playButton.setEnabled(false);
                checkPermissionAndRecord();
            }
        });


        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAudio();
            }
        });
    }

    private void checkPermissionAndRecord() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            startRecording();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
        }

    }

    // Play the recorded audio using MediaPlayer
    private void playAudio() {
        if (mediaPlayer == null && wavFilePath != null) {
            mediaPlayer = new MediaPlayer();
        }

        try {
            playButton.setEnabled(false);
            mediaPlayer.setDataSource(wavFilePath);
            mediaPlayer.prepare();
            mediaPlayer.start();

            // When playback completes, reset and enable the play button
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    playButton.setEnabled(true);
                    mediaPlayer.reset(); // Reset the MediaPlayer after playing
                }
            });

        } catch (IOException e) {
            Log.e("MediaPlayer", "Error preparing MediaPlayer: " + e.getMessage(), e);
            playButton.setEnabled(true);
        }
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    private void startRecording() {
        if (isRecording) {
            stopRecording();
            return;
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "recorded_audio_" + timeStamp + ".wav";
        lastRecord = fileName;
        wavFilePath = getActivity().getFilesDir().getAbsolutePath() + "/" + fileName;
        mRecorder = WavAudioRecorder.getInstanse();
        mRecorder.setOutputFile(wavFilePath);


        isRecording = true;
        startTime = SystemClock.uptimeMillis();
        timerHandler.postDelayed(updateTimerThread, 0);


        if (WavAudioRecorder.State.INITIALIZING == mRecorder.getState()) {
            mRecorder.prepare();
            mRecorder.start();
            enableStart(false);
        } else if (WavAudioRecorder.State.ERROR == mRecorder.getState()) {
            mRecorder.release();
            mRecorder = WavAudioRecorder.getInstanse();
            mRecorder.setOutputFile(wavFilePath);
            stopRecording();
            enableStart(true);
        } else {
            mRecorder.stop();
            mRecorder.reset();
            enableStart(true);
            stopRecording();
        }
    }

    private void enableStart(boolean b) {
        if (b) {

            startRecordButton.setImageDrawable(getContext().getDrawable(R.drawable.record_982_svgrepo_com));
        } else {
            startRecordButton.setImageDrawable(getContext().getDrawable(R.drawable.baseline_stop_24));
        }
    }


    private void stopRecording() {
        if (mRecorder != null && isRecording) {
            isRecording = false;

            timeSwapBuff += timeInMilliseconds;
            timerHandler.removeCallbacks(updateTimerThread);

            enableStart(true);
            recordingTimeText.setText(lastRecord + "   " + getFileSizeInKB(wavFilePath));
            playButton.setEnabled(true);


        }
    }

    public long getFileSizeInKB(String filePath) {
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            return file.length() / 1024; // Convert bytes to KB
        }
        return -1; // Return -1 if file doesn't exist or is not a regular file
    }

    // Timer thread to update the recording time
    private Runnable updateTimerThread = new Runnable() {
        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            updatedTime = timeSwapBuff + timeInMilliseconds;

            int secs = (int) (updatedTime / 1000);
            int mins = secs / 60;
            secs = secs % 60;
            recordingTimeText.setText(String.format("%02d:%02d", mins, secs));

            timerHandler.postDelayed(this, 1000);
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (executorService != null) {
            executorService.shutdown(); // Shut down the executor service
        }
    }
}