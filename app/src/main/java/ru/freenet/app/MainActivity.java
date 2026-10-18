package ru.freenet.app;

import android.app.Activity;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.location.Location;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.maplibre.android.camera.CameraUpdateFactory;
import org.maplibre.android.camera.CameraPosition;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.MapView;

import java.util.Locale;

import ru.freenet.app.data.AppPreferences;
import ru.freenet.app.data.WifiData;
import ru.freenet.app.map.MapStyle;
import ru.freenet.app.map.OfflineMapManager;
import ru.freenet.app.map.WifiMapController;
import ru.freenet.app.location.LocationHelper;
import ru.freenet.app.model.WifiPoint;
import ru.freenet.app.util.DistanceCalculator;
import ru.freenet.app.util.MapActions;

/** Показывает карту Wi-Fi-точек и управляет первым скачиванием офлайн-области. */
public class MainActivity extends Activity {

    private OfflineMapManager offlineMapManager;
    private AppPreferences preferences;
    private MapView mapView;
    private View offlinePanel;
    private ProgressBar progressBar;
    private TextView offlineTitle;
    private TextView offlineDescription;
    private TextView progressText;
    private Button retryButton;
    private TextView wifiCount;
    private TextView statusText;
    private View pointCard;
    private TextView pointTitle;
    private TextView pointCoordinates;
    private TextView pointDistance;
    private Button copyButton;
    private Button routeButton;
    private WifiMapController wifiMapController;
    private WifiPoint selectedPoint;
    private boolean mapSetupStarted;
    private static final int LOCATION_PERMISSION_REQUEST = 41;
    private double userLatitude = Double.NaN;
    private double userLongitude = Double.NaN;
    private boolean findNearestAfterLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Загружаем готовую разметку, чтобы экран было легко изучать в Android Studio.
        setContentView(R.layout.activity_main);
        findScreenViews();

        // Создаём помощников для карты и настроек телефона.
        offlineMapManager = new OfflineMapManager(this);
        preferences = new AppPreferences(this);
        mapView.onCreate(savedInstanceState);
        connectScreenButtons();

