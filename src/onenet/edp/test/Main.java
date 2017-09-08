package onenet.edp.test;

import onenet.edp.*;
import onenet.edp.util.CommonUtil;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import static onenet.edp.test.Main.STATUS.UNCONNECTED;


/**
 * 测试代码在这里
 */
public class Main {
    enum STATUS {
        UNCONNECTED, WORK
    }

    private static STATUS status = UNCONNECTED;
    private static IotSocketClient client;
    private static SocketReadThread readThread;

    public static void main(String[] args) {
        client = new IotSocketClient();
        readThread = new SocketReadThread(client);
        run();
    }

    public static void run() {
        while (true) {
            switch (status) {
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
                    status = STATUS.WORK;
                    break;
                }

                case WORK: { //设备工作状态
                    CommonUtil.log("step in work thread");
                    ExecutorService executor = new ScheduledThreadPoolExecutor(1);
                    executor.execute(readThread);

                    //进入循环,每隔30s读取SocketReadThread状态
                    while (true) {
                        try {
                            Thread.sleep(30000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        //SocketReadThread空闲,则发心跳到服务器,激活读取线程
                        if (readThread.isIdle()) {
                            CommonUtil.log("step in ping");
                            PingMsg pingMsg = new PingMsg();
                            client.writeBack(pingMsg.packMsg());
                        }

                        //SocketReadThread发现socket连接关闭,则关闭读取线程,进入连接状态
                        if (readThread.isUnConnected()) {
                            executor.shutdown();
                            status = STATUS.UNCONNECTED;
                            break;
                        }
                    }
                }
            }
        }
    }
}
