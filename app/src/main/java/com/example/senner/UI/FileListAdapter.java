package com.example.senner.UI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.senner.R;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.ViewHolder> {
    private Context mContext;
    private List<File> mFiles;

    public FileListAdapter(Context context, List<File> files) {
        mContext = context;
        mFiles = files;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        File currentFile = mFiles.get(position);

        if (currentFile.isDirectory()) {
            holder.iconView.setImageResource(R.drawable.round_folder);
        } else if (currentFile.getName().endsWith(".pdf")) {
            holder.iconView.setImageResource(R.drawable.round_picture_as_pdf);
        } else if (currentFile.getName().endsWith(".doc") || currentFile.getName().endsWith(".docx") || currentFile.getName().endsWith(".txt")) {
            holder.iconView.setImageResource(R.drawable.round_text);
        } else if (currentFile.getName().endsWith(".jpg") || currentFile.getName().endsWith(".jpeg") || currentFile.getName().endsWith(".png")) {
            holder.iconView.setImageResource(R.drawable.round_photo);
        } else {
            holder.iconView.setImageResource(R.drawable.round_default_file);
        }

        holder.nameView.setText(currentFile.getName());

        // 为iconView添加点击事件监听器
        holder.iconView.setOnClickListener(v -> {
            if (currentFile.isDirectory()) {
                // 如果是文件夹，则打开新的文件夹
                openDirectory(currentFile);
            } else {
                // 如果是文件，则打开文件
                openFile(currentFile);
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void openDirectory(File directory) {
        if (directory.isDirectory()) {
            mFiles.clear();
            mFiles.addAll(Arrays.asList(Objects.requireNonNull(directory.listFiles())));
            notifyDataSetChanged();
        }
    }

    private void openFile(File file) {

    }

    // 获取文件的MIME类型
    private String getMimeType(String url) {
        String extension = url.substring(url.lastIndexOf(".") + 1);
        String mimeTypeMap = MimeTypeMap.getFileExtensionFromUrl(extension);
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(mimeTypeMap);
    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView iconView;
        public TextView nameView;

        public ViewHolder(View itemView) {
            super(itemView);
            iconView = itemView.findViewById(R.id.icon_image_view);
            nameView = itemView.findViewById(R.id.name_text_view);
        }

    }
}
