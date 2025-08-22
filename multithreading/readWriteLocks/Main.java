package multithreading.readWriteLocks;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Main {
    public static void main(String[] args) {
        Counter counter = new Counter(0);
        Thread thread1 = new Thread(() -> {
            for(int i = 0; i < 5; i++){
                counter.increment();
            }
        });
        Thread thread2 = new Thread(() -> {
            for(int i = 0; i < 5; i++){
                counter.decrement();
            }
        });
        Thread thread3 = new Thread(() -> {
            for(int i = 0; i < 5; i++){
                System.out.println("Count: " + counter.getCount() + " Thread: " + Thread.currentThread().getName());
            }
        });
        thread1.start();
        thread2.start();
        thread3.start();
        try {
            thread1.join();
            thread2.join();
            thread3.join();
            System.out.println(counter.getCount());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static class Counter {
        private int count = 0;
        private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

        public Counter(int count){
            this.count = count;
        }

        public void increment() {
            lock.writeLock().lock();
            try{
                count++;
            }finally{
                lock.writeLock().unlock();
            }
        }

        public void decrement() {
            lock.writeLock().lock();
            try{
                count--;
            }finally{
                lock.writeLock().unlock();
            }
        }

        public int getCount() {
            lock.readLock().lock();
            try{
                return count;
            }finally{
                lock.readLock().unlock();
            }
        }
    }
}
