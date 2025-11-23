package com.example.platemate;

import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ToastUtils {
    
    public enum ToastType {
        SUCCESS,
        ERROR,
        INFO,
        WARNING
    }
    
    /**
     * Show a stylish neubrutal toast message
     * @param context The context
     * @param message The message to display
     * @param type The type of toast (SUCCESS, ERROR, INFO, WARNING)
     */
    public static void showToast(Context context, String message, ToastType type) {
        if (context == null || message == null || message.isEmpty()) {
            return;
        }
        
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.custom_toast, null);
        
        LinearLayout toastLayout = layout.findViewById(R.id.toastLayout);
        ImageView icon = layout.findViewById(R.id.toastIcon);
        TextView text = layout.findViewById(R.id.toastText);
        
        text.setText(message);
        
        // Set background and icon based on type
        int backgroundRes;
        int iconRes;
        ColorFilter iconColorFilter;
        
        switch (type) {
            case SUCCESS:
                backgroundRes = R.drawable.toast_background_success;
                iconRes = android.R.drawable.ic_dialog_info;
                iconColorFilter = new PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
                break;
            case ERROR:
                backgroundRes = R.drawable.toast_background_error;
                iconRes = android.R.drawable.ic_dialog_alert;
                iconColorFilter = new PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
                break;
            case WARNING:
                backgroundRes = R.drawable.toast_background_info;
                iconRes = android.R.drawable.ic_dialog_info;
                iconColorFilter = new PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
                break;
            case INFO:
            default:
                backgroundRes = R.drawable.toast_background;
                iconRes = android.R.drawable.ic_dialog_info;
                iconColorFilter = new PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
                break;
        }
        
        toastLayout.setBackgroundResource(backgroundRes);
        icon.setImageResource(iconRes);
        icon.setColorFilter(iconColorFilter);
        
        Toast toast = new Toast(context);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 100);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
    
    /**
     * Show a success toast
     */
    public static void showSuccess(Context context, String message) {
        showToast(context, message, ToastType.SUCCESS);
    }
    
    /**
     * Show an error toast
     */
    public static void showError(Context context, String message) {
        showToast(context, message, ToastType.ERROR);
    }
    
    /**
     * Show an info toast
     */
    public static void showInfo(Context context, String message) {
        showToast(context, message, ToastType.INFO);
    }
    
    /**
     * Show a warning toast
     */
    public static void showWarning(Context context, String message) {
        showToast(context, message, ToastType.WARNING);
    }
}

