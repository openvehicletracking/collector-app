package com.openvehicletracking.collector;

import com.openvehicletracking.core.DeviceState;

import java.util.Observable;
import java.util.Observer;

public class DeviceStateManager implements Observer {

    public DeviceStateManager() {
        SessionManager.getInstance().addObserver(this);
    }

    @Override
    public void update(Observable o, Object arg) {
        if (!(arg instanceof DeviceState)) {
            return;
        }

        DeviceState deviceState = (DeviceState) arg;
    }
}
