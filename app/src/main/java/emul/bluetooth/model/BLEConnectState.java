package emul.bluetooth.model;

import com.h3.hrm3200.Log;

import java.util.ArrayList;

import mocking.android.bluetooth.IBLEConnect;

/**
 * Created by khChoi on 2017-08-05.
 */

public class BLEConnectState extends BLEState {
    private int succ_or_fail;
    private int state;

    public BLEConnectState(int succ_or_fail, int state) {
        this(null, succ_or_fail, state);
    }

    public BLEConnectState(ArrayList<BLEState> next, int succ_or_fail, int state) {
        this.next = next;
        this.succ_or_fail = succ_or_fail;
        this.state = state;
    }

    @Override
    public void setupAction() {
        Log.v("[ HRM3200 ]", "BLEConnectState - Select one of the scanned bluetooth devices ...");
    }

    @Override
    public void action(IBLEConnect ibleconnect) {
        // Condition: This action should be called in doConnect.

        // Return succ_or_fail and state
        ibleconnect.setConnectResult(succ_or_fail, state);
    }
}
