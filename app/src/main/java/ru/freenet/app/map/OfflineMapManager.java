package ru.freenet.app.map;

import android.content.Context;

import org.maplibre.android.geometry.LatLngBounds;
import org.maplibre.android.offline.OfflineManager;
import org.maplibre.android.offline.OfflineRegion;
import org.maplibre.android.offline.OfflineRegionError;
import org.maplibre.android.offline.OfflineRegionStatus;
import org.maplibre.android.offline.OfflineTilePyramidRegionDefinition;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

import ru.freenet.app.data.AppPreferences;
import ru.freenet.app.data.WifiData;

/** Загружает область Уфы один раз и сохраняет её для показа без интернета. */
public class OfflineMapManager {

    // Скачиваем уровни карты, разрешённые учебной версией приложения.
    public static final double MIN_ZOOM = 10.0;
    public static final double MAX_ZOOM = 15.0;

    // Не приближаемся вплотную к лимитам OpenFreeMap, чтобы оставить запас на служебные данные.
    private static final long MAX_TILE_COUNT = 3000;
    private static final long SAFE_TILE_COUNT = 2800;
    private static final long MAX_RESOURCE_BYTES = 45L * 1024L * 1024L;
    private static final double MAP_EDGE_MARGIN_DEGREES = 0.01;
    private static final int PIXEL_RATIO = 1;
    private static final String REGION_NAME = "FreeNet Ufa Wi-Fi map";

    private final OfflineManager offlineManager;
    private final AppPreferences preferences;

    /** Подключает MapLibre и локальное хранилище настроек. */
    public OfflineMapManager(Context context) {
        offlineManager = OfflineManager.getInstance(context.getApplicationContext());
        preferences = new AppPreferences(context);

        // Просим MapLibre остановить загрузку при достижении лимита числа тайлов.
        offlineManager.setOfflineMapboxTileCountLimit(MAX_TILE_COUNT);
    }

    /** Проверяет, что ранее сохранённая карта полностью скачана. */
    public void checkReady(ReadyCallback callback) {
        long savedRegionId = preferences.getOfflineRegionId();
        if (savedRegionId < 0) {
            callback.onNotReady();
            return;
        }

        offlineManager.getOfflineRegion(savedRegionId, new OfflineManager.GetOfflineRegionCallback() {
            @Override
            public void onRegion(OfflineRegion region) {
                region.getStatus(new OfflineRegion.OfflineRegionStatusCallback() {
                    @Override
                    public void onStatus(OfflineRegionStatus status) {
                        if (status.isComplete()) {
                            callback.onReady();
                        } else {
                            callback.onNotReady();
                        }
                    }

                    @Override
                    public void onError(String message) {
                        callback.onError("Не удалось проверить сохранённую карту.");
                    }
                });
            }

            @Override
            public void onRegionNotFound() {
                preferences.clearOfflineRegionId();
                callback.onNotReady();
            }

            @Override
            public void onError(String message) {
                callback.onError("Не удалось открыть сохранённую карту.");
            }
        });
    }

    /** Начинает или продолжает загрузку карты Уфы и сообщает экрану о прогрессе. */
    public void downloadUfaMap(ProgressCallback progressCallback, CompletionCallback completionCallback) {
        long savedRegionId = preferences.getOfflineRegionId();

        // Если загрузка уже начиналась, продолжаем тот же регион, а не создаём копию.
        if (savedRegionId >= 0) {
            offlineManager.getOfflineRegion(savedRegionId, new OfflineManager.GetOfflineRegionCallback() {
                @Override
                public void onRegion(OfflineRegion region) {
                    watchRegion(region, progressCallback, completionCallback);
                    region.setDownloadState(OfflineRegion.STATE_ACTIVE);
                }

                @Override
                public void onRegionNotFound() {
                    preferences.clearOfflineRegionId();
                    createNewRegion(progressCallback, completionCallback);
                }

                @Override
                public void onError(String message) {
                    completionCallback.onError("Не удалось открыть незавершённую загрузку.");
                }
            });
            return;
        }

        // При первом запуске создаём прямоугольник вокруг всех точек Wi-Fi.
        createNewRegion(progressCallback, completionCallback);
    }

    /** Создаёт новую офлайн-область вокруг точек и запускает её загрузку. */
    private void createNewRegion(ProgressCallback progressCallback, CompletionCallback completionCallback) {
        LatLngBounds bounds = WifiData.createUfaBounds(MAP_EDGE_MARGIN_DEGREES);
        long expectedTiles = countTiles(bounds, (int) MIN_ZOOM, (int) MAX_ZOOM);
        if (expectedTiles > SAFE_TILE_COUNT) {
            completionCallback.onError("Для офлайн-карты нужна область меньшего размера.");
            return;
        }

        OfflineTilePyramidRegionDefinition regionDefinition = new OfflineTilePyramidRegionDefinition(
                MapStyle.URL,
                bounds,
                MIN_ZOOM,
                MAX_ZOOM,
                PIXEL_RATIO
        );

        // Сохраняем имя области в MapLibre, чтобы проще понять назначение файла карты.
        byte[] metadata = REGION_NAME.getBytes(StandardCharsets.UTF_8);
        offlineManager.createOfflineRegion(regionDefinition, metadata,
                new OfflineManager.CreateOfflineRegionCallback() {
                    @Override
                    public void onCreate(OfflineRegion region) {
                        preferences.setOfflineRegionId(region.getId());
                        watchRegion(region, progressCallback, completionCallback);
                        region.setDownloadState(OfflineRegion.STATE_ACTIVE);
                    }

                    @Override
                    public void onError(String message) {
                        completionCallback.onError("Не удалось подготовить офлайн-карту.");
                    }
                });
    }

