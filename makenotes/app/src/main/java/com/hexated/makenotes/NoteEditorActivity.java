package com.hexated.makenotes;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class NoteEditorActivity extends AppCompatActivity {
    private int noteId = -1;
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
    static class NotesDataBase extends SQLiteOpenHelper {

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
        Intent intent = getIntent();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_editor);
        EditText editTextContent = findViewById(R.id.editTextContent);
        EditText editTextTitle = findViewById(R.id.titleTxt);
        FloatingActionButton fabSave = findViewById(R.id.fabSave);
        String content = intent.getStringExtra("content");
        String title = intent.getStringExtra("title");
        editTextContent.setText(content);
        editTextTitle.setText(title);

        NotesDataBase notesDataBase = new NotesDataBase(this);
        mDatabase = notesDataBase.getWritableDatabase();
        mDatabase.execSQL(CREATE_TABLE_QUERY);
        fabSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = editTextTitle.getText().toString();
                String content = editTextContent.getText().toString();
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
                String time = dateFormat.format(calendar.getTime());
                ContentValues values = new ContentValues();
                values.put(COLUMN_TITLE, title);
                values.put(COLUMN_CONTENT, content);
                values.put(COLUMN_DATE, time);
                int temp;
                  temp= intent.getIntExtra("noteId", -1);
               if(temp==-1){
                   System.out.println("skipped");
               }else{
                   noteId= temp;
               }

                // Check if a row with the same ID exists
                Cursor cursor = mDatabase.query(TABLE_NAME, null,
                        COLUMN_ID + " = ?", new String[]{String.valueOf(noteId)}, null, null, null);

                if (cursor.moveToFirst()) {
                    // If a row with the same ID exists, update that row with the new values
                    mDatabase.update(TABLE_NAME, values, COLUMN_ID + " = ?",
                            new String[]{String.valueOf(noteId)});
                    Toast.makeText(NoteEditorActivity.this, "Note updated!", Toast.LENGTH_SHORT).show();

                } else {
                    long id = mDatabase.insert(TABLE_NAME, null, values);
                    noteId = (int) id;
                    System.out.println(noteId);
                    Toast.makeText(NoteEditorActivity.this, "Note added!", Toast.LENGTH_SHORT).show();

                }

                cursor.close();

            }
        });


    }
}