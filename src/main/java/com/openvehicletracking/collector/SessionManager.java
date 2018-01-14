package com.openvehicletracking.collector;

import com.openvehicletracking.collector.connection.ActiveDeviceConnection;

import java.util.Observable;
import java.util.concurrent.*;

public class SessionManager extends Observable {

    private final ConcurrentHashMap<String, ActiveDeviceConnection> activeDevices = new ConcurrentHashMap<>();
    // @TODO clean up this map with filter of isDone and isCancelled
    private final ConcurrentHashMap<String, ScheduledFuture> timeouts = new ConcurrentHashMap<>();
    private static final SessionManager instance = new SessionManager();

    public static SessionManager getInstance() {
        return instance;
    }

    public void updateSession(ActiveDeviceConnection activeDeviceConnection) {
        String deviceId = activeDeviceConnection.getDevice().getId();

        if (activeDeviceConnection.getDevice().getState() != null) {
            notifyObservers(activeDeviceConnection.getDevice().getState());
        }

        activeDevices.put(deviceId, activeDeviceConnection);

        if (timeouts.containsKey(deviceId)) {
            ScheduledFuture task = timeouts.get(deviceId);
            if (!task.isCancelled()) {
                task.cancel(true);
            }
            timeouts.remove(deviceId);
        }

        ScheduledFuture<?> futureTask = startTimerFor(activeDeviceConnection);
        timeouts.put(deviceId, futureTask);
    }

    public ActiveDeviceConnection getConnection(String deviceId) {
        return activeDevices.get(deviceId);
    }

    private ScheduledFuture<?> startTimerFor(ActiveDeviceConnection activeDeviceConnection) {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        return executor.schedule(() -> {
            activeDevices.remove(activeDeviceConnection.getDevice().getId());
        }, 10, TimeUnit.SECONDS);
    }


}
