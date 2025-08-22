package multithreading.locks;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    public static void main(String[] args) {

    }
}

class Counter {
    private int count = 0;
    private final Lock lock = new ReentrantLock(); // ReentrantLock is a thread-safe lock that can be used to synchronize access to a shared resource
    // fair lock is a lock that is fair, meaning that the thread that has been waiting the longest will get the lock first
    private final Lock fairLock = new ReentrantLock(true);  

    public Counter(int count){
        this.count = count;
    }

    public void increment() {
        try {
            if(lock.tryLock(1000, TimeUnit.SECONDS)){
                try{
                    count++;
                }catch(Exception e){
                    Thread.currentThread().interrupt();
                }finally{
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void decrement() {
        try {
            if(lock.tryLock(1000, TimeUnit.SECONDS)){
                try{
                    count--;
                }catch(Exception e){
                    Thread.currentThread().interrupt();
                }finally{
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    public int getCount(){
        return count;
    }
}
