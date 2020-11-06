package com.example.testapp4;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import static android.content.Context.MODE_PRIVATE;

public class Config extends AppCompatDialogFragment {
    private EditText tbF1;
    private EditText tbF2;

    private SharedPreferences sharedPreferences;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.config, null);

        builder.setView(view)
                .setTitle("Set command string for hot key")
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .setPositiveButton("set", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String f1 = tbF1.getText().toString();
                        String f2 = tbF2.getText().toString();
                        MainActivity.F1 = f1;
                        MainActivity.F2 = f2;
                        SharedPreferences.Editor editor = sharedPreferences.edit();

                        editor.putString("F1", f1);
                        editor.putString("F2", f2);
                        editor.commit();
                        Toast.makeText(getContext(),"saved", Toast.LENGTH_SHORT).show();

                    }
                });

        tbF1 = view.findViewById(R.id.tbF1);
        tbF2 = view.findViewById(R.id.tbF2);
        tbF1.setText(MainActivity.F1);
        tbF2.setText(MainActivity.F2);
        sharedPreferences = getContext().getSharedPreferences(MainActivity.APP_PREFERENCE, MODE_PRIVATE);
        return builder.create();
    }
}
