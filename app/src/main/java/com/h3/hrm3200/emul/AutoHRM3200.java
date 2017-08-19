package com.h3.hrm3200.emul;

import com.h3.hrm3200.emul.model.AppTime_0x80_0x81_State;
import com.h3.hrm3200.emul.model.CANCEL_DownloadStoredData_0x82_0x01_0x83;
import com.h3.hrm3200.emul.model.DeviceTimeReplyState;
import com.h3.hrm3200.emul.model.DisconnectByApp;
import com.h3.hrm3200.emul.model.InitializeData;
import com.h3.hrm3200.emul.model.OK_0x11_State;
import com.h3.hrm3200.emul.model.OK_0x15_State;
import com.h3.hrm3200.emul.model.REQ_Disconnection_0x82_0x02_State;
import com.h3.hrm3200.emul.model.REQ_DownloadStoredData_0x82_0x03_0x83;
import com.h3.hrm3200.emul.model.REQ_RealtimeData_0x82_0x01;
import com.h3.hrm3200.emul.model.REQ_SessionInfo_0x84_0x85;
import com.h3.hrm3200.emul.model.RealtimeDataReply;
import com.h3.hrm3200.emul.model.SendEndOfSession;
import com.h3.hrm3200.emul.model.SendSessionCount;
import com.h3.hrm3200.emul.model.SendSessionInfo;
import com.h3.hrm3200.emul.model.ServiceDiscoverFollowedByDeviceTimeReply;
import com.h3.hrm3200.emul.model.SharedSessionInfo;
import com.h3.hrm3200.emul.model.StoredDataReply;
import com.h3.hrm3200.emul.scenario.Scenario_BLEScan_Connect_Discovery_DownloadData_DisconnectionByDevice;
import com.h3.hrm3200.emul.scenario.Scenario_BLEScan_Connect_Discovery_RealtimeDataByUI_DisconnectionByApp;
import com.h3.hrm3200.emul.scenario.Scenario_BLEScan_Connect_Discovery_RealtimeDataByUI_DisconnectionByDevice;
import com.h3.hrm3200.emul.scenario.Scenario_BLEScan_Connect_Discovery_RealtimeData_DisconnectionByApp;
import com.h3.hrm3200.emul.scenario.Scenario_BLEScan_Connect_Discovery_RealtimeData_DisconnectionByDevice;

import java.util.ArrayList;
import java.util.UUID;

import emul.bluetooth.AutoBluetoothLE;
import emul.bluetooth.model.BLEConnectState;
import emul.bluetooth.model.BLEDisconnectState;
import emul.bluetooth.model.BLEScanState;
import emul.bluetooth.model.BLEServiceDiscoverState;
import emul.bluetooth.model.BLEState;
import emul.bluetooth.model.BLEStateException;
import emul.bluetooth.model.util.VertexBLEState;
import mocking.android.bluetooth.BLEService;
import mocking.android.bluetooth.BluetoothGatt;
import mocking.android.bluetooth.BluetoothGattCharacteristic;
import mocking.android.bluetooth.BluetoothProfile;
import mocking.android.bluetooth.IBLEChangeCharacteristic;
import mocking.android.bluetooth.IBLEDisconnect;
import mocking.android.bluetooth.IBLEDiscoverService;

/**
 * Created by khChoi on 2017-08-07.
 */

public class AutoHRM3200 extends AutoBluetoothLE {
    ArrayList<BLEState> path;

    public AutoHRM3200() {
        path = new ArrayList<BLEState>();

        // new Scenario_BLEScan_Connect_Discovery_RealtimeData_DisconnectionByApp(this, path);
        new Scenario_BLEScan_Connect_Discovery_RealtimeData_DisconnectionByDevice(this, path);

        // BUG : 디바이스에 저장된 데이터를 다운받지 않는 선택을 할 때 sessionData 객체를 초기화하지 않아 NullReferenceException 발생
        // new Scenario_BLEScan_Connect_Discovery_RealtimeDataByUI_DisconnectionByApp(this, path);

        // BUG : 디바이스에 저장된 데이터를 다운받지 않는 선택을 할 때 sessionData 객체를 초기화하지 않아 NullReferenceException 발생
        // new Scenario_BLEScan_Connect_Discovery_RealtimeDataByUI_DisconnectionByDevice(this, path);

        // BUG??? : 디바이스에 저장된 데이터를 모두 다운받은 다음 앱에서 Disconnect 명령을 내리면
        //          디바이스에서 Disconnect 완료 신호를 받지 않고 종료
        // new Scenario_BLEScan_Connect_Discovery_DownloadData_DisconnectionByDevice(this, path);

        // Initialize a path for testing
        this.setPath(path);
        this.setIndex(0);
    }

