package com.example.petopiaapp.ui.qna;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.petopiaapp.R;

public class qnaFragment extends Fragment {

    private qnaViewModel qnaViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        qnaViewModel =
                ViewModelProviders.of(this).get(qnaViewModel.class);
        View root = inflater.inflate(R.layout.fragment_qna, container, false);
        final TextView textView = root.findViewById(R.id.text_qna);
        qnaViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}