package ru.freenet.app.location;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

/** Получает одно местоположение телефона средствами Android. */
public final class LocationHelper {

    private LocationHelper() { }

    /** Начинает поиск координат; перед вызовом Activity должна получить разрешение. */
    @SuppressWarnings("deprecation")
    public static void requestCurrentLocation(Activity activity, LocationCallback callback) {
        // Проверяем разрешение ещё раз, чтобы не обращаться к геолокации без согласия.
        boolean hasFine = activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        boolean hasCoarse = activity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        if (!hasFine && !hasCoarse) {
            callback.onError("Разрешение на местоположение не выдано.");
            return;
        }

        LocationManager manager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        if (manager == null) {
            callback.onError("Служба местоположения недоступна.");
            return;
        }

        // Один и тот же слушатель принимает координаты GPS или сетевого определения.
        final boolean[] finished = {false};
        LocationListener listener = new LocationListener() {
            @Override public void onLocationChanged(Location location) {
                if (!finished[0]) {
                    finished[0] = true;
                    callback.onLocation(location);
                }
            }
            @Override public void onStatusChanged(String provider, int status, Bundle extras) { }
            @Override public void onProviderEnabled(String provider) { }
            @Override public void onProviderDisabled(String provider) { }
        };

        // Через 20 секунд прекращаем ожидание, если телефон не нашёл координаты.
        Handler handler = new Handler(Looper.getMainLooper());
        Runnable timeout = () -> {
            if (!finished[0]) {
                finished[0] = true;
                manager.removeUpdates(listener);
                callback.onError("Не удалось определить местоположение. Проверьте GPS.");
            }
        };
        handler.postDelayed(timeout, 20_000);

        try {
            // Сначала используем уже известную позицию, если телефон её недавно сохранял.
            Location lastKnown = null;
            for (String provider : new String[]{LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER}) {
                try {
                    Location candidate = manager.getLastKnownLocation(provider);
                    if (candidate != null && (lastKnown == null
                            || candidate.getTime() > lastKnown.getTime())) {
                        lastKnown = candidate;
                    }
                } catch (SecurityException | IllegalArgumentException ignored) {
                    // На некоторых устройствах один из способов определения не поддерживается.
                }
            }
            if (lastKnown != null && System.currentTimeMillis() - lastKnown.getTime() < 120_000) {
                finished[0] = true;
                handler.removeCallbacks(timeout);
                callback.onLocation(lastKnown);
                return;
            }

            // Запускаем одноразовый запрос у каждого разрешённого и включённого провайдера.
            boolean requested = false;
            for (String provider : new String[]{LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER}) {
                try {
                    if (manager.isProviderEnabled(provider)) {
                        manager.requestSingleUpdate(provider, listener, Looper.getMainLooper());
                        requested = true;
                    }
                } catch (SecurityException | IllegalArgumentException ignored) {
                    // Если конкретный провайдер запрещён или отсутствует, пробуем следующий.
                }
            }
            if (!requested) {
                finished[0] = true;
                handler.removeCallbacks(timeout);
                callback.onError("Включите геолокацию в настройках телефона.");
            }
        } catch (SecurityException error) {
            finished[0] = true;
            handler.removeCallbacks(timeout);
            callback.onError("Android не разрешил получить местоположение.");
        }
    }

    /** Небольшой обратный вызов возвращает результат на экран приложения. */
    public interface LocationCallback {
        void onLocation(Location location);
        void onError(String message);
    }
}