    private void buildGraph() {
        // VERTEX:
        VertexBLEState vtx_BLEScan =
                new VertexBLEState("State0 BLEScan", new BLEScanState("00:11:22:AA:BB:CC", "HRM3200"));

        // VERTEX:
        VertexBLEState vtx_ConnectSuccess =
                new VertexBLEState("State0 BLEConnect", new BLEConnectState(BluetoothGatt.GATT_SUCCESS, BluetoothProfile.STATE_CONNECTED));
        VertexBLEState vtx_ConnectFail =
                new VertexBLEState("State0 BLEConnect", new BLEConnectState(BluetoothGatt.GATT_FAILURE, BluetoothProfile.STATE_CONNECTED));

        // EDGE: vtx_BLEScan ---> vtx_Connect
        vtx_BLEScan.getAdjacencyList().add(vtx_ConnectSuccess);
        vtx_BLEScan.getAdjacencyList().add(vtx_ConnectFail);

        // TODO: vtx_ConnectFail ---> Any final state?????

        // VERTEX:
        VertexBLEState vtx_DisoverService =
                new VertexBLEState("State0 ServiceDiscovery", new ServiceDiscoverFollowedByDeviceTimeReply());

        // EDGE: vtx_Connect ---> vtx_DiscoverService
        vtx_ConnectSuccess.getAdjacencyList().add(vtx_DisoverService);

        // VERTEX:
        VertexBLEState vtx_State0 = new VertexBLEState("State0", new DeviceTimeReplyState(this));

        // EDGE: vtx_DiscoverService ---> vtx_State0
        vtx_DisoverService.getAdjacencyList().add(vtx_State0);

        // VERTEX:
        VertexBLEState vtx_State1_0x11 = new VertexBLEState("State1 0x11", new OK_0x11_State(this));

        // EDGE: vtx_State0 ---> vtx_State1_0x11
        vtx_State0.getAdjacencyList().add(vtx_State1_0x11);

        // VERTEX:
        // 1) 0x01, 0x01
        // 2) 0x00, 0x01
        // 3) 0x00, 0x00
        VertexBLEState vtx_State1_0x80_0x81_0x01_0x01 = new VertexBLEState("State1 0x80 0x01_0x01", new AppTime_0x80_0x81_State(this, 0x01, 0x01));
        VertexBLEState vtx_State1_0x80_0x81_0x00_0x01 = new VertexBLEState("State1 0x80 0x00_0x01", new AppTime_0x80_0x81_State(this, 0x00, 0x01));
        VertexBLEState vtx_State1_0x80_0x81_0x00_0x00 = new VertexBLEState("State1 0x80 0x00_0x00", new AppTime_0x80_0x81_State(this, 0x00, 0x00));

        // EDGE: vtx_State1_0x11 ---> vtxState1_0x80_0x01_0x01   (Measure data in realtime)
        // EDGE: vtx_State1_0x11 ---> vtxState1_0x80_0x00_0x01   (
        // EDGE: vtx_State1_0x11 ---> vtxState1_0x80_0x00_0x00
        vtx_State1_0x11.getAdjacencyList().add(vtx_State1_0x80_0x81_0x01_0x01);
        vtx_State1_0x11.getAdjacencyList().add(vtx_State1_0x80_0x81_0x00_0x01);
        vtx_State1_0x11.getAdjacencyList().add(vtx_State1_0x80_0x81_0x00_0x00);

        // 1. Realtime Data Reply
        // TODO: SetNotification 명령어를 확인하는 상태 추가 필요

        // VERTEX:
        VertexBLEState vtx_State5_0x1A_1 = new VertexBLEState("State5 1", new RealtimeDataReply(this));

        VertexBLEState vtx_State5_0x1A_2 = new VertexBLEState("State5 2", new RealtimeDataReply(this));
        VertexBLEState vtx_State5_0x1A_3 = new VertexBLEState("State5 3", new RealtimeDataReply(this));
        VertexBLEState vtx_State5_0x1A_4 = new VertexBLEState("State5 4", new RealtimeDataReply(this));
        VertexBLEState vtx_State5_0x1A_5 = new VertexBLEState("State5 5", new RealtimeDataReply(this));

        // EDGE :
        vtx_State1_0x80_0x81_0x01_0x01.getAdjacencyList().add(vtx_State5_0x1A_1);  // 1st data after 0x81 0x01 0x01
        vtx_State1_0x80_0x81_0x00_0x01.getAdjacencyList().add(vtx_State5_0x1A_1);  // 1st data after 0x81 0x00 0x01

        vtx_State5_0x1A_1.getAdjacencyList().add(vtx_State5_0x1A_2);        // 2nd data
        vtx_State5_0x1A_2.getAdjacencyList().add(vtx_State5_0x1A_3);        // 3rd data
        vtx_State5_0x1A_3.getAdjacencyList().add(vtx_State5_0x1A_4);        // 4th data
        vtx_State5_0x1A_4.getAdjacencyList().add(vtx_State5_0x1A_5);        // 5th data

        // 2. Download Stored Data
        VertexBLEState vtx_State6_REQ_0x82_0x03_0x83 = new VertexBLEState("State6 REQ_0x82_0x03_0x83",
                new REQ_DownloadStoredData_0x82_0x03_0x83(this));

        // EDGE : vtx_State1_0x80_0x81_0x00_0x00 ---> vtx_State6_REQ_0x82_0x03_0x83
        vtx_State1_0x80_0x81_0x00_0x00.getAdjacencyList().add(vtx_State6_REQ_0x82_0x03_0x83);

        // Session count
        final int session_count = 2;

        // VERTEX :
        VertexBLEState vtx_State7_SendSessionCount = new VertexBLEState("State7 Session Count",
                new SendSessionCount(this, session_count));

        // EDGE : vtx_State6_REQ_0x82_0x03_0x83 ---> vtx_State7_SendSessionCount
        vtx_State6_REQ_0x82_0x03_0x83.getAdjacencyList().add(vtx_State7_SendSessionCount);

        SharedSessionInfo sharedSessionInfo = new SharedSessionInfo();

        // 1st Session //////////////////////////////////////////////////////////////////////////

        // VERTEX :
        VertexBLEState vtx_ReqSessionInfo_1 = new VertexBLEState("State8 Req Session Info",
                new REQ_SessionInfo_0x84_0x85(this, sharedSessionInfo));

        // EDGE : vtx_State7_SendSessionCount ---> vtx_ReqSessionInfo_1
        vtx_State7_SendSessionCount.getAdjacencyList().add(vtx_ReqSessionInfo_1);

        sharedSessionInfo.dataTotalCount = 10;

        // VERTEX
        VertexBLEState vtx_SendSessionInfo_1 = new VertexBLEState("State10 Send Session Info",
                new SendSessionInfo(this, sharedSessionInfo));

        // EDGE : vtx_ReqSessionInfo_1 ---> vtx_SendSessionInfo_1
        vtx_ReqSessionInfo_1.getAdjacencyList().add(vtx_SendSessionInfo_1);

        // VERTEX :
        VertexBLEState vtx_OK_0x15_State_1 = new VertexBLEState("State11 OK 0x15",
                new OK_0x15_State(this));

        // EDGE : vtx_SendSessionInfo_1 ---> vtx_OK_0x15_State_1
        vtx_SendSessionInfo_1.getAdjacencyList().add(vtx_OK_0x15_State_1);

        // VERTICES as many as dataTotalCount
        VertexBLEState vtx_StoredDataReply_1_1 = new VertexBLEState("State12 Stored Data Reply",
                new StoredDataReply(this, sharedSessionInfo));
        VertexBLEState vtx_StoredDataReply_1_2 = new VertexBLEState("State12 Stored Data Reply",
                new StoredDataReply(this, sharedSessionInfo));
        VertexBLEState vtx_StoredDataReply_1_3 = new VertexBLEState("State12 Stored Data Reply",
                new StoredDataReply(this, sharedSessionInfo));
        VertexBLEState vtx_StoredDataReply_1_4 = new VertexBLEState("State12 Stored Data Reply",
                new StoredDataReply(this, sharedSessionInfo));
        VertexBLEState vtx_StoredDataReply_1_5 = new VertexBLEState("State12 Stored Data Reply",
                new StoredDataReply(this, sharedSessionInfo));
        VertexBLEState vtx_StoredDataReply_1_6 = new VertexBLEState("State12 Stored Data Reply",
                new StoredDataReply(this, sharedSessionInfo));
        VertexBLEState vtx_StoredDataReply_1_7 = new VertexBLEState("State12 Stored Data Reply",
                new StoredDataReply(this, sharedSessionInfo));
        VertexBLEState vtx_StoredDataReply_1_8 = new VertexBLEState("State12 Stored Data Reply",
                new StoredDataReply(this, sharedSessionInfo));
        VertexBLEState vtx_StoredDataReply_1_9 = new VertexBLEState("State12 Stored Data Reply",
                new StoredDataReply(this, sharedSessionInfo));
        VertexBLEState vtx_StoredDataReply_1_10 = new VertexBLEState("State12 Stored Data Reply",
                new StoredDataReply(this, sharedSessionInfo));

        VertexBLEState vtx_SendEndofSession_1 = new VertexBLEState("State Send End of Session",
                new SendEndOfSession(this, sharedSessionInfo));

        // EDGES :
        vtx_OK_0x15_State_1.getAdjacencyList().add(vtx_StoredDataReply_1_1);

        vtx_StoredDataReply_1_1.getAdjacencyList().add(vtx_StoredDataReply_1_2);
        vtx_StoredDataReply_1_2.getAdjacencyList().add(vtx_StoredDataReply_1_3);
        vtx_StoredDataReply_1_3.getAdjacencyList().add(vtx_StoredDataReply_1_4);
        vtx_StoredDataReply_1_4.getAdjacencyList().add(vtx_StoredDataReply_1_5);
        vtx_StoredDataReply_1_5.getAdjacencyList().add(vtx_StoredDataReply_1_6);
        vtx_StoredDataReply_1_6.getAdjacencyList().add(vtx_StoredDataReply_1_7);
        vtx_StoredDataReply_1_7.getAdjacencyList().add(vtx_StoredDataReply_1_8);
        vtx_StoredDataReply_1_8.getAdjacencyList().add(vtx_StoredDataReply_1_9);
        vtx_StoredDataReply_1_9.getAdjacencyList().add(vtx_StoredDataReply_1_10);

        vtx_StoredDataReply_1_10.getAdjacencyList().add(vtx_SendEndofSession_1);

        // 2nd Session //////////////////////////////////////////////////////////////////////////

        // VERTEX :
        VertexBLEState vtx_ReqSessionInfo_2 = new VertexBLEState("State8 Req Session Info",
                new REQ_SessionInfo_0x84_0x85(this, sharedSessionInfo));

        // EDGE : vtx_SendEndofSession_1 ---> vtx_ReqSessionInfo_2
        vtx_SendEndofSession_1.getAdjacencyList().add(vtx_ReqSessionInfo_2);

        sharedSessionInfo.dataTotalCount = 7;

        // VERTEX
        VertexBLEState vtx_SendSessionInfo_2 = new VertexBLEState("State10 Send Session Info",
                new SendSessionInfo(this, sharedSessionInfo));

        // EDGE : vtx_ReqSessionInfo_2 ---> vtx_SendSessionInfo_2
        vtx_ReqSessionInfo_2.getAdjacencyList().add(vtx_SendSessionInfo_2);

        // VERTEX :
        VertexBLEState vtx_OK_0x15_State_2 = new VertexBLEState("State11 OK 0x15",
                new OK_0x15_State(this));

        // EDGE : vtx_SendSessionInfo_2 ---> vtx_OK_0x15_State_2
        vtx_SendSessionInfo_2.getAdjacencyList().add(vtx_OK_0x15_State_2);

        // VERTICES as many as dataTotalCount
        VertexBLEState vtx_StoredDataReply_2_1 = new VertexBLEState("State12 Stored Data Reply",
                new StoredDataReply(this, sharedSessionInfo));
        VertexBLEState vtx_StoredDataReply_2_2 = new VertexBLEState("State12 Stored Data Reply",
                new StoredDataReply(this, sharedSessionInfo));
        VertexBLEState vtx_StoredDataReply_2_3 = new VertexBLEState("State12 Stored Data Reply",
                new StoredDataReply(this, sharedSessionInfo));
        VertexBLEState vtx_StoredDataReply_2_4 = new VertexBLEState("State12 Stored Data Reply",
                new StoredDataReply(this, sharedSessionInfo));
        VertexBLEState vtx_StoredDataReply_2_5 = new VertexBLEState("State12 Stored Data Reply",
                new StoredDataReply(this, sharedSessionInfo));
        VertexBLEState vtx_StoredDataReply_2_6 = new VertexBLEState("State12 Stored Data Reply",
                new StoredDataReply(this, sharedSessionInfo));
        VertexBLEState vtx_StoredDataReply_2_7 = new VertexBLEState("State12 Stored Data Reply",
                new StoredDataReply(this, sharedSessionInfo));

        VertexBLEState vtx_SendEndofSession_2 = new VertexBLEState("State Send End of Session",
                new SendEndOfSession(this, sharedSessionInfo));

        // EDGES :
        vtx_OK_0x15_State_2.getAdjacencyList().add(vtx_StoredDataReply_2_1);

        vtx_StoredDataReply_2_1.getAdjacencyList().add(vtx_StoredDataReply_2_2);
        vtx_StoredDataReply_2_2.getAdjacencyList().add(vtx_StoredDataReply_2_3);
        vtx_StoredDataReply_2_3.getAdjacencyList().add(vtx_StoredDataReply_2_4);
        vtx_StoredDataReply_2_4.getAdjacencyList().add(vtx_StoredDataReply_2_5);
        vtx_StoredDataReply_2_5.getAdjacencyList().add(vtx_StoredDataReply_2_6);
        vtx_StoredDataReply_2_6.getAdjacencyList().add(vtx_StoredDataReply_2_7);

        vtx_StoredDataReply_2_7.getAdjacencyList().add(vtx_SendEndofSession_2);

        // End of All Sessions

        // 3. Cancel to download stored data and to continue to receive realtime data

        // VERTEX :
        VertexBLEState vtx_State4_CANCEL_0x82_0x01_0x83 = new VertexBLEState("State4 CANCEL_0x82_0x01_0x83",
                new CANCEL_DownloadStoredData_0x82_0x01_0x83(this));

        // EDGE : vtx_State4_CANCEL_0x82_0x01_0x83 ---> vtx_State5_0x1A
        vtx_State4_CANCEL_0x82_0x01_0x83.getAdjacencyList().add(vtx_State5_0x1A_1);

        // Disconnection
        //     by App
        VertexBLEState vtx_StateReqDisconnect_0x82_0x02 = new VertexBLEState("State REQ_Disconnection",
                new REQ_Disconnection_0x82_0x02_State(this));
        VertexBLEState vtx_StateDisconnectByApp = new VertexBLEState("State Disconnection by App",
                new DisconnectByApp(this, BluetoothGatt.GATT_SUCCESS, BluetoothProfile.STATE_DISCONNECTED));

        // EDGE : 1) Disconnection by App after measuring data in realtime
        //           vtx_State5_0x1A_5 ---> vtx_StateReqDisconnect_0x82_0x02
        //           vtx_StateReqDisconnect_0x82_0x02 ---> vtx_StateDisconnectByApp

        //        2) Discoonection by App after downloading stored data
        //           vtx_SendEndofSession_2 ---> vtx_StateDisconnectByApp

        vtx_State5_0x1A_5.getAdjacencyList().add(vtx_StateReqDisconnect_0x82_0x02);
        vtx_StateReqDisconnect_0x82_0x02.getAdjacencyList().add(vtx_StateDisconnectByApp);

        vtx_SendEndofSession_2.getAdjacencyList().add(vtx_StateDisconnectByApp);

        //     by Device
        VertexBLEState vtx_StateDisconnectByDevice = new VertexBLEState("State Disconnection by Device",
                new BLEDisconnectState(this, BluetoothGatt.GATT_SUCCESS, BluetoothProfile.STATE_DISCONNECTED));

        // EDGE : vtx_State5_0x1A_5 ---> vtx_StateDiconnectByDevice
        vtx_State5_0x1A_5.getAdjacencyList().add(vtx_StateDisconnectByDevice);

    }

