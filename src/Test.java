import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.*;

import onenet.edp.*;
import onenet.edp.Common.MsgType;

import onenet.edp.ConnectMsg;
import onenet.edp.ConnectRespMsg;
import onenet.edp.EdpKit;
import onenet.edp.EdpMsg;
import onenet.edp.PingMsg;
import onenet.edp.PushDataMsg;
import onenet.edp.SaveDataMsg;
import onenet.edp.SaveRespMsg;
import onenet.edp.SoftInfo;
import onenet.edp.UpdateMsg;
import onenet.edp.UpdateRespMsg;
import org.json.JSONObject;

public class Test {

    /**
     * byte数组转换转16进制字符串
     *
     * @param array
     * @return hex string. if array is null, return null.
     */
    public static String byteArrayToString(byte[] array) {
        if (array == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            String hex = Integer.toHexString(array[i] & 0xff);
            if (hex.length() == 1) {
                sb.append("0" + hex);
            } else {
                sb.append(hex);
            }
        }

        return sb.toString();
    }

    /**
     * 打印日志到控制台
     *
     * @param info log information
     */
    public static void log(Object info) {
        System.out.println(info);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        //test sdk
        //与服务器建立socket连接
        //online
        String serverIp = "jjfaedp.hedevice.com";
        int serverPort = 876;
        Socket socket = new Socket(serverIp, serverPort);
        socket.setSoTimeout(60 * 1000);        //设置超时时长为一分钟
        InputStream inStream = socket.getInputStream();
        OutputStream outStream = socket.getOutputStream();

        //向服务器发送连接请求
        int devId = 12067945;                                //***用户请使用自己的设备ID***
        String devKey = "XM1xAFfU=O9S4hHOaRjg6K5=zU4=";    //***用户请使用自己的设备的鉴权key***
        ConnectMsg connectMsg = new ConnectMsg();
        byte[] packet = connectMsg.packMsg(devId, devKey);
        //若需要提供userId或edp超时时长，可参考一下方法
//		connectMsg.packMsg(deviceId, userId, authInfo);
//		connectMsg.packMsg(deviceId, userId, authInfo, connectTimeout);
        //若用项目id和auth_info鉴权，需deviceId设置为0
//		connectMsg.packMsg(0, 项目ID, auth_info);
        outStream.write(packet);
        log("[connect]packet size:" + packet.length);
        log("[connect]packet:" + byteArrayToString(packet));

        Thread.sleep(500);

        //接收服务器的连接响应
        EdpKit kit = new EdpKit();        //初始化一个EdpKit实例，用于服务器响应包的数据解析
        byte[] readBuffer = new byte[1024];    //接收数据的缓存池
        int readSize = inStream.read(readBuffer);
        if (readSize > 0) {
            byte[] recvPacket = new byte[readSize];
            System.arraycopy(readBuffer, 0, recvPacket, 0, readSize);
            List<EdpMsg> msgs = kit.unpack(recvPacket);
            if (msgs == null) {
                log("[connect response] receive packet exception.");
            } else {
                EdpMsg msg = msgs.get(0);
                if (msg.getMsgType() == MsgType.CONNRESP) {
                    ConnectRespMsg connectRespMsg = (ConnectRespMsg) msg;
                    log("[connect response] res_code:" + connectRespMsg.getResCode());
                    log("[connect response] res_msg:" + connectRespMsg);
                } else {
                    log("[connect response] response packet is not connect response.type:" + msg.getMsgType());
                }
            }
        }

        //向服务器发送心跳
        PingMsg pingMsg = new PingMsg();
        outStream.write(pingMsg.packMsg());
        readSize = inStream.read(readBuffer);
        if (readSize > 0) {
            byte[] recvPacket = new byte[readSize];
            System.arraycopy(readBuffer, 0, recvPacket, 0, readSize);
            log("[ping response]packet:" + byteArrayToString(recvPacket));
        }

        //向服务器发送转发数据
        for (int i = 0; i < 5; i++) {
            int desDevId = 12067945;            //***用户请使用自己的目标设备ID***
            String pushData = "test sdk push data:" + i;
            PushDataMsg pushDataMsg = new PushDataMsg();
            packet = pushDataMsg.packMsg(desDevId, pushData);
            outStream.write(packet);
            Thread.sleep(5 * 1000);
        }
//		
//		//从服务器接收转发数据
//		for (int i = 0; i < 5; i++) {
//			readSize = inStream.read(readBuffer);
//			if (readSize > 0) {
//				byte[] recvPacket = new byte[readSize];
//				System.arraycopy(readBuffer, 0, recvPacket, 0, readSize);
//				List<EdpMsg> msgs = kit.unpack(recvPacket);
//				if (msgs == null) {
//					log("[receive push data]can't find complete packet.");
//				}
//				else {
//					PushDataMsg pushDataMsg = (PushDataMsg)msgs.get(0);
//					log("[receive push data]src_dev_id:" + pushDataMsg.getSrcDeviceId());
//					log("[receive push data]data:" + new String(pushDataMsg.getData()));
//				}
//			}
//		}

        //向服务器发送存储数据
        SaveDataMsg saveDataMsg = new SaveDataMsg();
        //格式1消息
        JSONObject saveData1 = new JSONObject();
        SaveDataMsg.packSaveData1Msg(saveData1, "111", "test1", new Date(), "101");
        saveDataMsg.packMsg(1, null, saveData1.toString().getBytes());    //存储消息首次添加数据点，请使用该方法
        //若同时有需要转发的设备，请参考如下方法
//		saveDataMsg.packMsg(int desDeviceId, int dataType, String tokenStr, byte[] _data);
        //格式2消息
        String saveData2 = "test sdk save data type 2 msg";
        JSONObject saveData2Token = new JSONObject();
        SaveDataMsg.packSaveData2Token(saveData2Token, "222", "test2", new Date(), null);
        saveDataMsg.addDatapoint(2, saveData2Token.toString(), saveData2);    //存储消息多次添加其他格式数据点，请重复调用此方法
        //格式3消息
        JSONObject saveData3 = new JSONObject();
        SaveDataMsg.packSaveData3Msg(saveData3, "test3", "103");
        saveDataMsg.addDatapoint(3, null, saveData3.toString());
        //格式4消息
        JSONObject saveData4 = new JSONObject();
        SaveDataMsg.packSaveData4Msg(saveData4, "test4", new Date(), "104");
        saveDataMsg.addDatapoint(4, null, saveData4.toString());
        //格式5消息
        StringBuilder saveData5 = new StringBuilder();
        SaveDataMsg.packetSaveData5Msg(saveData5, "105", "test5");
        saveDataMsg.addDatapoint(5, null, saveData5.toString());
        packet = saveDataMsg.commit();                                        //添加完数据点，commit来生成消息包
        outStream.write(packet);

        //接收存储数据点的响应
        for (int j = 0; j < 2; j++) {
            readSize = inStream.read(readBuffer);
            if (readSize > 0) {
                byte[] recvPacket = new byte[readSize];
                System.arraycopy(readBuffer, 0, recvPacket, 0, readSize);
                List<EdpMsg> msgs = kit.unpack(recvPacket);
                if (msgs == null) {
                    log("[save resp]don't receive responce packet.");
                } else {
                    for (int i = 0; i < msgs.size(); i++) {
                        EdpMsg msg = msgs.get(i);
                        if (msg.getMsgType() != MsgType.SAVERESP) {
                            log("[save resp]receive not responce packet.type:" + msg.getMsgType());
                        } else {
                            SaveRespMsg saveRespMsg = (SaveRespMsg) msg;
                            log("[save resp]data:" + new String(saveRespMsg.getData()));
                        }
                    }
                }
            } else if (readSize == -1) {
                log("[save resp]no responce packet.");
                break;
            }
        }

        //固件信息消息示例
        //上报固件信息
        UpdateMsg updateMsg = new UpdateMsg(MsgType.UPDATEREQ);
        Collection<SoftInfo> collection = new ArrayList<>();
        collection.add(new SoftInfo("soft1", "1.0.SNAPSHORT"));
        collection.add(new SoftInfo("soft2", "2.0"));


        outStream.write(updateMsg.packMsg(collection));
        log("finish send update msg");
        //接收平台下发的固件信息，前提是平台下发了该信息
        readSize = inStream.read(readBuffer);
        if (readSize > 0) {
            byte[] recvPacket = new byte[readSize];
            System.arraycopy(readBuffer, 0, recvPacket, 0, readSize);
            List<EdpMsg> msgs = kit.unpack(recvPacket);
            for (EdpMsg msg : msgs) {
                if (msg.getMsgType() == MsgType.UPDATERESP) {
                    UpdateRespMsg updateRespMsg = (UpdateRespMsg) msg;
                    log("receive soft info size" + updateRespMsg.getSoftInfos().size());
                }
            }
        } else {
            log("not receive data");
        }

        //发送和接收透传第三方数据示例
        //发送透传数据
        TunnelMsg tunnelMsg = new TunnelMsg();
        for (int i = 0; i < 10; i++) {
            outStream.write(tunnelMsg.packMsg("svr_name1", ("test by yonghua " + i).getBytes()));
            log("tunnel msg " + i);
            Thread.sleep(2000);
        }
        //接收透传数据，使用前提，接收之前，第三方服务需要下发透传数据
        readSize = inStream.read(readBuffer);
        if (readSize > 0) {
            byte[] recvPacket = new byte[readSize];
            System.arraycopy(readBuffer, 0, recvPacket, 0, readSize);
            List<EdpMsg> msgs = kit.unpack(recvPacket);
            for (EdpMsg msg : msgs) {
                if (msg.getMsgType() == MsgType.TUNNEL) {
                    TunnelMsg tunnel = (TunnelMsg) msg;
                    log("server:" + tunnel.getServer() + " data:" + new String(tunnel.getData()));
                }
            }
        } else {
            log("not receive data");
        }

        //关闭socket连接
        socket.close();
        inStream.close();
        outStream.close();
    }
}
