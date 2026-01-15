package com.harshatalap1474.playbox;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    // UI Components
    private ListView songListView;
    private ImageButton btnPlay, btnPrev, btnNext;
    private TextView txtSongName, txtStartTime, txtEndTime;
    private SeekBar seekBar;

    // Logic Variables
    private ArrayList<String> songTitleList;
    private ArrayList<String> songPathList;
    private MediaPlayer mediaPlayer;
    private int currentSongIndex = -1;
    private Handler handler = new Handler(); // For updating SeekBar
    private static final int PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Initialize UI
        songListView = findViewById(R.id.songListView);
        btnPlay = findViewById(R.id.btnPlay);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        txtSongName = findViewById(R.id.txtSongName);
        txtStartTime = findViewById(R.id.txtStartTime);
        txtEndTime = findViewById(R.id.txtEndTime);
        seekBar = findViewById(R.id.seekBar);

        // 2. Initialize Logic
        songTitleList = new ArrayList<>();
        songPathList = new ArrayList<>();
        mediaPlayer = new MediaPlayer();

        // 3. Permission Check
        if (checkPermission()) {
            loadSongs();
        } else {
            requestPermission();
        }

        // 4. List Click Listener
        songListView.setOnItemClickListener((parent, view, position, id) -> playSong(position));

        // 5. Play Button
        btnPlay.setOnClickListener(v -> {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                btnPlay.setImageResource(android.R.drawable.ic_media_play);
            } else {
                if (currentSongIndex != -1) {
                    mediaPlayer.start();
                    btnPlay.setImageResource(android.R.drawable.ic_media_pause);
                } else if (!songPathList.isEmpty()) {
                    playSong(0);
                }
            }
        });

        // 6. Next / Prev Buttons
        btnNext.setOnClickListener(v -> {
            if (currentSongIndex < songPathList.size() - 1) playSong(currentSongIndex + 1);
            else playSong(0);
        });

        btnPrev.setOnClickListener(v -> {
            if (currentSongIndex > 0) playSong(currentSongIndex - 1);
        });

        // 7. Auto Play Next
        mediaPlayer.setOnCompletionListener(mp -> {
            if (currentSongIndex < songPathList.size() - 1) playSong(currentSongIndex + 1);
            else playSong(0); // Loop playlist
        });

        // 8. SEEKBAR LOGIC (New!)
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                    txtStartTime.setText(formatTime(progress));
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Start the background thread to update SeekBar
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                    txtStartTime.setText(formatTime(mediaPlayer.getCurrentPosition()));
                }
                handler.postDelayed(this, 100); // Check every 0.1 seconds
            }
        });
    }

    // --- HELPER METHODS ---

    private void loadSongs() {
        ContentResolver contentResolver = getContentResolver();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";

        Cursor cursor = contentResolver.query(songUri, null, null, null, sortOrder);

        if (cursor != null && cursor.moveToFirst()) {
            int titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);

            do {
                String thisTitle = cursor.getString(titleColumn);
                String thisPath = cursor.getString(dataColumn);
                songTitleList.add(thisTitle);
                songPathList.add(thisPath);
            } while (cursor.moveToNext());
            cursor.close();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, songTitleList);
        songListView.setAdapter(adapter);
    }

    private void playSong(int index) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(songPathList.get(index));
            mediaPlayer.prepare();
            mediaPlayer.start();

            currentSongIndex = index;
            txtSongName.setText(songTitleList.get(index));
            btnPlay.setImageResource(android.R.drawable.ic_media_pause);

            // Set Max for SeekBar
            seekBar.setMax(mediaPlayer.getDuration());
            txtEndTime.setText(formatTime(mediaPlayer.getDuration()));

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error playing song", Toast.LENGTH_SHORT).show();
        }
    }

    // Format milliseconds into MM:SS
    private String formatTime(int msec) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(msec),
                TimeUnit.MILLISECONDS.toSeconds(msec) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(msec))
        );
    }

    // --- PERMISSIONS ---
    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_AUDIO}, PERMISSION_REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadSongs();
            } else {
                Toast.makeText(this, "Permission Required", Toast.LENGTH_SHORT).show();
            }
        }
    }
}