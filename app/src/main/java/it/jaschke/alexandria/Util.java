package it.jaschke.alexandria;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.widget.Toast;

/**
 * Created by An on 9/10/2015.
 */
public class Util {
    private static Util util;
    private static final String LOG_TAG = "UTILITY CLASS";

    public static synchronized Util instatnce() {
        if (util == null)
            util = new Util();
        return util;
    }

    public static void displayToast (Context context, CharSequence msg) {
        Toast noConToast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        noConToast.setGravity(Gravity.TOP | Gravity.CENTER, 0, 0);
        noConToast.show();
    }

    public static void displayNeutralAlert(Context context, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // Title and message
        builder.setMessage(message)
                .setTitle(title);
        // OK button
        builder.setNeutralButton(R.string.ok_alert, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static String isbn10ToIsbn13Converse (String isbn10) {
        int temp = 0, sum = 0, mul= 3, checkDigit;
        StringBuilder isbn13 = new StringBuilder("978");
        for (int i = 0; i < 9; i++) {
            temp = Character.getNumericValue(isbn10.charAt(i));
            sum = sum + temp*mul;
            if (mul == 3)
                mul = 1;
            else mul = 3;
        }
        sum = sum + (9*1) + (7*3) + (8*1);
        checkDigit = 10 - (sum%10);
        if (checkDigit == 10)
            checkDigit = 0;

        return isbn13
                .append(isbn10.substring(0,9))
                .append(String.valueOf(checkDigit))
                .toString();
    }
}
