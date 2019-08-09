package gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.engine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.activities.Activity;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.activities.ActivityThread;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.time.Time;

public class SimulationEngine {

    Time currentSimulationTime;
    PendingEventQueue pendingEventQueue;
    Map<Activity<?>, ActivityThread> activityToThreadMap;
    Thread engineThread;
    // private final Lock lock = new ReentrantLock();
    private final Object monitor = new Object();

    public void simulate() {
        engineThread = Thread.currentThread();

        while (!pendingEventQueue.isEmpty()) {
            ActivityThread thread = pendingEventQueue.remove();
            System.out.println("Dequeued activity thread: " + thread.toString());

            System.out.println("Advancing from T=" + currentSimulationTime.toString() + " to T=" + thread.getEventTime());
            currentSimulationTime = thread.getEventTime();

            this.dispatchContext(thread);
            thread.execute();
            try {
                this.suspend();
                // thread.join();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            System.out.println("====== EFFECT MODEL COMPLETE OR PAUSED ======");
        }
    }

    public SimulationEngine(Time simulationStartTime, List<ActivityThread> activityThreads) {
        currentSimulationTime = simulationStartTime;
        pendingEventQueue = new PendingEventQueue();
        activityToThreadMap = new HashMap<>();

        for (ActivityThread thread: activityThreads) {
            pendingEventQueue.add(thread);
            activityToThreadMap.put(thread.getActivity(), thread);
        }
    }

    public void dispatchContext(ActivityThread activityThread) {
        // TODO: see if we need to detach from this later with resumes
        SimulationContext ctx = new SimulationContext(this, activityThread);
        activityThread.setContext(ctx);
    }

    public void resume() {
        synchronized (monitor) {
            monitor.notify();
        }
    }

    public void suspend() throws InterruptedException {
        synchronized (monitor) {
            monitor.wait();
        }
    }

    public Time getCurrentSimulationTime() {
        return this.currentSimulationTime;
    }

}