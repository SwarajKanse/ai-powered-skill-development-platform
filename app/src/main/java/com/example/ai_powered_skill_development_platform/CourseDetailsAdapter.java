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
import java.util.ArrayList;
import java.util.List;

public class CourseDetailsAdapter extends RecyclerView.Adapter<CourseDetailsAdapter.CourseViewHolder> {

    private List<Course> originalList;
    private List<Course> filteredList;

    public CourseDetailsAdapter(List<Course> courseList) {
        this.originalList = courseList;
        this.filteredList = new ArrayList<>(courseList);
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course_details, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course course = filteredList.get(position);
        holder.courseTitle.setText(course.getTitle());
        String title = course.getTitle();
        System.out.println("Course title: " + title);
        holder.courseDescription.setText(course.getDescription());
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
        return filteredList.size();
    }

    /**
     * Filters the adapter data based on the query.
     *
     * @param query The text to filter courses by.
     */
    public void filter(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(originalList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Course course : originalList) {
                if (course.getTitle().toLowerCase().contains(lowerQuery) ||
                        course.getDescription().toLowerCase().contains(lowerQuery)) {
                    filteredList.add(course);
                }
            }
        }
        notifyDataSetChanged();
    }

    public static class CourseViewHolder extends RecyclerView.ViewHolder {
        TextView courseTitle, courseDescription;
        ImageView thumbnail;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            courseTitle = itemView.findViewById(R.id.tv_course_title);
            courseDescription = itemView.findViewById(R.id.tv_course_description);
            thumbnail = itemView.findViewById(R.id.iv_thumbnail);
        }
    }
}
