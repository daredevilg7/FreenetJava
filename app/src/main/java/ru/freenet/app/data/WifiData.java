package ru.freenet.app.data;

import org.maplibre.android.geometry.LatLngBounds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.freenet.app.model.WifiPoint;

/** Данные бесплатных Wi-Fi-точек, перенесённые из веб-версии проекта. */
public final class WifiData {

    // Все 210 записей идут в том же порядке, что и в map.js; повторы сохранены.
    private static final List<WifiPoint> POINTS = createPoints();

    // Утилитный класс содержит только готовые данные и методы для их чтения.
    private WifiData() {
    }

    /** Возвращает список точек для показа на карте. */
    public static List<WifiPoint> getPoints() {
        // Запрещаем случайно удалить или заменить точки в другом месте программы.
        return Collections.unmodifiableList(POINTS);
    }

    /** Создаёт прямоугольник карты вокруг всех точек с запасом в градусах. */
    public static LatLngBounds createUfaBounds(double marginDegrees) {
        // Сначала ищем крайние координаты, чтобы ни одна точка не выпала из карты.
        double minLatitude = Double.MAX_VALUE;
        double maxLatitude = -Double.MAX_VALUE;
        double minLongitude = Double.MAX_VALUE;
        double maxLongitude = -Double.MAX_VALUE;

        for (WifiPoint point : POINTS) {
            minLatitude = Math.min(minLatitude, point.getLatitude());
            maxLatitude = Math.max(maxLatitude, point.getLatitude());
            minLongitude = Math.min(minLongitude, point.getLongitude());
            maxLongitude = Math.max(maxLongitude, point.getLongitude());
        }

        // Возвращаем границы: север, восток, юг, запад.
        return LatLngBounds.from(
                maxLatitude + marginDegrees,
                maxLongitude + marginDegrees,
                minLatitude - marginDegrees,
                minLongitude - marginDegrees
        );
    }

