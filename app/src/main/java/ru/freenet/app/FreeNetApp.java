package ru.freenet.app;

import android.app.Application;

import org.maplibre.android.MapLibre;
import org.maplibre.android.module.http.HttpRequestUtil;

import java.util.concurrent.TimeUnit;

import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;

/** Настраивает общие для всего приложения запросы карты. */
public class FreeNetApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // MapLibre должен запуститься первым: сетевой модуль сразу получает его настройки.
        MapLibre.getInstance(this);

        // Не разрешаем библиотеке открыть больше четырёх сетевых запросов одновременно.
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(4);
        dispatcher.setMaxRequestsPerHost(4);

        // Добавляем понятное имя приложения в каждый запрос к картографическому серверу.
        OkHttpClient mapHttpClient = new OkHttpClient.Builder()
                .dispatcher(dispatcher)
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(chain -> chain.proceed(
                        chain.request().newBuilder()
                                .header("User-Agent", "FreeNet/1.0 (ru.freenet.app)")
                                .build()
                ))
                .build();
        HttpRequestUtil.setOkHttpClient(mapHttpClient);

    }
}
