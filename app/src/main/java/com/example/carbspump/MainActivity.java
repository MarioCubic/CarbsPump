package com.example.carbspump;


import android.annotation.SuppressLint;
import android.content.Context;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.carbspump.xdrip.XdripSender;

import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {


    public TreeMap<String, Double> dishes = new TreeMap<>();
    public int counter = 1;
    Double total = 0.0;
    Double roundedTotal = 0.0;
    private Receiver receiver;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch xDripSwitch;
    private SharedPreferences sharedPreferences;
    private static final String SWITCH_STATE_KEY = "switch_state";
    private Button xdrip;
    private PackageManager pm;
    LinearLayout dishesContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        XdripSender.registerAppWithXDrip(this);
        EditText carbs_per_100 = findViewById(R.id.carbs_per_100);
        EditText weight = findViewById(R.id.weight);
        Button oneMore = findViewById(R.id.oneMore);
        Button clear = findViewById(R.id.clear_button);
        Button exitButton = findViewById(R.id.exit_button);
        xdrip = findViewById(R.id.xdrip_button);
        pm = getPackageManager();
        xDripSwitch = findViewById(R.id.switch2);
        sharedPreferences = getSharedPreferences("CarbsPumpPreferences", Context.MODE_PRIVATE);
        boolean savedSwitchState = sharedPreferences.getBoolean(SWITCH_STATE_KEY, false);
        xDripSwitch.setChecked(savedSwitchState);
        dishesContainer = findViewById(R.id.dishesContainer);


        oneMore.setOnClickListener(v -> oneMoreClick(carbs_per_100, weight));
        clear.setOnClickListener(v -> clearAll());
        // Сразу установить начальное состояние (если оно уже было сохранено ранее)
        boolean savedState = sharedPreferences.getBoolean(SWITCH_STATE_KEY, false);
        xDripSwitch.setChecked(savedState); // отразим в UI
        updateXDripState(savedState);       // и сразу применим поведение

// Обработка изменений
        xDripSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateXDripState(isChecked);
        });



        exitButton.setOnClickListener(v -> {
            new AlertDialog.Builder(MainActivity.this)
                    .setMessage("Вы уверены, что хотите выйти?")
                    .setCancelable(false)
                    .setPositiveButton("Да", (dialog, id) -> {
                        finishAffinity();  // Закрыть все активности приложения
                        System.exit(0);    // Завершить приложение
                    })
                    .setNegativeButton("Нет", (dialog, id) -> dialog.dismiss())  // Закрыть диалог
                    .show();
        });


    }

    private void oneMoreClick(EditText carbs_per_100, EditText weight) {



        TextView result = findViewById(R.id.textViewTotal);


        try {
            Double carbs = Double.parseDouble(carbs_per_100.getText().toString());
            Double weight_per_dish = Double.parseDouble(weight.getText().toString());
            String dish = "Блюдо " + counter;
            Double carbon = carbs * (weight_per_dish / 100);
            Double roundedCarbon = Math.round(carbon * 100.0) / 100.0;
            dishes.put(dish, roundedCarbon);


                TextView dishText = new TextView(this);
            dishText.setText(dish + ": " + roundedCarbon + " г.");
                dishText.setTextSize(18);
                dishText.setPadding(8, 16, 8, 16);
                //dishText.setBackground(ContextCompat.getDrawable(this, R.drawable.dish_item_background)); // если хочешь фон
                dishesContainer.addView(dishText);

            Toast.makeText(this, "Записал!", Toast.LENGTH_SHORT).show();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Введите корректное число", Toast.LENGTH_SHORT).show();
        }




        total = dishes.values().stream().mapToDouble(Double::doubleValue).sum();
        roundedTotal = Math.round(total * 100.0) / 100.0;
        result.setText("Всего: " + roundedTotal + "\nуглеводов");

        if (dishes.size() > 6) {
            new AlertDialog.Builder(MainActivity.this)
                    .setMessage("Куда столько? Ты это съешь?!")
                    .setCancelable(false)
                    .setPositiveButton("Да", (dialog, id) -> dialog.dismiss())
                    .setNegativeButton("Нет", (dialog, id) ->
                    {
                        clearAll();
                    })
                    .show();
        }

        carbs_per_100.setText("");
        weight.setText("");
        hideKeyboard();
        counter++;
    }

    private void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null && getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    private void clearAll() {

        new AlertDialog.Builder(MainActivity.this)
                .setMessage("Очистить всё?")
                .setCancelable(false)
                .setPositiveButton("Да", (dialog, id) -> {
                    dishes.clear();
                    counter = 1;
                    total = 0.0;
                    roundedTotal = 0.0;
                    dishesContainer.removeAllViews();

                    TextView result = findViewById(R.id.textViewTotal);
                    result.setText("");
                })
                .setNegativeButton("Нет", (dialog, id) -> dialog.dismiss())
                .show();
    }
    private void updateXDripState(boolean isChecked) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SWITCH_STATE_KEY, isChecked);
        editor.apply();

        if (isChecked) {
            if (XdripSender.isXDripInstalled(pm)) {
                xdrip.setOnClickListener(v -> {
                    DialogWindow.show(MainActivity.this, roundedTotal, (double insulin, double carbs, long timeInMillis) -> {
                        try {
                            Log.d("Main", "Инсулин: " + insulin + ", Углеводы: " + carbs + ", Время: " + timeInMillis);
                            XdripSender.sendToXDrip(MainActivity.this, insulin, carbs, timeInMillis);
                        } catch (Exception e) {
                            Log.e("MainActivity", "Ошибка при отправке данных в XDrip: " + e.getMessage());
                            e.printStackTrace();
                        }
                    });
                });
                Toast.makeText(MainActivity.this, "xDrip подключен", Toast.LENGTH_SHORT).show();
            } else {
                xdrip.setOnClickListener(null);
                xDripSwitch.setChecked(false);
                Toast.makeText(MainActivity.this, "xDrip не найден", Toast.LENGTH_SHORT).show();
            }
        } else {
            xdrip.setOnClickListener(null);
            Toast.makeText(MainActivity.this, "xDrip не подключен", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    @Override
    protected void onPause() {
        super.onPause();

        // Останавливаем или отменяем ресиверы, если они больше не нужны
        if (receiver != null) {
            try {
                // Отменяем регистрацию ресивера
                unregisterReceiver(receiver);
            } catch (IllegalArgumentException e) {
                // Игнорируем ошибку, если ресивер уже был отменен
                Log.d("MainActivity", "Receiver already unregistered or not registered.");
            }
        }


    }
    @Override
    protected void onStop() {
        super.onStop();

        // Отменяем регистрацию ресивера
        if (receiver != null) {
            try {
                unregisterReceiver(receiver);
            } catch (IllegalArgumentException e) {
                // Ресивер может быть уже отписан, игнорируем
            }
        }
    }


}

