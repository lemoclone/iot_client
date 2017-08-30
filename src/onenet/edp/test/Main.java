package onenet.edp.test;

import onenet.edp.*;
import onenet.edp.util.CommonUtil;

import java.util.List;


/**
 * 测试代码在这里
 */
public class Main {
    enum STATUS {
        UNCONNECTED, CONNECTED
    }

    private static STATUS status = STATUS.UNCONNECTED;
    private static IotSocketClient client;

    public static void main(String[] args) {
        client = new IotSocketClient();
        run();
    }

    public static void run() {
        while (true) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            switch (status) {
                case CONNECTED: { //设备登录状态,循环发送心跳
                    while (true) {
                        PingMsg pingMsg = new PingMsg();
                        byte[] packet = client.write(pingMsg.packMsg());
                        if (packet == null) {
                            status = STATUS.UNCONNECTED;
                            break;
                        } else {
                            CommonUtil.log("[ping response] packet: " + CommonUtil.byteArrayToHexString(packet));
                            EdpKit kit = new EdpKit();
                            List<EdpMsg> msgs = kit.unpack(packet);
                            for (EdpMsg msg : msgs) {
                                if (msg.getMsgType() == Common.MsgType.PINGRESP) {
                                    PingRespMsg pingRespMsg = (PingRespMsg) msg;
                                    CommonUtil.log("PingRespMsg: " + pingRespMsg.getMsgType());
                                }
                                if(msg.getMsgType() == Common.MsgType.CMDREQ) {
                                    CmdRequestMsg cmdRequestMsg = (CmdRequestMsg)  msg;
                                    CommonUtil.log("CmdRequestMsg: " + cmdRequestMsg);
                                    CmdRespMsg cmdRespMsg = new CmdRespMsg();
                                    byte[] cmdRespMsgBytes = cmdRespMsg.packMsg(cmdRequestMsg.getCmdid(),CommonUtil.stringToByteArray("good"));
                                    client.write(cmdRespMsgBytes);
                                }
                            }
                        }
                        try {
                            Thread.sleep(10 * 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                }
                case UNCONNECTED: { //设备未登录状态,先登录
                    int devId = 12067945;                              //设备ID
                    String devKey = "XM1xAFfU=O9S4hHOaRjg6K5=zU4=";    //设备的APIKey,不是鉴权key
                    ConnectMsg connectMsg = new ConnectMsg();
                    byte[] packet = connectMsg.packMsg(devId, devKey);
                    byte[] res = client.write(packet);
                    if (res != null) {
                        EdpKit kit = new EdpKit();
                        List<EdpMsg> msgs = kit.unpack(res);
                        if (msgs == null) {
                            CommonUtil.log("[connect response] receive packet exception.");
                        } else {
                            EdpMsg msg = msgs.get(0);
                            if (msg.getMsgType() == Common.MsgType.CONNRESP) {
                                ConnectRespMsg connectRespMsg = (ConnectRespMsg) msg;
                                CommonUtil.log("[connect response] res_code:" + connectRespMsg.getResCode());
                                CommonUtil.log("[connect response] res_msg:" + connectRespMsg);
                            } else {
                                CommonUtil.log("[connect response] response packet is not connect response.type:" + msg.getMsgType());
                            }
                        }
                    }
                    System.out.println("connected res: " + res);
                    status = STATUS.CONNECTED;
                    break;
                }
            }
        }
    }
}
