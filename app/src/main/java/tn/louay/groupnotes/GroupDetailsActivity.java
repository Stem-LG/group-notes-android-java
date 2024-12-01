package tn.louay.groupnotes;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tn.louay.groupnotes.adapter.NoteAdapter;
import tn.louay.groupnotes.model.Note;

public class GroupDetailsActivity extends AppCompatActivity implements NoteAdapter.NoteClickListener {
    private String groupId;
    private String groupName;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private NoteAdapter adapter;
    private List<Note> notes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);

        // Get group details from intent
        groupId = getIntent().getStringExtra("groupId");
        groupName = getIntent().getStringExtra("groupName");

        if (groupId == null || groupName == null) {
            Toast.makeText(this, "Error: Group details not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Set up toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(groupName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Initialize RecyclerView
        RecyclerView recyclerView = findViewById(R.id.notesRecyclerView);
        notes = new ArrayList<>();
        adapter = new NoteAdapter(notes, mAuth.getCurrentUser().getUid(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Set up FABs
        FloatingActionButton addNoteFab = findViewById(R.id.addNoteFab);
        FloatingActionButton chatFab = findViewById(R.id.chatFab);

        addNoteFab.setOnClickListener(v -> showAddNoteDialog());
        chatFab.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("groupId", groupId);
            intent.putExtra("groupName", groupName);
            startActivity(intent);
        });

        // Load notes
        loadNotes();
    }

    private void loadNotes() {
        db.collection("notes")
                .whereEqualTo("groupId", groupId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error loading notes: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    notes.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            Note note = doc.toObject(Note.class);
                            note.setId(doc.getId());
                            notes.add(note);
                        }
                    }
                    adapter.updateNotes(notes);
                });
    }

    private void showAddNoteDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_note, null);
        EditText titleInput = dialogView.findViewById(R.id.noteTitleInput);
        EditText contentInput = dialogView.findViewById(R.id.noteContentInput);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Create New Note")
                .setView(dialogView)
                .setPositiveButton("Create", (dialog, which) -> {
                    String title = titleInput.getText().toString().trim();
                    String content = contentInput.getText().toString().trim();
                    if (!title.isEmpty() && !content.isEmpty()) {
                        createNote(title, content);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void createNote(String title, String content) {
        String userId = mAuth.getCurrentUser().getUid();
        long timestamp = System.currentTimeMillis();

        Map<String, Object> note = new HashMap<>();
        note.put("title", title);
        note.put("content", content);
        note.put("groupId", groupId);
        note.put("createdBy", userId);
        note.put("createdAt", timestamp);
        note.put("updatedAt", timestamp);

        db.collection("notes")
                .add(note)
                .addOnSuccessListener(documentReference -> 
                    Toast.makeText(this, "Note created successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> 
                    Toast.makeText(this, "Error creating note: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onNoteClick(Note note) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_view_note, null);
        ((TextView) dialogView.findViewById(R.id.noteTitleText)).setText(note.getTitle());
        ((TextView) dialogView.findViewById(R.id.noteContentText)).setText(note.getContent());
        ((TextView) dialogView.findViewById(R.id.noteAuthorText)).setText("Created by " + note.getCreatedBy());

        new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .show();
    }

    @Override
    public void onEditClick(Note note) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_note, null);
        EditText titleInput = dialogView.findViewById(R.id.noteTitleInput);
        EditText contentInput = dialogView.findViewById(R.id.noteContentInput);

        titleInput.setText(note.getTitle());
        contentInput.setText(note.getContent());

        new MaterialAlertDialogBuilder(this)
                .setTitle("Edit Note")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newTitle = titleInput.getText().toString().trim();
                    String newContent = contentInput.getText().toString().trim();
                    if (!newTitle.isEmpty() && !newContent.isEmpty()) {
                        updateNote(note.getId(), newTitle, newContent);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDeleteClick(Note note) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Note")
                .setMessage("Are you sure you want to delete this note?")
                .setPositiveButton("Delete", (dialog, which) -> deleteNote(note.getId()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateNote(String noteId, String newTitle, String newContent) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("title", newTitle);
        updates.put("content", newContent);
        updates.put("updatedAt", System.currentTimeMillis());

        db.collection("notes").document(noteId)
                .update(updates)
                .addOnSuccessListener(aVoid -> 
                    Toast.makeText(this, "Note updated successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> 
                    Toast.makeText(this, "Error updating note: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show());
    }

    private void deleteNote(String noteId) {
        db.collection("notes").document(noteId)
                .delete()
                .addOnSuccessListener(aVoid -> 
                    Toast.makeText(this, "Note deleted successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> 
                    Toast.makeText(this, "Error deleting note: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show());
    }
} 