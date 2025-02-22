package com.example.ai_powered_skill_development_platform;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class CourseDetailsAdapter extends RecyclerView.Adapter<CourseDetailsAdapter.CourseViewHolder> {

    private List<Course> courseList;

    public CourseDetailsAdapter(List<Course> courseList) {
        this.courseList = courseList;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course_details, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course course = courseList.get(position);
        holder.courseTitle.setText(course.getTitle());
        holder.courseDescription.setText(course.getDescription());
        holder.progressBar.setProgress(course.getProgress());
        if (course.isOnlineImage()) {
            Glide.with(holder.itemView.getContext())
                    .load(course.getThumbnailUrl())
                    .into(holder.thumbnail);
        } else {
            holder.thumbnail.setImageResource(course.getThumbnailResId());
        }
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    public static class CourseViewHolder extends RecyclerView.ViewHolder {
        TextView courseTitle, courseDescription;
        ProgressBar progressBar;
        ImageView thumbnail;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            courseTitle = itemView.findViewById(R.id.tv_course_title);
            courseDescription = itemView.findViewById(R.id.tv_course_description);
            progressBar = itemView.findViewById(R.id.course_progress);
            thumbnail = itemView.findViewById(R.id.iv_thumbnail);
        }
    }
}
