package tn.louay.groupnotes;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
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

import tn.louay.groupnotes.adapter.GroupAdapter;
import tn.louay.groupnotes.model.Group;

public class DashboardActivity extends AppCompatActivity implements GroupAdapter.GroupClickListener {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GroupAdapter adapter;
    private List<Group> groups;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Set up toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize RecyclerView
        RecyclerView recyclerView = findViewById(R.id.groupsRecyclerView);
        groups = new ArrayList<>();
        adapter = new GroupAdapter(groups, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Set up FAB
        FloatingActionButton fab = findViewById(R.id.addGroupFab);
        fab.setOnClickListener(v -> showAddGroupDialog());

        // Load groups
        loadGroups();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dashboard_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            mAuth.signOut();
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadGroups() {
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("groups")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error loading groups: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    groups.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            Group group = doc.toObject(Group.class);
                            group.setId(doc.getId());
                            groups.add(group);
                        }
                    }
                    adapter.updateGroups(groups);
                });
    }

    private void showAddGroupDialog() {
        EditText input = new EditText(this);
        input.setHint("Group Name");

        new MaterialAlertDialogBuilder(this)
                .setTitle("Create New Group")
                .setView(input)
                .setPositiveButton("Create", (dialog, which) -> {
                    String groupName = input.getText().toString().trim();
                    if (!groupName.isEmpty()) {
                        createGroup(groupName);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void createGroup(String name) {
        String userId = mAuth.getCurrentUser().getUid();
        Map<String, Object> group = new HashMap<>();
        group.put("name", name);
        group.put("createdBy", userId);
        group.put("createdAt", System.currentTimeMillis());

        db.collection("groups")
                .add(group)
                .addOnSuccessListener(documentReference -> 
                    Toast.makeText(this, "Group created successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> 
                    Toast.makeText(this, "Error creating group: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onGroupClick(Group group) {
        Intent intent = new Intent(this, GroupDetailsActivity.class);
        intent.putExtra("groupId", group.getId());
        intent.putExtra("groupName", group.getName());
        startActivity(intent);
    }

    @Override
    public void onEditClick(Group group) {
        EditText input = new EditText(this);
        input.setText(group.getName());

        new MaterialAlertDialogBuilder(this)
                .setTitle("Edit Group Name")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        updateGroup(group.getId(), newName);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDeleteClick(Group group) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Group")
                .setMessage("Are you sure you want to delete this group?")
                .setPositiveButton("Delete", (dialog, which) -> deleteGroup(group.getId()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateGroup(String groupId, String newName) {
        db.collection("groups").document(groupId)
                .update("name", newName)
                .addOnSuccessListener(aVoid -> 
                    Toast.makeText(this, "Group updated successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> 
                    Toast.makeText(this, "Error updating group: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show());
    }

    private void deleteGroup(String groupId) {
        db.collection("groups").document(groupId)
                .delete()
                .addOnSuccessListener(aVoid -> 
                    Toast.makeText(this, "Group deleted successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> 
                    Toast.makeText(this, "Error deleting group: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show());
    }
} 