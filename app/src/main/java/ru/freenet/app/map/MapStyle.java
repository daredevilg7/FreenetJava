package ru.freenet.app.map;

/** Настройки вида карты храним отдельно, чтобы легко заменить источник позже. */
public final class MapStyle {

    // Этот стиль содержит дорожную подложку и подписи на карте Уфы.
    public static final String URL = "https://tiles.openfreemap.org/styles/liberty";

    // Утилитный класс не нужно создавать через new.
    private MapStyle() {
    }
}
