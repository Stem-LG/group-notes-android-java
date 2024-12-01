package tn.louay.groupnotes;

import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tn.louay.groupnotes.adapter.MessageAdapter;
import tn.louay.groupnotes.model.Message;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";
    private String groupId;
    private String groupName;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private MessageAdapter adapter;
    private List<Message> messages;
    private EditText messageInput;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

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
        recyclerView = findViewById(R.id.messagesRecyclerView);
        messages = new ArrayList<>();
        adapter = new MessageAdapter(messages, mAuth.getCurrentUser().getUid());
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        // Initialize views
        messageInput = findViewById(R.id.messageInput);
        MaterialButton sendButton = findViewById(R.id.sendButton);

        // Set up send button
        sendButton.setOnClickListener(v -> sendMessage());

        // Set up input field
        messageInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });

        // Load messages
        loadMessages();
    }

    private void loadMessages() {
        Log.d(TAG, "Loading messages for group: " + groupId);
        db.collection("messages")
                .whereEqualTo("groupId", groupId)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error loading messages: ", error);
                        Toast.makeText(this, "Error loading messages: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    messages.clear();
                    if (value != null) {
                        Log.d(TAG, "Number of messages received: " + value.size());
                        for (QueryDocumentSnapshot doc : value) {
                            Message message = doc.toObject(Message.class);
                            message.setId(doc.getId());
                            messages.add(message);
                            Log.d(TAG, "Message loaded: " + message.getContent() + " from: " + message.getSenderName());
                        }
                    }
                    adapter.updateMessages(new ArrayList<>(messages));
                    if (!messages.isEmpty()) {
                        recyclerView.post(() -> recyclerView.smoothScrollToPosition(messages.size() - 1));
                    }
                });
    }

    private void sendMessage() {
        String content = messageInput.getText().toString().trim();
        if (content.isEmpty()) {
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        String userName = mAuth.getCurrentUser().getDisplayName();

        Map<String, Object> message = new HashMap<>();
        message.put("content", content);
        message.put("groupId", groupId);
        message.put("senderId", userId);
        message.put("senderName", userName);
        message.put("createdAt", System.currentTimeMillis());

        Log.d(TAG, "Sending message: " + content + " to group: " + groupId);
        db.collection("messages")
                .add(message)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Message sent successfully with ID: " + documentReference.getId());
                    messageInput.setText("");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error sending message: ", e);
                    Toast.makeText(this, "Error sending message: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
} 