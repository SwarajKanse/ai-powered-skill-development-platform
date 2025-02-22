package com.example.ai_powered_skill_development_platform;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class LearningPathAdapter extends RecyclerView.Adapter<LearningPathAdapter.ViewHolder> {

    private List<LearningPath> learningPathList;

    public LearningPathAdapter(List<LearningPath> learningPathList) {
        this.learningPathList = learningPathList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_learning_path, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LearningPath learningPath = learningPathList.get(position);
        holder.pathNameTextView.setText(learningPath.getName());
        holder.progressBar.setProgress(learningPath.getProgress());
        holder.progressTextView.setText(learningPath.getProgress() + "%");
    }

    @Override
    public int getItemCount() {
        return learningPathList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView pathNameTextView, progressTextView;
        ProgressBar progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            pathNameTextView = itemView.findViewById(R.id.pathNameTextView);
            progressBar = itemView.findViewById(R.id.progressBar);
            progressTextView = itemView.findViewById(R.id.progressTextView);
        }
    }
}