    //////////////////////////////////////////////////////////////////////////
    //  Service Discovery
    //    - doDiscoverService
    //    - Classes for relevant states
    //////////////////////////////////////////////////////////////////////////
    @Override
    public void doDiscoverService(IBLEDiscoverService ibleDiscoverService) {
        if (path != null && index() < path.size()) {
            BLEState state = path.get(index());
            if (state instanceof ServiceDiscoverFollowedByDeviceTimeReply) {
                state.action(ibleDiscoverService);

                incIndex();

                BLEState nextstate = path.get(index());
                nextstate.setupAction();

                return;
            }

            throw new BLEStateException("doDiscoverService: " + state.getClass());
        }

        throw new BLEStateException("doDiscoverService: fails "
                + (path != null) + " "
                + (index() < path.size()) );
    }

    // HRM3200 Service UUID and characteristic UUID
    public static UUID serviceUuid = UUID.fromString("0000ff00-0000-1000-8000-00805f9b34fb");
    public static UUID characteristicUuid = UUID.fromString("0000ff01-0000-1000-8000-00805f9b34fb");

    //////////////////////////////////////////////////////////////////////////
    //  Notification (Change Characterisitcs)
    //    - doNotification
    //    - Classes for relevant states
    //////////////////////////////////////////////////////////////////////////

