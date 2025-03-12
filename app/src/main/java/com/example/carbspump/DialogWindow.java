package com.example.carbspump;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.icu.util.Calendar;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class DialogWindow {

    public interface OnDialogConfirmed {
        void onConfirmed(double insulin, double carbs, long timeInMillis);
    }

    @SuppressLint("MissingInflatedId")
    public static void show(Context context, double defaultCarbsValue, OnDialogConfirmed callback) {
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.dialog_custom, null);
        dialog.setContentView(view);

        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
            View bottomSheetInternal = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheetInternal != null) {
                BottomSheetBehavior.from(bottomSheetInternal).setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        Switch pause = view.findViewById(R.id.switch1);
        EditText inputLong1 = view.findViewById(R.id.inputLong1);
        EditText inputLong2 = view.findViewById(R.id.inputLong2);

        inputLong2.setSelection(inputLong2.getText().length());
        TimePicker timePicker = view.findViewById(R.id.timePicker);
        DatePicker datePicker = view.findViewById(R.id.datePicker);
        NumberPicker pausePicker = view.findViewById(R.id.minutePicker);
        Button applyButton = view.findViewById(R.id.applyButton);
        Button cancelButton = view.findViewById(R.id.cancelButton);

        if(defaultCarbsValue != 0){
            inputLong2.setText(String.valueOf(defaultCarbsValue));
        }

        timePicker.setIs24HourView(true);

        SharedPreferences sharedPreferences = context.getSharedPreferences("PausePreferences", Context.MODE_PRIVATE);

        // Восстановление состояния Switch из SharedPreferences
        boolean isChecked = sharedPreferences.getBoolean("SWITCH_STATE_KEY", false);
        pause.setChecked(isChecked);

        // Настройка NumberPicker
        pausePicker.setMinValue(0);
        pausePicker.setMaxValue(isChecked ? 60 : 0);
        pausePicker.setWrapSelectorWheel(isChecked);

        pause.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Сохраняем состояние в SharedPreferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("SWITCH_STATE_KEY", isChecked);
                editor.apply();

                // Обновляем NumberPicker
                pausePicker.setMinValue(0);
                pausePicker.setMaxValue(isChecked ? 60 : 0);
                pausePicker.setWrapSelectorWheel(isChecked);
            }
        });

        applyButton.setOnClickListener(v -> {
            try {
                String insulinStr = inputLong1.getText().toString();
                String carbsStr = inputLong2.getText().toString();

                if (insulinStr.isEmpty()) insulinStr = "0";
                if (carbsStr.isEmpty()) carbsStr = "0";

                double insulin = Double.parseDouble(insulinStr);
                double carbs = Double.parseDouble(carbsStr);

                int day = datePicker.getDayOfMonth();
                int month = datePicker.getMonth(); // Важно! Нумерация месяцев с 0
                int year = datePicker.getYear();
                int hour = timePicker.getHour();
                int minute = timePicker.getMinute();

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, day);
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);

                long timeInMillis = calendar.getTimeInMillis();

                if (pause.isChecked()) {
                    int pauseMinute = pausePicker.getValue();
                    long pauseMinutesInMillis = pauseMinute * 60000L;

                    callback.onConfirmed(insulin, 0.0, timeInMillis);

                    // Используем Handler для задержки
                    new Handler().postDelayed(() -> {
                        callback.onConfirmed(0.0, carbs, timeInMillis + pauseMinutesInMillis);
                        dialog.dismiss();
                    }, 2000); // Задержка в 2 секунды
                } else {
                    callback.onConfirmed(insulin, carbs, timeInMillis);
                    dialog.dismiss();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(context, "Введите корректные числа", Toast.LENGTH_SHORT).show();
            }
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}