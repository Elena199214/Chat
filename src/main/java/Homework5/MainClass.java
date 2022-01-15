package Homework5;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

/**
 * Организуем гонки:
 * Все участники должны стартовать одновременно, несмотря на то, что на подготовку у каждого из них уходит разное время.
 * В туннель не может заехать одновременно больше половины участников (условность).
 * Попробуйте всё это синхронизировать.
 * Первый участник, пересекший финишную черту, объявляется победителем (в момент пересечения этой самой черты).
 * Победитель должен быть только один (ситуация с 0 или 2+ победителями недопустима).
 * Только после того как все завершат гонку, нужно выдать объявление об окончании.
 * Можете корректировать классы (в т.ч. конструктор машин) и добавлять объекты классов из пакета util.concurrent.
 */

public class MainClass {
    public static final int CARS_COUNT = 4;
    public static void main(String[] args) {

        CountDownLatch start = new CountDownLatch(CARS_COUNT);
        CountDownLatch finish = new CountDownLatch(CARS_COUNT);
        CyclicBarrier barrier = new CyclicBarrier(CARS_COUNT);

        // трассы
        Race race = new Race(new Road(60), new Tunnel(), new Road(40));

        // участники
        Car[] cars = new Car[CARS_COUNT];
        for (int i = 0; i < cars.length; i++) {
            cars[i] = new Car(race, 20 + (int) (Math.random() * 10), barrier, start, finish); // одна трасса на всех участников, своя скорость
        }
        for (int i = 0; i < cars.length; i++) {
            new Thread(cars[i]).start();// для каждого участника создаем свой поток
        }
        try {
            System.out.println("ВАЖНОЕ ОБЪЯВЛЕНИЕ >>> Подготовка!!!");
            start.await(); // ждем начало гонки
            System.out.println("ВАЖНОЕ ОБЪЯВЛЕНИЕ >>> Гонка началась!!!");
            finish.await(); // ждем конца гонки
            System.out.println("ВАЖНОЕ ОБЪЯВЛЕНИЕ >>> Гонка закончилась!!!");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    }


