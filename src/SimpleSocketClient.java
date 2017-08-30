import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;

public class SimpleSocketClient {
    public static void main(String[] args) {
        new SimpleSocketClient();
    }

    public SimpleSocketClient() {
        String testServerName = "183.230.40.40";
        int port = 1811;
        try {
            Socket socket = openSocket(testServerName, port);

            //*92842#20170730test01#tcptest*
            //*PID#AuthCode#ParserName*
            String result = writeToAndReadFromSocket(socket, "*92842#20170730test01#tcptest*");

            System.out.println(result);
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String writeToAndReadFromSocket(Socket socket, String writeTo) throws Exception {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedWriter.write(writeTo);
            bufferedWriter.flush();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String str;
            while ((str = bufferedReader.readLine()) != null) {
                sb.append(str + "\n");
            }

            bufferedReader.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    private Socket openSocket(String server, int port) throws Exception {
        Socket socket;

        try {
            InetAddress inteAddress = InetAddress.getByName(server);
            SocketAddress socketAddress = new InetSocketAddress(inteAddress, port);
            socket = new Socket();
            int timeoutInMs = 10 * 1000;
            socket.connect(socketAddress, timeoutInMs);
            return socket;
        } catch (SocketTimeoutException ste) {
            System.err.println("Timed out waiting for the socket.");
            ste.printStackTrace();
            throw ste;
        }
    }
}