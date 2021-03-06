package emul.bluetooth;

import android.os.Message;

import java.util.ArrayList;

import emul.bluetooth.model.BLEConnectState;
import emul.bluetooth.model.BLEDisconnectState;
import emul.bluetooth.model.BLEScanState;
import emul.bluetooth.model.BLEServiceDiscoverState;
import emul.bluetooth.model.BLEState;
import emul.bluetooth.model.BLEStateException;
import emul.bluetooth.model.BLEWriteCharacteristicState;
import mocking.android.bluetooth.BluetoothGattCharacteristic;
import mocking.android.bluetooth.IBLEChangeCharacteristic;
import mocking.android.bluetooth.IBLEConnect;
import mocking.android.bluetooth.IBLEDisconnect;
import mocking.android.bluetooth.IBLEDiscoverService;
import mocking.android.bluetooth.IBLEScan;

/**
 * Created by khChoi on 2017-08-07.
 */

public class AutoBluetoothLE extends BluetoothLE {
    private ArrayList<BLEState> path;
    private int index;

    public void setPath(ArrayList<BLEState> path) {
        this.path = path;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int index() { return index; }

    public void incIndex() { index++; }

    @Override
    public void doBLEScan(IBLEScan iblescan) {
        if (path != null && index < path.size()) {
            BLEState state = path.get(index);
            if (state instanceof BLEScanState) {
                state.action(iblescan);

                index++;

                BLEState nextstate = path.get(index());
                nextstate.setupAction();

                return;
            }

            throw new BLEStateException("doBLEScan: " + state.getClass());
        }

        throw new BLEStateException("doBLEScan: path fails "
                + (path != null) + " "
                + (index() < path.size()) );
    }

    @Override
    public void doConnect(IBLEConnect ibleconnect) {
        if (path != null && index < path.size()) {
            BLEState state = path.get(index);
            if (state instanceof BLEConnectState) {
                state.action(ibleconnect);

                index++;
                if (index < path.size()) {

                    BLEState nextstate = path.get(index());
                    nextstate.setupAction();
                }
                return;
            }

            throw new BLEStateException("doConnect: " + state.getClass());
        }

        throw new BLEStateException("doConnect: path fails "
                + (path != null) + " "
                + (index() < path.size()) );
    }

    @Override
    public void doDiscoverService(IBLEDiscoverService ibleDiscoverService) {
        if (path != null && index < path.size()) {
            BLEState state = path.get(index);
            if (state instanceof BLEServiceDiscoverState) {
                state.action(ibleDiscoverService);

                index++;

                BLEState nextstate = path.get(index());
                nextstate.setupAction();

                return;
            }
            throw new BLEStateException("doDiscoverService: " + state.getClass());
        }

        throw new BLEStateException("doDiscoverService: path fails "
                + (path != null) + " "
                + (index() < path.size()) );
    }

    @Override
    public void doDisconnect(IBLEDisconnect ibleDisconnect) {
        if (path != null && index < path.size()) {
            BLEState state = path.get(index);
            if (state instanceof BLEDisconnectState) {
                state.action(ibleDisconnect);

//                index++;
//
//                BLEState nextstate = path.get(index());
//                nextstate.setupAction();

                return;
            }
            throw new BLEStateException("doDisconnect: " + state.getClass());
        }
        throw new BLEStateException("doDisconnect: path fails "
                + (path != null) + " "
                + (index() < path.size()) );
    }

    @Override
    public void doReadCharacteristic(Message msg) {
        super.doReadCharacteristic(msg);
    }

    @Override
    public void doWriteCharacteristic(BluetoothGattCharacteristic btGattCharacteristic, IBLEChangeCharacteristic ibleChangeCharacteristic) {
        if (path != null && index < path.size()) {
            BLEState state = path.get(index);
            if (state instanceof BLEWriteCharacteristicState) {
                //state.action();

                index++;

                BLEState nextstate = path.get(index());
                nextstate.setupAction();

                return;
            }

            throw new BLEStateException("doWriteCharacteristic: " + state.getClass());
        }

        throw new BLEStateException("doWriteCharacteristic: path fails "
                + (path != null) + " "
                + (index() < path.size()) );
    }

    @Override
    public void doNotification(IBLEChangeCharacteristic ibleChangeCharacteristic) {
        if (path != null && index < path.size()) {
            BLEState state = path.get(index);
            if (state instanceof BLEWriteCharacteristicState) {
                // state.action();

                index++;

                BLEState nextstate = path.get(index());
                nextstate.setupAction();

                return;
            }

            throw new BLEStateException("doNotification: " + state.getClass());
        }

        throw new BLEStateException("doNotification: path fails "
                + (path != null) + " "
                + (index() < path.size()) );
    }
}
