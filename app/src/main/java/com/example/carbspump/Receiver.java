package com.example.carbspump;

import static androidx.core.content.ContextCompat.registerReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

public class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String function = intent.getStringExtra(Const.INTENT_FUNCTION_KEY);
            if (function == null) {
                return;
            }

            String replyMsg = intent.getStringExtra(Const.INTENT_REPLY_MSG);
            String replyCode = intent.getStringExtra(Const.INTENT_REPLY_CODE);

            Log.d("XDripResponse", "Ответ от xDrip: code=" + replyCode + ", msg=" + replyMsg);

            if (Const.INTENT_REPLY_CODE_OK.equals(replyCode)) {

                Toast.makeText(context, "Данные успешно добавлены в xDrip: " + replyMsg, Toast.LENGTH_SHORT).show();
            } else {
                String errorMessage = "Ошибка при добавлении данных в xDrip: " + replyMsg;
                if (Const.INTENT_REPLY_CODE_NOT_REGISTERED.equals(replyCode)) {
                    errorMessage = "Приложение не зарегистрировано в xDrip";
                } else if (Const.INTENT_REPLY_CODE_PACKAGE_ERROR.equals(replyCode)) {
                    errorMessage = "Ошибка packageKey";
                } else if (Const.CMD_START.equals(replyCode)) {
                    errorMessage = "Я был прав!";
                } else if (Const.INTENT_REPLY_CODE_ERROR.equals(replyCode)) {
                    errorMessage = "Ваще непонятновое!";
                }


                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();

            }
        }


    }

