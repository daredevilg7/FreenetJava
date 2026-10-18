package ru.freenet.app.util;

import java.util.List;

import ru.freenet.app.model.WifiPoint;

/** Считает расстояние по прямой между координатами на поверхности Земли. */
public final class DistanceCalculator {

    // Средний радиус Земли в километрах нужен формуле гаверсинусов.
    private static final double EARTH_RADIUS_KM = 6371.0;

    // Утилитный класс не нужно создавать через new.
    private DistanceCalculator() {
    }

    /** Возвращает расстояние в километрах между двумя широтой-долготой. */
    public static double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        // Разницы широты и долготы переводим из градусов в радианы.
        double latitudeDifference = Math.toRadians(lat2 - lat1);
        double longitudeDifference = Math.toRadians(lon2 - lon1);

        // Формула гаверсинусов находит расстояние по поверхности Земли.
        double firstPart = Math.sin(latitudeDifference / 2) * Math.sin(latitudeDifference / 2);
        double secondPart = Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(longitudeDifference / 2)
                * Math.sin(longitudeDifference / 2);
        double value = firstPart + secondPart;

        // Ограничиваем значение из-за возможной погрешности округления компьютера.
        value = Math.max(0.0, Math.min(1.0, value));
        double angle = 2 * Math.atan2(Math.sqrt(value), Math.sqrt(1 - value));

        // Умножаем угол на радиус Земли и возвращаем километры.
        return EARTH_RADIUS_KM * angle;
    }

    /** Находит ближайшую точку или возвращает null, если список пуст. */
    public static WifiPoint findNearest(List<WifiPoint> points, double latitude, double longitude) {
        // Пока нет подходящей точки, считаем, что ближайшего расстояния ещё нет.
        WifiPoint nearestPoint = null;
        double shortestDistance = Double.MAX_VALUE;

        // Сравниваем расстояние до каждой точки и запоминаем самую близкую.
        for (WifiPoint point : points) {
            double distance = distanceKm(
                    latitude,
                    longitude,
                    point.getLatitude(),
                    point.getLongitude()
            );

            if (distance < shortestDistance) {
                shortestDistance = distance;
                nearestPoint = point;
            }
        }

        return nearestPoint;
    }
}
