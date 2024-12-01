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
import tn.louay.groupnotes.model.Group;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {
    private List<Group> groups;
    private final GroupClickListener listener;

    public interface GroupClickListener {
        void onGroupClick(Group group);
        void onEditClick(Group group);
        void onDeleteClick(Group group);
    }

    public GroupAdapter(List<Group> groups, GroupClickListener listener) {
        this.groups = groups;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Group group = groups.get(position);
        holder.bind(group);
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    public void updateGroups(List<Group> newGroups) {
        this.groups = newGroups;
        notifyDataSetChanged();
    }

    class GroupViewHolder extends RecyclerView.ViewHolder {
        private final TextView groupNameText;
        private final ImageButton optionsButton;

        GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            groupNameText = itemView.findViewById(R.id.groupNameText);
            optionsButton = itemView.findViewById(R.id.groupOptionsButton);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onGroupClick(groups.get(position));
                }
            });
        }

        void bind(Group group) {
            groupNameText.setText(group.getName());
            
            optionsButton.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(v.getContext(), v);
                popup.inflate(R.menu.group_options_menu);
                popup.setOnMenuItemClickListener(item -> {
                    int itemId = item.getItemId();
                    if (itemId == R.id.action_edit) {
                        listener.onEditClick(group);
                        return true;
                    } else if (itemId == R.id.action_delete) {
                        listener.onDeleteClick(group);
                        return true;
                    }
                    return false;
                });
                popup.show();
            });
        }
    }
} 