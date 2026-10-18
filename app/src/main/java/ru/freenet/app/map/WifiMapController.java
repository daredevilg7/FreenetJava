package ru.freenet.app.map;

import android.content.Context;

import org.maplibre.android.annotations.Marker;
import org.maplibre.android.annotations.MarkerOptions;
import org.maplibre.android.camera.CameraUpdateFactory;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.maps.MapLibreMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.freenet.app.data.AppPreferences;
import ru.freenet.app.model.WifiPoint;
import ru.freenet.app.R;

/** Показывает Wi-Fi-маркеры и сообщает экрану, когда пользователь выбрал точку. */
public class WifiMapController {

    private final Context context;
    private final MapLibreMap map;
    private final Map<Integer, Marker> markersByNumber = new HashMap<>();
    private final Map<Long, WifiPoint> pointsByMarkerId = new HashMap<>();
    private final AppPreferences preferences;
    private PointSelectedListener pointSelectedListener;
    private Marker userLocationMarker;

    /** Запоминает карту и включает обработку нажатий на маркеры. */
    public WifiMapController(Context context, MapLibreMap map) {
        this.context = context;
        this.map = map;
        preferences = new AppPreferences(context);

        // При нажатии на маркер находим соответствующую запись и передаём её Activity.
        map.setOnMarkerClickListener(marker -> {
            WifiPoint point = pointsByMarkerId.get(marker.getId());
            if (point != null && pointSelectedListener != null) {
                pointSelectedListener.onPointSelected(point);
            }
            return false;
        });

        // После перемещения карты сохраняем её центр, чтобы восстановить при следующем запуске.
        map.addOnCameraIdleListener(() -> {
            LatLng center = map.getCameraPosition().target;
            preferences.saveCamera(center.getLatitude(), center.getLongitude(), map.getZoom());
        });
    }

    /** Создаёт по одному маркеру для каждой записи Wi-Fi-точки. */
    public void showPoints(List<WifiPoint> points) {
        // Сначала удаляем старые маркеры, чтобы после обновления не было копий.
        for (Marker marker : markersByNumber.values()) {
            map.removeMarker(marker);
        }
        markersByNumber.clear();
        pointsByMarkerId.clear();

        // Добавляем маркеры в том же порядке, что точки в исходных данных.
        for (WifiPoint point : points) {
            Marker marker = map.addMarker(new MarkerOptions()
                    .position(new LatLng(point.getLatitude(), point.getLongitude()))
                    .title(context.getString(R.string.wifi_point_number, point.getNumber()))
                    .snippet(formatCoordinates(point)));
            markersByNumber.put(point.getNumber(), marker);
            pointsByMarkerId.put(marker.getId(), point);
        }
    }

    /** Перемещает карту к точке и открывает её маркер. */
    public void focus(WifiPoint point, double zoom) {
        // Новое положение камеры ставит выбранную Wi-Fi-точку в центр экрана.
        map.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                        new LatLng(point.getLatitude(), point.getLongitude()),
                        zoom
                ),
                450
        );

        // После перемещения показываем информационную подпись маркера.
        Marker marker = markersByNumber.get(point.getNumber());
        if (marker != null) {
            map.selectMarker(marker);
        }
    }

    /** Показывает отдельный маркер пользователя и переносит камеру к нему. */
    public void showUserLocation(double latitude, double longitude) {
        LatLng position = new LatLng(latitude, longitude);
        if (userLocationMarker != null) {
            map.removeMarker(userLocationMarker);
        }
        userLocationMarker = map.addMarker(new MarkerOptions()
                .position(position)
                .title(context.getString(R.string.my_location)));
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15), 450);
    }

    /** Переносит карту к координатам без выбора Wi-Fi-маркера. */
    public void centerOn(double latitude, double longitude, double zoom) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(latitude, longitude), zoom), 450);
    }

    /** Устанавливает обработчик, который обновит карточку выбранной точки. */
    public void setPointSelectedListener(PointSelectedListener listener) {
        pointSelectedListener = listener;
    }

    /** Возвращает координаты в привычном для пользователя виде. */
    private String formatCoordinates(WifiPoint point) {
        return String.format(
                java.util.Locale.getDefault(),
                "%.6f, %.6f",
                point.getLatitude(),
                point.getLongitude()
        );
    }

    /** Этот интерфейс передаёт выбранную точку обратно на экран приложения. */
    public interface PointSelectedListener {
        void onPointSelected(WifiPoint point);
    }
}