    @Override
    public void doNotification(IBLEChangeCharacteristic ibleChangeCharacteristic) {
        if (path != null && index() < path.size()) {
            BLEState state = path.get(index());

            if (state instanceof DeviceTimeReplyState
                    || state instanceof RealtimeDataReply
                    || state instanceof SendSessionInfo
                    || state instanceof SendSessionCount
                    || state instanceof InitializeData
                    || state instanceof StoredDataReply
                    || state instanceof SendEndOfSession)
            {
                state.action(ibleChangeCharacteristic);

                incIndex();

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

    //////////////////////////////////////////////////////////////////////////
    //  Write Characterisitcs  (Commands by Apps)
    //    - doWriteCharacteristic
    //    - Classes for relevant states
    //////////////////////////////////////////////////////////////////////////

    @Override
    public void doWriteCharacteristic(BluetoothGattCharacteristic btGattCharacteristic,
                                      IBLEChangeCharacteristic ibleChangeCharacteristic) {
        if (path != null && index() < path.size()) {
            BLEState state = path.get(index());

            if (state instanceof OK_0x11_State
                    || state instanceof AppTime_0x80_0x81_State
                    || state instanceof REQ_RealtimeData_0x82_0x01
                    || state instanceof REQ_DownloadStoredData_0x82_0x03_0x83
                    || state instanceof CANCEL_DownloadStoredData_0x82_0x01_0x83
                    || state instanceof OK_0x15_State
                    || state instanceof REQ_SessionInfo_0x84_0x85
                    || state instanceof REQ_Disconnection_0x82_0x02_State)
            {
                state.action(btGattCharacteristic, ibleChangeCharacteristic);

                incIndex();

                BLEState nextstate = path.get(index());
                nextstate.setupAction();

                return;
            }

            throw new BLEStateException("doWriteCharacteristic: " + state.getClass().getCanonicalName());
        }

        throw new BLEStateException("doWriteCharacteristic: path fails "
                + (path != null) + " "
                + (index() < path.size()) );
    }

    //////////////////////////////////////////////////////////////////////////
    //  Disconnection
    //    - doDisconnect
    //    - Classes for relevant states
    //////////////////////////////////////////////////////////////////////////

    @Override
    public void doDisconnect(IBLEDisconnect ibleDisconnect) {
        if (path != null && index() < path.size()) {
            BLEState state = path.get(index());

            if (state instanceof BLEDisconnectState
                    || state instanceof DisconnectByApp)
            {
                // We arrive at the final state!!
                state.action(ibleDisconnect);

                // No incIndex();
                return;
            }

            throw new BLEStateException("doDisconnect: " + state.getClass().getCanonicalName());
        }

        throw new BLEStateException("doDisconnect: path fails "
                + (path != null) + " "
                + (index() < path.size()) );
    }

}
