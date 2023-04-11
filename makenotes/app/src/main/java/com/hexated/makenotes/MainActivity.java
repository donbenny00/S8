package com.hexated.makenotes;

import android.annotation.SuppressLint;
import android.content.ContentProvider;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashSet;

public class MainActivity extends AppCompatActivity {


    public SQLiteDatabase mDatabase;
    public static final String DATABASE_NAME = "notes.db";
    public static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "notes";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_DATE = "date";
    private static final String CREATE_TABLE_QUERY =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TITLE + " TEXT, " +
                    COLUMN_CONTENT + " TEXT, " +
                    COLUMN_DATE + " DATE)";
    private static class NotesDataBase extends SQLiteOpenHelper {

        public NotesDataBase(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // This method is called when the database is created for the first time
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This method is called when the database needs to be upgraded to a new version
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton fab=(FloatingActionButton)findViewById(R.id.fab_add);
        NotesDataBase notesDataBase = new NotesDataBase(this);
        mDatabase = notesDataBase.getWritableDatabase();
        mDatabase.execSQL(CREATE_TABLE_QUERY);
        mDatabase = openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);
        updateNotesList();




        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), NoteEditorActivity.class);
                startActivity(intent);
            }
        });
    }

    private void updateNotesList() {
        ArrayList<Note> notesList = new ArrayList<>();
        String[] projection = { "id", "title", "content", "date" };
        String orderBy = "datetime(" + COLUMN_DATE + ", 'dd/MM/yyyy hh:mm a')";
        Cursor cursor = mDatabase.query(TABLE_NAME, projection, null, null, null, null, orderBy + " DESC");

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
                @SuppressLint("Range") String title = cursor.getString(cursor.getColumnIndex(COLUMN_TITLE));
                @SuppressLint("Range") String content = cursor.getString(cursor.getColumnIndex(COLUMN_CONTENT));
                @SuppressLint("Range") String date = cursor.getString(cursor.getColumnIndex(COLUMN_DATE));
                notesList.add(new Note(id, title, content, date));
            } while (cursor.moveToNext());
        }
        cursor.close();

        // Clear the existing notes from the UI
        LinearLayout notesLayout = findViewById(R.id.linearHold);
        notesLayout.removeAllViews();

        // Inflate the note container layout and add it to the UI for each note
        LayoutInflater inflater = getLayoutInflater();
        for (int i = 0; i < notesList.size(); i++) {
            Note note = notesList.get(i);
            View noteContainer = inflater.inflate(R.layout.note_container, null);

            noteContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int noteId = note.getId();
                    String content = note.getContent();
                    String title  = note.getTitle();
                    Intent intent = new Intent(getApplicationContext(), NoteEditorActivity.class);
                    intent.putExtra("noteId",noteId);
                    intent.putExtra("content",content);
                    intent.putExtra("title",title);
                    startActivity(intent);
                }
            });

            noteContainer.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Delete Note");
                    builder.setMessage("Are you sure you want to delete this note?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Handle the "Yes" button click here
                            mDatabase.delete(TABLE_NAME, COLUMN_ID + " = ?", new String[]{String.valueOf(note.getId())});
                            // Remove the note container from the UI
                            notesLayout.removeView(noteContainer);
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Handle the "No" button click here
                        }
                    });
                    builder.show();
                    return false;
                }
            });

            TextView titleTextView = noteContainer.findViewById(R.id.title_note);
            TextView contentTextView = noteContainer.findViewById(R.id.content);
            TextView dateTextView = noteContainer.findViewById(R.id.date_created);
            titleTextView.setText(note.getTitle());
            contentTextView.setText(note.getContent());
            dateTextView.setText(note.getDate());
            notesLayout.addView(noteContainer);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("Resumed");
        updateNotesList();
    }
    public class Note {
        private int id;
        private String title;
        private String content;
        private String date;

        public Note(int id, String title, String content, String date) {
            this.id = id;
            this.title = title;
            this.content = content;
            this.date = date;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }
    }


}
