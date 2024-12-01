package tn.louay.groupnotes.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import tn.louay.groupnotes.R;
import tn.louay.groupnotes.model.Note;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {
    private List<Note> notes;
    private final NoteClickListener listener;
    private final String currentUserId;

    public interface NoteClickListener {
        void onNoteClick(Note note);
        void onEditClick(Note note);
        void onDeleteClick(Note note);
    }

    public NoteAdapter(List<Note> notes, String currentUserId, NoteClickListener listener) {
        this.notes = notes;
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notes.get(position);
        holder.bind(note);
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public void updateNotes(List<Note> newNotes) {
        this.notes = newNotes;
        notifyDataSetChanged();
    }

    class NoteViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleText;
        private final TextView contentText;
        private final TextView authorText;
        private final ImageButton optionsButton;

        NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.noteTitleText);
            contentText = itemView.findViewById(R.id.noteContentText);
            authorText = itemView.findViewById(R.id.noteAuthorText);
            optionsButton = itemView.findViewById(R.id.noteOptionsButton);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onNoteClick(notes.get(position));
                }
            });
        }

        void bind(Note note) {
            titleText.setText(note.getTitle());
            contentText.setText(note.getContent());
            authorText.setText("Created by " + note.getCreatedBy());
            
            // Only show options button for notes created by the current user
            optionsButton.setVisibility(
                note.getCreatedBy().equals(currentUserId) ? View.VISIBLE : View.GONE
            );
            
            optionsButton.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(v.getContext(), v);
                popup.inflate(R.menu.note_options_menu);
                popup.setOnMenuItemClickListener(item -> {
                    int itemId = item.getItemId();
                    if (itemId == R.id.action_edit) {
                        listener.onEditClick(note);
                        return true;
                    } else if (itemId == R.id.action_delete) {
                        listener.onDeleteClick(note);
                        return true;
                    }
                    return false;
                });
                popup.show();
            });
        }
    }
} 