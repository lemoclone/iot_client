package onenet.edp.test;

import java.io.*;
import java.net.*;

public class IotSocketClient {
    private Socket socket;

    public IotSocketClient() {
        String testServerName = "jjfaedp.hedevice.com";
        int port = 876;
        socket = openSocket(testServerName, port);
    }

    public byte[] write(byte[] writeTo) {
        InputStream inputStream = null;
        try {
            OutputStream outStream = socket.getOutputStream();
            outStream.write(writeTo);
            outStream.flush();
            byte[] readBuffer = new byte[1024];
            inputStream = socket.getInputStream();
            int readSize = inputStream.read(readBuffer);
            return readByte(readBuffer, readSize);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }
        return null;
    }

    public void writeBack(byte[] writeTo) {
        InputStream inputStream = null;
        try {
            OutputStream outStream = socket.getOutputStream();
            outStream.write(writeTo);
            outStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }
    }


    public byte[] read() {
        InputStream inputStream = null;
        try {
            byte[] readBuffer = new byte[1024];
            inputStream = socket.getInputStream();
            int readSize = inputStream.read(readBuffer);
            return readByte(readBuffer, readSize);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }
        return null;
    }

    private byte[] readByte(byte[] readBuffer, int readSize) {
        byte[] rcvPacket = null;
        if (readSize > 0) {
            rcvPacket = new byte[readSize];
            System.arraycopy(readBuffer, 0, rcvPacket, 0, readSize);
        }
        return rcvPacket;
    }


    private Socket openSocket(String server, int port) {
        Socket socket;
        try {
            InetAddress inetAddress = InetAddress.getByName(server);
            SocketAddress socketAddress = new InetSocketAddress(inetAddress, port);
            socket = new Socket();
            int timeoutInMs = 60 * 1000;
            socket.connect(socketAddress, timeoutInMs);
            return socket;
        } catch (SocketTimeoutException ste) {
            ste.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}