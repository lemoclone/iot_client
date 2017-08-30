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
        try {
            OutputStream outStream = socket.getOutputStream();
            outStream.write(writeTo);
            outStream.flush();
            byte[] readBuffer = new byte[1024];
            int readSize = socket.getInputStream().read(readBuffer);
            if(readSize > 0) {
                byte[] rcvPacket = new byte[readSize];
                System.arraycopy(readBuffer, 0, rcvPacket, 0, readSize);
                return rcvPacket;
            } else{
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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