package com.harshatalap1474.playbox;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // UI Components
    private ListView songListView;
    private Button btnPlay, btnPrev, btnNext;
    private TextView txtSongName;

    // Logic Variables
    private ArrayList<String> songTitleList; // List of song names to show
    private ArrayList<String> songPathList;  // List of actual file paths
    private MediaPlayer mediaPlayer;
    private int currentSongIndex = -1;       // Which song is currently selected?

    // Permission Request Code
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

        songTitleList = new ArrayList<>();
        songPathList = new ArrayList<>();
        mediaPlayer = new MediaPlayer();

        // 2. Check Permissions before loading songs
        if (checkPermission()) {
            loadSongs();
        } else {
            requestPermission();
        }

        // 3. Handle Song Selection from List
        songListView.setOnItemClickListener((parent, view, position, id) -> {
            playSong(position);
        });

        // 4. Button Logic
        btnPlay.setOnClickListener(v -> {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                btnPlay.setText("Play"); // If using a drawable, change image here
                btnPlay.setText("Play");
            } else {
                if(currentSongIndex != -1) {
                    mediaPlayer.start();
                    btnPlay.setText("Pause");
                }
            }
        });

        btnNext.setOnClickListener(v -> {
            if (currentSongIndex < songPathList.size() - 1) {
                playSong(currentSongIndex + 1);
            }
        });

        btnPrev.setOnClickListener(v -> {
            if (currentSongIndex > 0) {
                playSong(currentSongIndex - 1);
            }
        });
    }

    // --- HELPER METHODS ---

    // Load songs from device storage
    private void loadSongs() {
        ContentResolver contentResolver = getContentResolver();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        // This query looks for music files
        Cursor cursor = contentResolver.query(songUri, null, null, null, null);

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

        // Show the list on screen using a simple Adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, songTitleList);
        songListView.setAdapter(adapter);
    }

    // Logic to play audio
    private void playSong(int index) {
        try {
            mediaPlayer.reset(); // Clean up previous resource
            mediaPlayer.setDataSource(songPathList.get(index));
            mediaPlayer.prepare();
            mediaPlayer.start();

            currentSongIndex = index;
            txtSongName.setText(songTitleList.get(index));
            btnPlay.setText("Pause");

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Unable to play song", Toast.LENGTH_SHORT).show();
        }
    }

    // --- PERMISSION HANDLING ---

    private boolean checkPermission() {
        // Android 13 (API 33) uses different permission
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
                Toast.makeText(this, "Permission Denied. Cannot load songs.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}