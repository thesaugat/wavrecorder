//package com.niru.fakeguard.ui.home;
//
//import android.Manifest;
//import android.annotation.SuppressLint;
//import android.content.pm.PackageManager;
//import android.media.AudioFormat;
//import android.media.AudioRecord;
//import android.media.MediaPlayer;
//import android.media.MediaRecorder;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.SystemClock;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.activity.result.ActivityResultLauncher;
//import androidx.activity.result.contract.ActivityResultContracts;
//import androidx.annotation.NonNull;
//import androidx.annotation.RequiresPermission;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//import androidx.fragment.app.Fragment;
//import androidx.lifecycle.ViewModelProvider;
//
//import com.niru.fakeguard.R;
//import com.niru.fakeguard.databinding.FragmentHomeBinding;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//public class HomeFragment extends Fragment {
//
//    private static final int REQUEST_AUDIO_PERMISSION_CODE = 1;
//    private static final int SAMPLE_RATE = 44100;  // CD-quality audio
//    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
//    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
//    private ExecutorService executorService;
//    private Button startRecordButton, stopRecordButton, playButton;
//    private TextView recordingTimeText;
//    private boolean isRecording = false;
//    private AudioRecord audioRecord;
//    private int bufferSize;
//
//    private Thread recordingThread;
//    private String wavFilePath;
//    private MediaPlayer mediaPlayer;
//
//    // Timer variables
//    private long startTime = 0L;
//    private Handler timerHandler = new Handler();
//    private long timeInMilliseconds = 0L;
//    private long updatedTime = 0L;
//    private long timeSwapBuff = 0L;
//
//    private ActivityResultLauncher<String> requestPermissionLauncher;
//
//    private FragmentHomeBinding binding;
//
//    public View onCreateView(@NonNull LayoutInflater inflater,
//                             ViewGroup container, Bundle savedInstanceState) {
//
//
//        binding = FragmentHomeBinding.inflate(inflater, container, false);
//        View root = binding.getRoot();
//        initalizeViewElements(root);
//        initPermission();
//        executorService = Executors.newSingleThreadExecutor();
//
//        return root;
//    }
//
//    private void initPermission() {
//        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
//                isGranted -> {
//                    if (isGranted) {
//                        checkPermissionAndRun();
//                    } else {
//                        Toast.makeText(getActivity(), "Permission denied", Toast.LENGTH_SHORT).show();
//
//                        // Permission denied, handle accordingly
//                    }
//                });
////        checkPermissionAndRun();
//    }
//
//    private void checkPermissionAndRun() {
//        if (ContextCompat.checkSelfPermission(
//                getActivity(), Manifest.permission.RECORD_AUDIO) ==
//                PackageManager.PERMISSION_GRANTED) {
//            // Permission is granted, safe to call the function
//            startRecording();
//        } else {
//            // Request the permission
//            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
//        }
//    }
//
//
//    private void initalizeViewElements(View root) {
//        startRecordButton = root.findViewById(R.id.start_record_button);
//        stopRecordButton = root.findViewById(R.id.stop_record_button);
//        recordingTimeText = root.findViewById(R.id.recording_time);
//        playButton = root.findViewById(R.id.play_button);
//        stopRecordButton.setEnabled(false);
//        startRecordButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//
//                checkPermissionAndRecord();
//            }
//        });
//
//        // Stop recording
//        stopRecordButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                stopRecording();
//            }
//        });
//
//        playButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                playAudio();
//            }
//        });
//    }
//
//    private void checkPermissionAndRecord() {
//        if (ContextCompat.checkSelfPermission(
//                getActivity(), Manifest.permission.RECORD_AUDIO) ==
//                PackageManager.PERMISSION_GRANTED) {
//            startRecording();
//        } else {
//            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
//        }
//
//    }
//
//    // Play the recorded audio using MediaPlayer
//    private void playAudio() {
//        if (mediaPlayer == null) {
//            mediaPlayer = new MediaPlayer();
//        }
//
//        try {
//            mediaPlayer.setDataSource(wavFilePath);
//            mediaPlayer.prepare();
//            mediaPlayer.start();
//            playButton.setEnabled(false);  // Disable play button during playback
//
//            // When playback completes, reset and enable the play button
//            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                @Override
//                public void onCompletion(MediaPlayer mp) {
//                    playButton.setEnabled(true);
//                    mediaPlayer.reset(); // Reset the MediaPlayer after playing
//                }
//            });
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
//    private void startRecording() {
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//        String fileName = "recorded_audio_" + timeStamp + ".wav";
//        wavFilePath = getActivity().getFilesDir().getAbsolutePath() + "/" + fileName;
//        // Get the minimum buffer size required for the AudioRecord object
//        bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
//
//        // Check if the buffer size is valid (greater than zero)
//        if (bufferSize == AudioRecord.ERROR_BAD_VALUE || bufferSize == AudioRecord.ERROR || bufferSize <= 0) {
//            Log.e("AudioRecord", "Invalid buffer size returned: " + bufferSize);
//            return; // Exit or handle the error
//        }
//
//        // Initialize AudioRecord with the calculated buffer size
//        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, bufferSize);
//
//        // Check if AudioRecord was initialized correctly
//        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
//            Log.e("AudioRecord", "AudioRecord initialization failed");
//            return; // Exit or handle the error
//        }
//
//        // Start recording
//        audioRecord.startRecording();
//        isRecording = true;
//
//        startTime = SystemClock.uptimeMillis();
//        timerHandler.postDelayed(updateTimerThread, 0);
//
//        recordingThread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                writeAudioDataToFile();
//            }
//        }, "AudioRecorder Thread");
//        recordingThread.start();
//
//        startRecordButton.setEnabled(false);
//        stopRecordButton.setEnabled(true);
//
//    }
//
//    private void stopRecording() {
//        if (audioRecord != null && isRecording) {
//            isRecording = false;
//            audioRecord.stop();
//            audioRecord.release();
//            audioRecord = null;
//            recordingThread = null;
//
//            timeSwapBuff += timeInMilliseconds;
//            timerHandler.removeCallbacks(updateTimerThread);
//
//            startRecordButton.setEnabled(true);
//            stopRecordButton.setEnabled(false);
//
//            // Convert raw PCM to WAV
//            executorService.execute(new Runnable() {
//                @Override
//                public void run() {
//                    convertPcmToWav(wavFilePath);
//                    playButton.setEnabled(true);
//                }
//            });
//
////            convertPcmToWav(wavFilePath);
//
//        }
//    }
//
//    // Timer thread to update the recording time
//    private Runnable updateTimerThread = new Runnable() {
//        public void run() {
//            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
//            updatedTime = timeSwapBuff + timeInMilliseconds;
//
//            int secs = (int) (updatedTime / 1000);
//            int mins = secs / 60;
//            secs = secs % 60;
//            recordingTimeText.setText(String.format("%02d:%02d", mins, secs));
//
//            timerHandler.postDelayed(this, 1000);
//        }
//    };
//
//    private void writeAudioDataToFile() {
//        byte[] data = new byte[bufferSize];
//        FileOutputStream os = null;
//        try {
//            os = new FileOutputStream(wavFilePath);
//            while (isRecording) {
//                int read = audioRecord.read(data, 0, bufferSize);
//                if (read != AudioRecord.ERROR_INVALID_OPERATION) {
//                    os.write(data, 0, read);
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (os != null) {
//                    os.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    // Convert PCM to WAV file
//    private void convertPcmToWav(String pcmFilePath) {
//        File pcmFile = new File(pcmFilePath);
//        File wavFile = new File(wavFilePath);
//
//        try {
//            FileInputStream fis = new FileInputStream(pcmFile);
//            FileOutputStream fos = new FileOutputStream(wavFile);
//
//            long totalAudioLen = fis.getChannel().size();
//            long totalDataLen = totalAudioLen + 36;
//            long byteRate = SAMPLE_RATE * 2; // 16-bit mono audio
//
//            writeWavHeader(fos, totalAudioLen, totalDataLen, SAMPLE_RATE, 1, byteRate);
//
//            byte[] buffer = new byte[bufferSize];
//            while (fis.read(buffer) != -1) {
//                fos.write(buffer);
//            }
//
//            fis.close();
//            fos.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void writeWavHeader(FileOutputStream out, long totalAudioLen, long totalDataLen, long sampleRate, int channels, long byteRate) throws IOException {
//        byte[] header = new byte[44];
//
//        header[0] = 'R'; // RIFF/WAVE header
//        header[1] = 'I';
//        header[2] = 'F';
//        header[3] = 'F';
//        header[4] = (byte) (totalDataLen & 0xff);
//        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
//        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
//        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
//        header[8] = 'W';
//        header[9] = 'A';
//        header[10] = 'V';
//        header[11] = 'E';
//        header[12] = 'f'; // 'fmt ' chunk
//        header[13] = 'm';
//        header[14] = 't';
//        header[15] = ' ';
//        header[16] = 16; // Subchunk1Size for PCM
//        header[17] = 0;
//        header[18] = 0;
//        header[19] = 0;
//        header[20] = 1; // AudioFormat (1 = PCM)
//        header[21] = 0;
//        header[22] = (byte) channels;
//        header[23] = 0;
//        header[24] = (byte) (sampleRate & 0xff);
//        header[25] = (byte) ((sampleRate >> 8) & 0xff);
//        header[26] = (byte) ((sampleRate >> 16) & 0xff);
//        header[27] = (byte) ((sampleRate >> 24) & 0xff);
//        header[28] = (byte) (byteRate & 0xff);
//        header[29] = (byte) ((byteRate >> 8) & 0xff);
//        header[30] = (byte) ((byteRate >> 16) & 0xff);
//        header[31] = (byte) ((byteRate >> 24) & 0xff);
//        header[32] = (byte) (2 * 16 / 8); // BlockAlign
//        header[33] = 0;
//        header[34] = 16; // Bits per sample
//        header[35] = 0;
//        header[36] = 'd';
//        header[37] = 'a';
//        header[38] = 't';
//        header[39] = 'a';
//        header[40] = (byte) (totalAudioLen & 0xff);
//        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
//        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
//        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
//
//        out.write(header, 0, 44);
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        if (mediaPlayer != null) {
//            mediaPlayer.release();
//            mediaPlayer = null;
//        }
//        if (executorService != null) {
//            executorService.shutdown(); // Shut down the executor service
//        }
//    }
//}