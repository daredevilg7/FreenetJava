package ru.freenet.app.model;

/** Одна Wi-Fi-точка с номером и координатами. */
public class WifiPoint {

    // Номер нужен, чтобы показать точку человеку в списке и на карточке.
    private final int number;

    // Широта и долгота показывают точное место точки на карте.
    private final double latitude;
    private final double longitude;

    /** Сохраняет номер и координаты новой точки. */
    public WifiPoint(int number, double latitude, double longitude) {
        this.number = number;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /** Возвращает порядковый номер точки. */
    public int getNumber() {
        return number;
    }

    /** Возвращает широту точки. */
    public double getLatitude() {
        return latitude;
    }

    /** Возвращает долготу точки. */
    public double getLongitude() {
        return longitude;
    }
}
