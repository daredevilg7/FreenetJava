package ru.freenet.app.data;

import android.content.Context;
import android.content.SharedPreferences;

/** Сохраняет небольшие настройки приложения в памяти телефона. */
public class AppPreferences {

    // Это имя используется Android для файла с настройками FreeNet.
    private static final String FILE_NAME = "freenet_settings";
    private static final String KEY_REGION_ID = "offline_region_id";
    private static final String KEY_MAP_LATITUDE = "map_latitude";
    private static final String KEY_MAP_LONGITUDE = "map_longitude";
    private static final String KEY_MAP_ZOOM = "map_zoom";

    private final SharedPreferences preferences;

    /** Открывает сохранённые настройки приложения. */
    public AppPreferences(Context context) {
        preferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    /** Возвращает номер сохранённого региона или -1, если карты ещё нет. */
    public long getOfflineRegionId() {
        return preferences.getLong(KEY_REGION_ID, -1L);
    }

    /** Сохраняет номер готовой офлайн-карты. */
    public void setOfflineRegionId(long regionId) {
        preferences.edit().putLong(KEY_REGION_ID, regionId).apply();
    }

    /** Удаляет номер карты, если MapLibre сообщил, что регион потерян. */
    public void clearOfflineRegionId() {
        preferences.edit().remove(KEY_REGION_ID).apply();
    }

    /** Сохраняет центр и увеличение карты перед закрытием приложения. */
    public void saveCamera(double latitude, double longitude, double zoom) {
        preferences.edit()
                .putFloat(KEY_MAP_LATITUDE, (float) latitude)
                .putFloat(KEY_MAP_LONGITUDE, (float) longitude)
                .putFloat(KEY_MAP_ZOOM, (float) zoom)
                .apply();
    }

    /** Возвращает сохранённую широту карты или центр Уфы при первом запуске. */
    public double getMapLatitude() {
        return preferences.getFloat(KEY_MAP_LATITUDE, 54.7431f);
    }

    /** Возвращает сохранённую долготу карты или центр Уфы при первом запуске. */
    public double getMapLongitude() {
        return preferences.getFloat(KEY_MAP_LONGITUDE, 55.9578f);
    }

    /** Возвращает сохранённый уровень увеличения или стандартное значение 12. */
    public double getMapZoom() {
        return preferences.getFloat(KEY_MAP_ZOOM, 12.0f);
    }
}
