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
import android.widget.ImageButton; // Changed from Button
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // UI Components (Matching your XML IDs)
    private ListView songListView;
    private ImageButton btnPlay, btnPrev, btnNext; // ImageButtons for icons
    private TextView txtSongName;

    // Logic Variables
    private ArrayList<String> songTitleList;
    private ArrayList<String> songPathList;
    private MediaPlayer mediaPlayer;
    private int currentSongIndex = -1;
    private static final int PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Initialize UI Views
        songListView = findViewById(R.id.songListView);
        btnPlay = findViewById(R.id.btnPlay);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        txtSongName = findViewById(R.id.txtSongName);

        // 2. Initialize Logic Lists
        songTitleList = new ArrayList<>();
        songPathList = new ArrayList<>();
        mediaPlayer = new MediaPlayer();

        // 3. Permission Check
        if (checkPermission()) {
            loadSongs();
        } else {
            requestPermission();
        }

        // 4. Handle Song Selection (Clicking a song in the list)
        songListView.setOnItemClickListener((parent, view, position, id) -> {
            playSong(position);
        });

        // 5. Play/Pause Button Logic
        btnPlay.setOnClickListener(v -> {
            if (mediaPlayer.isPlaying()) {
                // Determine logic: If playing, pause it.
                mediaPlayer.pause();
                // Change Icon to PLAY arrow
                btnPlay.setImageResource(android.R.drawable.ic_media_play);
            } else {
                // If paused and a song is selected, resume.
                if (currentSongIndex != -1) {
                    mediaPlayer.start();
                    // Change Icon to PAUSE bars
                    btnPlay.setImageResource(android.R.drawable.ic_media_pause);
                } else {
                    // If no song is selected yet, play the first one if available
                    if (!songPathList.isEmpty()) {
                        playSong(0);
                    } else {
                        Toast.makeText(this, "No songs found", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // 6. Next Button Logic
        btnNext.setOnClickListener(v -> {
            if (currentSongIndex < songPathList.size() - 1) {
                playSong(currentSongIndex + 1);
            } else {
                // Loop back to start (Optional)
                playSong(0);
            }
        });

        // 7. Previous Button Logic
        btnPrev.setOnClickListener(v -> {
            if (currentSongIndex > 0) {
                playSong(currentSongIndex - 1);
            }
        });

        // Listener to auto-play next song when current one ends
        mediaPlayer.setOnCompletionListener(mp -> {
            if (currentSongIndex < songPathList.size() - 1) {
                playSong(currentSongIndex + 1);
            }
        });
    }

    // --- HELPER METHODS ---

    private void loadSongs() {
        ContentResolver contentResolver = getContentResolver();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";

        Cursor cursor = contentResolver.query(songUri, null, selection, null, sortOrder);

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

        // Using simple list item layout for the list rows
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

            // Set the icon to PAUSE because the song just started playing
            btnPlay.setImageResource(android.R.drawable.ic_media_pause);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error playing song", Toast.LENGTH_SHORT).show();
        }
    }

    // --- PERMISSIONS (Boilerplate) ---

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
                Toast.makeText(this, "Permission Required to View Songs", Toast.LENGTH_SHORT).show();
            }
        }
    }
}