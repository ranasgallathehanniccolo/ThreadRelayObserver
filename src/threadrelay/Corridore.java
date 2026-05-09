package threadrelay;

import java.util.ArrayList;
import java.util.List;

public class Corridore implements Runnable {

    private final int id;
    private final int speed;
    private final Corridore prossimoCorridore;

    private boolean paused  = false;
    private boolean stopped = false;
    private boolean canStart = false;
    private boolean finished = false;
    private int count = 0;

    private final List<CorridoreObserver> observers = new ArrayList<>();

    public Corridore(int id, int speed, Corridore prossimoCorridore) {
        this.id = id;
        this.speed = speed;
        this.prossimoCorridore = prossimoCorridore;
    }

    public synchronized void addObserver(CorridoreObserver o) {
        if (!observers.contains(o)) {
            observers.add(o);
        }
    }

    public synchronized void removeObserver(CorridoreObserver o) {
        observers.remove(o);
    }

    private synchronized void notifyAggiornamento(int id, int count) {
        List<CorridoreObserver> copia = new ArrayList<>(observers);
        for (CorridoreObserver o : copia) {
            o.onAggiornamento(id, count);
        }
    }

    private synchronized void notifyFine(int id) {
        List<CorridoreObserver> copia = new ArrayList<>(observers);
        for (CorridoreObserver o : copia) {
            o.onFine(id);
        }
    }
    
    public synchronized void allowStart() {
        canStart = true;
        notifyAll();
    }

    public synchronized boolean isFinished() {
        return finished;
    }

    public synchronized void pause() {
        paused = true;
    }

    public synchronized void resume() {
        paused = false;
        notifyAll();
    }

    public synchronized void stop() {
        stopped = true;
        paused  = false;
        notifyAll();
    }

    @Override
    public void run() {
        synchronized (this) {
            while (!canStart) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }

        for (count = 0; count <= 99; count++) {

            notifyAggiornamento(id, count);

            synchronized (this) {
                while (paused) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                if (stopped) {
                    return;
                }
            }
            if (count == 90 && prossimoCorridore != null) {
                prossimoCorridore.allowStart();
            }
            synchronized (this) {
                try {
                    wait(speed);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
        synchronized (this) {
            finished = true;
            notifyAll();
        }
        notifyFine(id);
    }
}