    /** Следит за загрузкой, обновляет прогресс и сохраняет только готовую карту. */
    private void watchRegion(
            OfflineRegion region,
            ProgressCallback progressCallback,
            CompletionCallback completionCallback
    ) {
        AtomicBoolean hasFinished = new AtomicBoolean(false);

        region.setObserver(new OfflineRegion.OfflineRegionObserver() {
            @Override
            public void onStatusChanged(OfflineRegionStatus status) {
                if (hasFinished.get()) {
                    return;
                }

                // Останавливаем загрузку заранее, чтобы не выйти за условия сервиса карт.
                if (status.getCompletedTileCount() > SAFE_TILE_COUNT
                        || status.getCompletedResourceSize() > MAX_RESOURCE_BYTES) {
                    if (hasFinished.compareAndSet(false, true)) {
                        region.setDownloadState(OfflineRegion.STATE_INACTIVE);
                        region.delete(null);
                        preferences.clearOfflineRegionId();
                        completionCallback.onError("Карта больше допустимого размера. Попробуйте загрузить меньшую область.");
                    }
                    return;
                }

                int percent = 0;
                if (status.isRequiredResourceCountPrecise() && status.getRequiredResourceCount() > 0) {
                    percent = (int) Math.min(100,
                            status.getCompletedResourceCount() * 100 / status.getRequiredResourceCount());
                }
                progressCallback.onProgress(
                        percent,
                        status.getCompletedTileCount(),
                        status.getCompletedResourceSize()
                );

                // Готовность MapLibre означает, что все ресурсы области записаны на устройство.
                if (status.isComplete() && hasFinished.compareAndSet(false, true)) {
                    region.setDownloadState(OfflineRegion.STATE_INACTIVE);
                    preferences.setOfflineRegionId(region.getId());
                    completionCallback.onComplete();
                }
            }

            @Override
            public void onError(OfflineRegionError error) {
                if (hasFinished.compareAndSet(false, true)) {
                    region.setDownloadState(OfflineRegion.STATE_INACTIVE);
                    completionCallback.onError(readableError(error));
                }
            }

            @Override
            public void mapboxTileCountLimitExceeded(long limit) {
                if (hasFinished.compareAndSet(false, true)) {
                    region.setDownloadState(OfflineRegion.STATE_INACTIVE);
                    region.delete(null);
                    preferences.clearOfflineRegionId();
                    completionCallback.onError("Карта превысила лимит тайлов. Уменьшите область загрузки.");
                }
            }
        });
    }

    /** Считает примерное число тайлов, чтобы не начинать слишком большую загрузку. */
    private long countTiles(LatLngBounds bounds, int minZoom, int maxZoom) {
        long tileCount = 0;

        // На каждом уровне увеличения площадь делится на квадратные тайлы.
        for (int zoom = minZoom; zoom <= maxZoom; zoom++) {
            long tileSide = 1L << zoom;
            long firstX = longitudeToTile(bounds.getLonWest(), tileSide);
            long lastX = longitudeToTile(bounds.getLonEast(), tileSide);
            long firstY = latitudeToTile(bounds.getLatNorth(), tileSide);
            long lastY = latitudeToTile(bounds.getLatSouth(), tileSide);
            tileCount += (lastX - firstX + 1) * (lastY - firstY + 1);
        }

        return tileCount;
    }

    /** Преобразует долготу в номер столбца тайла Web Mercator. */
    private long longitudeToTile(double longitude, long tileSide) {
        double x = (longitude + 180.0) / 360.0 * tileSide;
        return Math.max(0, Math.min(tileSide - 1, (long) Math.floor(x)));
    }

    /** Преобразует широту в номер строки тайла Web Mercator. */
    private long latitudeToTile(double latitude, long tileSide) {
        double limitedLatitude = Math.max(-85.0511, Math.min(85.0511, latitude));
        double radians = Math.toRadians(limitedLatitude);
        double mercator = Math.log(Math.tan(radians) + 1.0 / Math.cos(radians));
        double y = (1.0 - mercator / Math.PI) / 2.0 * tileSide;
        return Math.max(0, Math.min(tileSide - 1, (long) Math.floor(y)));
    }

    /** Превращает техническую ошибку загрузки в короткое сообщение по-русски. */
    private String readableError(OfflineRegionError error) {
        if (OfflineRegionError.REASON_CONNECTION.equals(error.getReason())) {
            return "Нет подключения к интернету. Подключитесь к сети и попробуйте снова.";
        }
        if (OfflineRegionError.REASON_SERVER.equals(error.getReason())) {
            return "Сервер карты временно недоступен. Попробуйте ещё раз позже.";
        }
        return "Не удалось загрузить карту. Проверьте свободное место и попробуйте снова.";
    }

    /** Получает ответ о том, сохранена ли готовая карта. */
    public interface ReadyCallback {
        void onReady();
        void onNotReady();
        void onError(String message);
    }

    /** Получает процент загрузки, число тайлов и число скачанных байтов. */
    public interface ProgressCallback {
        void onProgress(int percent, long completedTiles, long completedBytes);
    }

    /** Получает итог первичной загрузки карты. */
    public interface CompletionCallback {
        void onComplete();
        void onError(String message);
    }
}