        // При старте проверяем сохранённую карту и скачиваем её только при необходимости.
        showLoadingScreen(getString(R.string.offline_downloading));
        offlineMapManager.checkReady(new OfflineMapManager.ReadyCallback() {
            @Override
            public void onReady() {
                openSavedMap();
            }

            @Override
            public void onNotReady() {
                startFirstDownload();
            }

            @Override
            public void onError(String message) {
                showError(message);
            }
        });
    }

    /** Находит элементы разметки один раз, чтобы обработчики могли ими пользоваться. */
    private void findScreenViews() {
        mapView = findViewById(R.id.mapView);
        offlinePanel = findViewById(R.id.offlinePanel);
        progressBar = findViewById(R.id.offlineProgressBar);
        offlineTitle = findViewById(R.id.offlineTitle);
        offlineDescription = findViewById(R.id.offlineDescription);
        progressText = findViewById(R.id.offlineProgressText);
        retryButton = findViewById(R.id.offlineRetryButton);
        wifiCount = findViewById(R.id.wifiCount);
        statusText = findViewById(R.id.statusText);
        pointCard = findViewById(R.id.pointCard);
        pointTitle = findViewById(R.id.pointTitle);
        pointCoordinates = findViewById(R.id.pointCoordinates);
        pointDistance = findViewById(R.id.pointDistance);
        copyButton = findViewById(R.id.copyCoordinatesButton);
        routeButton = findViewById(R.id.openRouteButton);
    }

    /** Подключает кнопки карты к геолокации, точкам и действиям Android. */
    private void connectScreenButtons() {
        retryButton.setOnClickListener(view -> startFirstDownload());

        findViewById(R.id.locationButton).setOnClickListener(view -> requestLocation(false));
        findViewById(R.id.nearestButton).setOnClickListener(view -> {
            if (Double.isFinite(userLatitude) && Double.isFinite(userLongitude)) {
                findNearestPoint();
            } else {
                requestLocation(true);
            }
        });

        // Обновляем список локальных точек, не обращаясь к интернету.
        findViewById(R.id.refreshButton).setOnClickListener(view -> {
            if (wifiMapController != null) {
                wifiMapController.showPoints(WifiData.getPoints());
                showStatus(getString(R.string.points_refreshed));
            }
        });

        copyButton.setOnClickListener(view -> {
            if (selectedPoint != null) MapActions.copyCoordinates(this, selectedPoint);
        });
        routeButton.setOnClickListener(view -> {
            if (selectedPoint != null) MapActions.openRoute(this, selectedPoint);
        });
    }

    /** Начинает или продолжает загрузку, если телефон подключён к сети. */
    private void startFirstDownload() {
        if (!hasNetworkConnection()) {
            showError(getString(R.string.offline_no_network));
            return;
        }

        showLoadingScreen(getString(R.string.offline_downloading));
        offlineMapManager.downloadUfaMap(
                new OfflineMapManager.ProgressCallback() {
                    @Override
                    public void onProgress(int percent, long completedTiles, long completedBytes) {
                        // Показываем, сколько тайлов и мегабайтов уже сохранено.
                        String downloadedSize = String.format(
                                Locale.getDefault(),
                                "%.1f МБ",
                                completedBytes / 1024.0 / 1024.0
                        );
                        progressText.setText(getString(
                                R.string.offline_progress,
                                completedTiles,
                                downloadedSize
                        ));

                        // Если MapLibre знает общий объём загрузки, показываем точный процент.
                        if (percent > 0 && percent < 100) {
                            progressBar.setIndeterminate(false);
                            progressBar.setProgress(percent);
                        } else {
                            progressBar.setIndeterminate(true);
                        }
                    }
                },
                new OfflineMapManager.CompletionCallback() {
                    @Override
                    public void onComplete() {
                        openSavedMap();
                    }

                    @Override
                    public void onError(String message) {
                        showError(message);
                    }
                }
        );
    }

    /** Показывает объяснение, прогресс загрузки и кнопку повтора при ошибке. */
    private void showLoadingScreen(String message) {
        mapView.setVisibility(View.GONE);
        pointCard.setVisibility(View.GONE);
        offlinePanel.setVisibility(View.VISIBLE);
        offlineTitle.setText(R.string.app_name);
        offlineDescription.setText(R.string.offline_first_download);
        progressText.setText(message);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setIndeterminate(true);
        retryButton.setVisibility(View.GONE);
    }

    /** Показывает понятную ошибку и предлагает повторить загрузку. */
    private void showError(String message) {
        mapView.setVisibility(View.GONE);
        offlinePanel.setVisibility(View.VISIBLE);
        offlineTitle.setText(R.string.offline_problem);
        offlineDescription.setText(message);
        progressText.setText("");
        progressBar.setVisibility(View.GONE);
        retryButton.setVisibility(View.VISIBLE);
    }

    /** Открывает карту только после того, как офлайн-область полностью сохранена. */
    private void openSavedMap() {
        offlinePanel.setVisibility(View.GONE);
        mapView.setVisibility(View.VISIBLE);
        wifiCount.setText(getString(R.string.wifi_count));

        // Асинхронно получаем готовый объект карты и загружаем сохранённый стиль.
        if (!mapSetupStarted) {
            mapSetupStarted = true;
            mapView.getMapAsync(map -> map.setStyle(MapStyle.URL, style -> configureMap(map)));
        }
    }

    /** Настраивает камеру и маркеры после загрузки стиля карты. */
    private void configureMap(MapLibreMap map) {
        // Камера остаётся внутри скачанного региона и не уходит за его масштаб.
        map.setLatLngBoundsForCameraTarget(WifiData.createUfaBounds(0.01));
        map.setMinZoomPreference(OfflineMapManager.MIN_ZOOM);
        map.setMaxZoomPreference(OfflineMapManager.MAX_ZOOM);

        // Восстанавливаем позицию, а при повреждённых настройках используем центр Уфы.
        double latitude = preferences.getMapLatitude();
        double longitude = preferences.getMapLongitude();
        double zoom = preferences.getMapZoom();
        if (!Double.isFinite(latitude) || latitude < -85 || latitude > 85
                || !Double.isFinite(longitude) || longitude < -180 || longitude > 180) {
            latitude = 54.7431;
            longitude = 55.9578;
            zoom = 12;
        }
        zoom = Math.max(OfflineMapManager.MIN_ZOOM, Math.min(OfflineMapManager.MAX_ZOOM, zoom));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), zoom));

        // Добавляем Wi-Fi-точки и показываем карточку выбранного маркера.
        wifiMapController = new WifiMapController(this, map);
        wifiMapController.showPoints(WifiData.getPoints());
        wifiMapController.setPointSelectedListener(this::showPointCard);
    }

    /** Обновляет карточку после выбора маркера на карте. */
    private void showPointCard(WifiPoint point) {
        selectedPoint = point;
        pointTitle.setText(getString(R.string.wifi_point_number, point.getNumber()));
        pointCoordinates.setText(String.format(
                Locale.getDefault(),
                "%.6f, %.6f",
                point.getLatitude(),
                point.getLongitude()
        ));
        updatePointDistance();
        pointCard.setVisibility(View.VISIBLE);
    }

    /** Просит разрешение только после нажатия кнопки и затем получает координаты. */
    private void requestLocation(boolean findNearestAfterFix) {
        findNearestAfterLocation = findNearestAfterFix;
        boolean hasFine = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        boolean hasCoarse = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        if (!hasFine && !hasCoarse) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST);
            return;
        }
        loadLocation();
    }

    /** Вызывает помощник и сохраняет последнюю полученную позицию. */
    private void loadLocation() {
        LocationHelper.requestCurrentLocation(this, new LocationHelper.LocationCallback() {
            @Override public void onLocation(Location location) {
                userLatitude = location.getLatitude();
                userLongitude = location.getLongitude();
                if (wifiMapController != null) {
                    wifiMapController.showUserLocation(userLatitude, userLongitude);
                }
                updatePointDistance();
                if (findNearestAfterLocation) findNearestPoint();
                else showStatus(getString(R.string.location_found));
                findNearestAfterLocation = false;
            }

            @Override public void onError(String message) {
                findNearestAfterLocation = false;
                showStatus(message);
            }
        });
    }

    /** Android сообщает результат системного окна с разрешениями. */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != LOCATION_PERMISSION_REQUEST) return;
        boolean granted = false;
        for (int result : grantResults) {
            if (result == PackageManager.PERMISSION_GRANTED) granted = true;
        }
        if (granted) loadLocation();
        else {
            findNearestAfterLocation = false;
            showStatus(getString(R.string.location_permission_denied));
        }
    }

    /** Находит ближайшую запись из исходного списка и открывает её карточку. */
    private void findNearestPoint() {
        WifiPoint nearest = DistanceCalculator.findNearest(
                WifiData.getPoints(), userLatitude, userLongitude);
        if (nearest == null) {
            showStatus(getString(R.string.no_wifi_points));
            return;
        }
        if (wifiMapController != null) wifiMapController.focus(nearest, 16);
        showPointCard(nearest);
    }

    /** Показывает прямое расстояние до карточки, если координаты пользователя известны. */
    private void updatePointDistance() {
        if (selectedPoint == null) return;
        if (!Double.isFinite(userLatitude) || !Double.isFinite(userLongitude)) {
            pointDistance.setText(R.string.point_distance_unknown);
            return;
        }
        double km = DistanceCalculator.distanceKm(userLatitude, userLongitude,
                selectedPoint.getLatitude(), selectedPoint.getLongitude());
        pointDistance.setText(getString(R.string.point_distance_km, km));
    }

    /** Показывает короткую подсказку и скрывает её через несколько секунд. */
    private void showStatus(String message) {
        statusText.setText(message);
        statusText.setVisibility(View.VISIBLE);
        new Handler(Looper.getMainLooper()).postDelayed(
                () -> statusText.setVisibility(View.GONE),
                2500
        );
    }

    /** Проверяет, сообщил ли Android о доступном интернете или мобильной сети. */
    private boolean hasNetworkConnection() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) {
            return false;
        }

        Network activeNetwork = manager.getActiveNetwork();
        if (activeNetwork == null) {
            return false;
        }

        NetworkCapabilities capabilities = manager.getNetworkCapabilities(activeNetwork);
        return capabilities != null
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        mapView.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
