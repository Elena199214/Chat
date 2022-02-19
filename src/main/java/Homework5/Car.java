package Homework5;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Car implements Runnable {
    private static int CARS_COUNT = 0;
    private static final Lock lock = new ReentrantLock();
    private static boolean isFirstFinish = true;
    private final CyclicBarrier barrier;
    private final CountDownLatch start;
    private final CountDownLatch finish;

    private final Race race;
    private final int speed;
    private final String name;

    public String getName() {
        return name;
    }

    public int getSpeed() {
        return speed;
    }

    public Car(Race race, int speed, CyclicBarrier barrier, CountDownLatch start, CountDownLatch finish) {
        this.barrier = barrier;
        this.start = start;
        this.finish = finish;
        this.race = race;
        this.speed = speed;
        CARS_COUNT++;
        this.name = "Участник #" + CARS_COUNT;

    }

    @Override
    public void run() {

        try {
            // подготовка к заеду
            System.out.println(this.name + " готовится");
            Thread.sleep(500 + (int) (Math.random() * 800)); // получение времени для подготовки
            System.out.println(this.name + " готов");
            barrier.await();
            start.countDown();
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (int i = 0; i < race.getStages().size(); i++) {
            race.getStages().get(i).go(this); // получить текущую трассу и вызвать на этом участке метод go
        }
            // конец заезда
            finish.countDown();

            // победитель
            try {
                lock.lock();
                if (isFirstFinish) {
                    isFirstFinish = false;
                    System.out.println(this.name + " WIN");
                }
            } finally {
                lock.unlock();
            }
        }

    }


