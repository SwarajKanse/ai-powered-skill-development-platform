package com.example.ai_powered_skill_development_platform;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    private Context context;
    private List<Course> courseList;

    public CourseAdapter(Context context, List<Course> courseList) {
        this.context = context;
        this.courseList = courseList;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.course_section_layout, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course course = courseList.get(position);
        holder.courseTitle.setText(course.getTitle());
        holder.courseDescription.setText(course.getDescription());
        holder.courseProgress.setProgress(course.getProgress());

        if (course.isOnlineImage()) {
            Glide.with(holder.itemView.getContext())
                    .load(course.getThumbnailUrl())
                    .into(holder.courseThumbnail);
        } else {
            holder.courseThumbnail.setImageResource(course.getThumbnailResId());
        }
    }


    @Override
    public int getItemCount() {
        return courseList.size();
    }

    public static class CourseViewHolder extends RecyclerView.ViewHolder {
        TextView courseTitle, courseDescription;
        ImageView courseThumbnail;
        ProgressBar courseProgress;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            courseTitle = itemView.findViewById(R.id.course_title);
            courseDescription = itemView.findViewById(R.id.course_description);
            courseThumbnail = itemView.findViewById(R.id.course_thumbnail);
            courseProgress = itemView.findViewById(R.id.course_progress);
        }
    }
    // Add this method to your CourseAdapter class
    public void updateCourses(List<Course> courses) {
        this.courseList = new ArrayList<>(courses);
        // Optionally call notifyDataSetChanged() here instead of in the caller
    }
}