    /** Создаёт точки из координат, сохранённых в исходном JavaScript-файле. */
    private static List<WifiPoint> createPoints() {
        // Список с заранее заданным размером не тратит память на лишнее расширение.
        List<WifiPoint> points = new ArrayList<>(210);
        Collections.addAll(points,
                new WifiPoint(1, 54.732598, 55.945472),
                new WifiPoint(2, 54.713031, 55.953204),
                new WifiPoint(3, 54.718659, 55.954729),
                new WifiPoint(4, 54.720758, 55.928454),
                new WifiPoint(5, 54.719304, 55.944400),
                new WifiPoint(6, 54.740612, 55.949871),
                new WifiPoint(7, 54.740870, 55.950230),
                new WifiPoint(8, 54.805637, 56.035764),
                new WifiPoint(9, 54.805637, 56.035764),
                new WifiPoint(10, 54.805637, 56.035764),
                new WifiPoint(11, 54.806029, 56.036718),
                new WifiPoint(12, 54.805637, 56.035764),
                new WifiPoint(13, 54.703215, 55.830441),
                new WifiPoint(14, 54.775491, 56.022061),
                new WifiPoint(15, 54.736447, 55.972770),
                new WifiPoint(16, 54.737843, 55.971441),
                new WifiPoint(17, 54.772961, 56.059353),
                new WifiPoint(18, 54.773139, 56.059466),
                new WifiPoint(19, 54.773003, 56.059559),
                new WifiPoint(20, 54.773407, 56.062602),
                new WifiPoint(21, 54.773407, 56.062602),
                new WifiPoint(22, 54.772096, 56.058560),
                new WifiPoint(23, 54.716733, 55.924017),
                new WifiPoint(24, 54.714391, 55.935595),
                new WifiPoint(25, 54.716133, 55.927183),
                new WifiPoint(26, 54.714391, 55.935595),
                new WifiPoint(27, 54.714733, 55.934750),
                new WifiPoint(28, 54.714391, 55.935595),
                new WifiPoint(29, 54.716600, 55.925883),
                new WifiPoint(30, 54.714391, 55.935595),
                new WifiPoint(31, 54.714391, 55.935595),
                new WifiPoint(32, 54.714391, 55.935595),
                new WifiPoint(33, 54.714391, 55.935595),
                new WifiPoint(34, 54.715467, 55.928400),
                new WifiPoint(35, 54.714391, 55.935595),
                new WifiPoint(36, 54.714760, 55.933120),
                new WifiPoint(37, 54.717267, 55.922733),
                new WifiPoint(38, 54.714391, 55.935595),
                new WifiPoint(39, 54.714867, 55.931567),
                new WifiPoint(40, 54.714391, 55.935595),
                new WifiPoint(41, 54.714391, 55.935595),
                new WifiPoint(42, 54.714391, 55.935595),
                new WifiPoint(43, 54.778305, 56.130179),
                new WifiPoint(44, 54.721999, 56.013399),
                new WifiPoint(45, 54.697771, 55.987626),
                new WifiPoint(46, 54.777666, 56.078769),
                new WifiPoint(47, 54.768214, 56.081841),
                new WifiPoint(48, 54.751741, 55.996402),
                new WifiPoint(49, 54.774156, 56.072337),
                new WifiPoint(50, 54.777355, 56.124978),
                new WifiPoint(51, 54.764381, 56.067208),
                new WifiPoint(52, 54.774774, 56.127655),
                new WifiPoint(53, 54.719368, 55.989485),
                new WifiPoint(54, 54.763794, 56.048056),
                new WifiPoint(55, 54.771175, 56.068375),
                new WifiPoint(56, 54.753259, 55.990447),
                new WifiPoint(57, 54.769347, 56.073954),
                new WifiPoint(58, 54.763586, 56.059859),
                new WifiPoint(59, 54.711879, 55.998621),
                new WifiPoint(60, 54.774592, 56.067702),
                new WifiPoint(61, 54.693681, 55.991183),
                new WifiPoint(62, 54.768999, 56.070819),
                new WifiPoint(63, 54.768214, 56.081841),
                new WifiPoint(64, 54.776051, 56.077772),
                new WifiPoint(65, 54.723054, 55.992019),
                new WifiPoint(66, 54.705044, 55.983556),
                new WifiPoint(67, 54.777298, 56.130386),
                new WifiPoint(68, 54.777355, 56.124978),
                new WifiPoint(69, 54.782838, 56.137986),
                new WifiPoint(70, 54.778918, 56.130494),
                new WifiPoint(71, 54.780891, 56.114719),
                new WifiPoint(72, 54.747361, 56.022831),
                new WifiPoint(73, 54.702916, 55.996025),
                new WifiPoint(74, 54.763332, 56.068492),
                new WifiPoint(75, 54.771834, 56.076586),
                new WifiPoint(76, 54.760709, 56.050786),
                new WifiPoint(77, 54.767763, 56.061324),
                new WifiPoint(78, 54.77641, 56.075158),
                new WifiPoint(79, 54.761, 56.067621),
                new WifiPoint(80, 54.770728, 56.077547),
                new WifiPoint(81, 54.767461, 56.049924),
                new WifiPoint(82, 54.773958, 56.083871),
                new WifiPoint(83, 54.778429, 56.074161),
                new WifiPoint(84, 54.774364, 56.079002),
                new WifiPoint(85, 54.719331, 56.009105),
                new WifiPoint(86, 54.823885, 56.079973),
                new WifiPoint(87, 54.707156, 56.006257),
                new WifiPoint(88, 54.704872, 56.006868),
                new WifiPoint(89, 54.707957, 56.00331),
                new WifiPoint(90, 54.726003, 56.006904),
                new WifiPoint(91, 54.766236, 56.016516),
                new WifiPoint(92, 54.750224, 56.005179),
                new WifiPoint(93, 54.696017, 56.005242),
                new WifiPoint(94, 54.698577, 56.00623),
                new WifiPoint(95, 54.699847, 55.994453),
                new WifiPoint(96, 54.703821, 55.998352),
                new WifiPoint(97, 54.764049, 56.056068),
                new WifiPoint(98, 54.703114, 55.992576),
                new WifiPoint(99, 54.721999, 56.013399),
                new WifiPoint(100, 54.710693, 55.995172),
                new WifiPoint(101, 54.776944, 56.120999),
                new WifiPoint(102, 54.701335, 55.996681),
                new WifiPoint(103, 54.766848, 56.064477),
                new WifiPoint(104, 54.768827, 56.053814),
                new WifiPoint(105, 54.76595, 56.077457),
                new WifiPoint(106, 54.773252, 56.069301),
                new WifiPoint(107, 54.705533, 56.007407),
                new WifiPoint(108, 54.674606, 55.917857),
                new WifiPoint(109, 54.674606, 55.917857),
                new WifiPoint(110, 54.674606, 55.917857),
                new WifiPoint(111, 54.705242, 55.825098),
                new WifiPoint(112, 54.725348, 55.997098),
                new WifiPoint(113, 54.725348, 55.997098),
                new WifiPoint(114, 54.725348, 55.997098),
                new WifiPoint(115, 54.725348, 55.997098),
                new WifiPoint(116, 54.675507, 55.918516),
                new WifiPoint(117, 54.771538, 56.081868),
                new WifiPoint(118, 54.757269, 56.000103),
                new WifiPoint(119, 54.735315, 55.98146),
                new WifiPoint(120, 54.741416, 56.026918),
                new WifiPoint(121, 54.564866, 55.884307),
                new WifiPoint(122, 54.569852, 55.901747),
                new WifiPoint(123, 54.714588, 55.963371),
                new WifiPoint(124, 54.717444, 55.954065),
                new WifiPoint(125, 54.815218, 56.067297),
                new WifiPoint(126, 54.712149, 56.006203),
                new WifiPoint(127, 54.70224, 55.832613),
                new WifiPoint(128, 54.788564, 56.131141),
                new WifiPoint(129, 54.787671, 56.123532),
                new WifiPoint(130, 54.785169, 56.040366),
                new WifiPoint(131, 54.820955, 56.107138),
                new WifiPoint(132, 54.780288, 56.021025),
                new WifiPoint(133, 54.735163, 55.967836),
                new WifiPoint(134, 54.726158, 55.949187),
                new WifiPoint(135, 54.738604, 55.997651),
                new WifiPoint(136, 54.817578, 56.087042),
                new WifiPoint(137, 54.733047, 55.962015),
                new WifiPoint(138, 54.70816, 55.994552),
                new WifiPoint(139, 54.729985, 55.946681),
                new WifiPoint(140, 54.758168, 56.011404),
                new WifiPoint(141, 54.711156, 56.002017),
                new WifiPoint(142, 54.731482, 55.938982),
                new WifiPoint(143, 54.73117, 55.937958),
                new WifiPoint(144, 54.708305, 56.007245),
                new WifiPoint(145, 54.721052, 55.928795),
                new WifiPoint(146, 54.711385, 55.993851),
                new WifiPoint(147, 54.712846, 55.826845),
                new WifiPoint(148, 54.752001, 56.02878),
                new WifiPoint(149, 54.72293, 55.946267),
                new WifiPoint(150, 54.761638, 56.022265),
                new WifiPoint(151, 54.724921, 55.924892),
                new WifiPoint(152, 54.814362, 56.087402),
                new WifiPoint(153, 54.780288, 56.021025),
                new WifiPoint(154, 54.710215, 55.994309),
                new WifiPoint(155, 54.740667, 55.96868),
                new WifiPoint(156, 54.742554, 55.959239),
                new WifiPoint(157, 54.815218, 56.067297),
                new WifiPoint(158, 54.71863, 55.959159),
                new WifiPoint(159, 54.733026, 55.951172),
                new WifiPoint(160, 54.745906, 55.993204),
                new WifiPoint(161, 54.716871, 56.00976),
                new WifiPoint(162, 54.7186, 55.9406),
                new WifiPoint(163, 54.813858, 56.073047),
                new WifiPoint(164, 54.721349, 55.952511),
                new WifiPoint(165, 54.781218, 56.030422),
                new WifiPoint(166, 54.770162, 56.059599),
                new WifiPoint(167, 54.670259, 55.912518),
                new WifiPoint(168, 54.711582, 55.8382),
                new WifiPoint(169, 54.724931, 55.942297),
                new WifiPoint(170, 54.777412, 56.031557),
                new WifiPoint(171, 54.776315, 56.031276),
                new WifiPoint(172, 54.74076, 55.967517),
                new WifiPoint(173, 54.724931, 55.942297),
                new WifiPoint(174, 54.767879, 56.015935),
                new WifiPoint(175, 54.672195, 55.918114),
                new WifiPoint(176, 54.70389, 55.842912),
                new WifiPoint(177, 54.766734, 56.05287),
                new WifiPoint(178, 54.791755, 56.140616),
                new WifiPoint(179, 54.76394, 56.011575),
                new WifiPoint(180, 54.716642, 56.00745),
                new WifiPoint(181, 54.733317, 55.934086),
                new WifiPoint(182, 54.753233, 56.011582),
                new WifiPoint(183, 54.704113, 55.836341),
                new WifiPoint(184, 54.697631, 55.982095),
                new WifiPoint(185, 54.722930, 55.946267),
                new WifiPoint(186, 54.734513, 55.956347),
                new WifiPoint(187, 54.733832, 55.959841),
                new WifiPoint(188, 54.733832, 55.959841),
                new WifiPoint(189, 54.735719, 55.959473),
                new WifiPoint(190, 54.738058, 55.956329),
                new WifiPoint(191, 54.736944, 55.959280),
                new WifiPoint(192, 54.738942, 55.960173),
                new WifiPoint(193, 54.740501, 55.959293),
                new WifiPoint(194, 54.751237, 55.986135),
                new WifiPoint(195, 54.758924, 55.994829),
                new WifiPoint(196, 54.766100, 56.019893),
                new WifiPoint(197, 54.779598, 56.114567),
                new WifiPoint(198, 54.779598, 56.114567),
                new WifiPoint(199, 54.781311, 56.126245),
                new WifiPoint(200, 54.821484, 56.058790),
                new WifiPoint(201, 54.822246, 56.059770),
                new WifiPoint(202, 54.818942, 56.071906),
                new WifiPoint(203, 54.817697, 56.072831),
                new WifiPoint(204, 54.804394, 56.116651),
                new WifiPoint(205, 54.804394, 56.116651),
                new WifiPoint(206, 54.738838, 55.955143),
                new WifiPoint(207, 54.622777, 56.110144),
                new WifiPoint(208, 54.761231, 55.994685),
                new WifiPoint(209, 54.697635, 55.852914),
                new WifiPoint(210, 54.687572, 55.996959)
        );
        return points;
    }
}
