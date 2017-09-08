package onenet.edp.test;

import onenet.edp.*;
import onenet.edp.util.CommonUtil;

import java.util.List;

/**
 * Socket读取线程
 */
public class SocketReadThread implements Runnable {
    //Socket客户端
    private IotSocketClient client;
    //Socket上次读取时间
    private long lastReadTime = System.currentTimeMillis();
    //Socket连接状态
    private boolean unConnected;

    private static final long IDLE_SECONDS_IN_MILLIS = 60 * 1000;


    public SocketReadThread(IotSocketClient client) {
        this.client = client;
    }

    @Override
    public void run() {
        while (true) {
            lastReadTime = System.currentTimeMillis();
            byte[] packet = client.read(); //此处阻塞,直到有数据读到,才继续执行
            //todo 数据为空,说明连接断开？
            if (packet == null) {
                unConnected = true;
                break;
            }
            CommonUtil.log("SocketReadThread [cmd response] packet: " + CommonUtil.byteArrayToHexString(packet));
            EdpKit kit = new EdpKit();
            List<EdpMsg> msgs = kit.unpack(packet);
            for (EdpMsg msg : msgs) {
                if (msg.getMsgType() == Common.MsgType.CMDREQ) {
                    CmdRequestMsg cmdRequestMsg = (CmdRequestMsg) msg;
                    CommonUtil.log("SocketReadThread CmdRequestMsg: " + cmdRequestMsg);
                    CmdRespMsg cmdRespMsg = new CmdRespMsg();
                    byte[] cmdRespMsgBytes = cmdRespMsg.packMsg(cmdRequestMsg.getCmdid(), CommonUtil.stringToByteArray("good"));
                    client.writeBack(cmdRespMsgBytes);
                }
            }
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 判断客户端socket是否空闲,超过 IDLE_SECONDS_IN_MILLIS 则为空闲
     * @return
     */
    public boolean isIdle() {
        return System.currentTimeMillis() - lastReadTime > IDLE_SECONDS_IN_MILLIS;
    }

    /**
     * 判断客户端socket是否断开
     * @return
     */
    public boolean isUnConnected(){
        return unConnected;
    }
}
