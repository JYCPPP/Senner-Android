package com.example.senner.Fragment;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.senner.Helper.FileScanner;
import com.example.senner.Helper.SharedPreferenceHelper;
import com.example.senner.R;
import com.example.senner.UI.FileListAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProjectFragment extends Fragment {

    private RecyclerView mFileRecyclerView;
    private FileListAdapter mAdapter;
    private final FileScanner mFileScanner = new FileScanner();
    private List<File> mFiles;
    private final SharedPreferenceHelper sharedPreferenceHelper = new SharedPreferenceHelper();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.fragment_project, container, false);

        mFileRecyclerView = view.findViewById(R.id.recyclerview);
        mFileRecyclerView.setLayoutManager(new GridLayoutManager(requireActivity(), 3, GridLayoutManager.VERTICAL, false));

        mFiles = new ArrayList<>();
        mAdapter = new FileListAdapter(requireActivity(), mFiles);
        mFileRecyclerView.setAdapter(mAdapter);

        // 在onCreate()方法中启动任务
        new ScanFilesTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        return view;
    }

    @SuppressLint("StaticFieldLeak")
    private class ScanFilesTask extends AsyncTask<Void, Void, List<File>> {
        @Override
        protected List<File> doInBackground(Void... voids) {
            String ProjectPath = sharedPreferenceHelper.getString(requireActivity(),"Project Path", "");
            Log.d("Project Path", ProjectPath);
            return mFileScanner.getFiles(ProjectPath);
        }

        @SuppressLint("NotifyDataSetChanged")
        @Override
        protected void onPostExecute(List<File> files) {
            super.onPostExecute(files);
            mFiles.clear();
            mFiles.addAll(files);
            mAdapter.notifyDataSetChanged();
        }
    }
}