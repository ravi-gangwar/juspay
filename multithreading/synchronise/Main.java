package multithreading.synchronise;

public class Main {
    public static void main(String[] args) {
        Counter counter = new Counter(0);
        MyThread myThread = new MyThread("MyThread", counter);
        MyThread myThread2 = new MyThread("MyThread2", counter);
        myThread.start();
        myThread2.start();
        try {
            myThread.join();
            myThread2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Counter value: " + counter.getCount());
    }
}

class Counter {
    private int count;
    
    public Counter(int initialValue) {
        this.count = initialValue;
    }
    
    // public synchronized void increment() {
    //     count++;
    // }

    public void increment() {
        synchronized (this) {
            count++;
        }
    }
    
    public int getCount() {
        return count;
    }
}

class MyThread extends Thread{
    private Counter counter;
    public MyThread(String name, Counter counter){
        super(name);
        this.counter = counter;
    }
    @Override
    public void run(){
        for(int i = 0; i < 1000; i++){
            counter.increment();
        }
        System.out.println("MyThread running" + this.getName() + " " + this.getId() + " " + counter.getCount());
    }
}   
