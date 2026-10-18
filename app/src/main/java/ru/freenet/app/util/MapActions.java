package ru.freenet.app.util;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import java.util.Locale;

import ru.freenet.app.R;
import ru.freenet.app.model.WifiPoint;

/** Простые действия Android: копирование координат и открытие внешнего маршрута. */
public final class MapActions {

    private MapActions() { }

    /** Кладёт широту и долготу выбранной точки в системный буфер обмена. */
    public static void copyCoordinates(Context context, WifiPoint point) {
        String coordinates = String.format(Locale.US, "%.6f, %.6f",
                point.getLatitude(), point.getLongitude());
        ClipboardManager clipboard = (ClipboardManager)
                context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText("Координаты Wi-Fi", coordinates));
            Toast.makeText(context, R.string.coordinates_copied, Toast.LENGTH_SHORT).show();
        }
    }

    /** Передаёт координаты точки приложению Яндекс Карт или другому обработчику ссылок. */
    public static void openRoute(Context context, WifiPoint point) {
        String destination = String.format(Locale.US, "%.6f,%.6f",
                point.getLatitude(), point.getLongitude());
        Uri routeUri = Uri.parse("https://yandex.ru/maps/?rtext=~" + destination + "&rtt=auto");
        Intent routeIntent = new Intent(Intent.ACTION_VIEW, routeUri);
        try {
            context.startActivity(routeIntent);
        } catch (ActivityNotFoundException error) {
            Toast.makeText(context, R.string.route_app_missing, Toast.LENGTH_LONG).show();
        }
    }
}
