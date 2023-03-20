import clientClasses.SocketClient;
import com.jjerome.models.Request;
import com.jjerome.models.Response;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SocketControllersTest {

    @Test
    public void socket_controller_hello_test() throws IOException, InterruptedException {
        SocketClient socketClient = new SocketClient();
        socketClient.startConnection("127.0.0.1" , 8001);

        for (int i = 0; i < 5; i++) {
            Request<String> request = new Request<>("/hello", "socket_controller_hello_test");
            Response response = socketClient.sendMessage(request);

            System.out.println(response);
            assertEquals("Hello socket_controller_hello_test", response.getResBody());

            sleep(1000);
        }
    }


    @Test
    public void multi_thread_controller_name_test() throws InterruptedException {
        List<Thread> threadList = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            Thread thread = new Thread(() -> {
                try {
                    SocketClient socketClient = new SocketClient();
                    socketClient.startConnection("127.0.0.1" , 8001);

                    for (int j = 0; j < 5; j++) {
                        Request<String> request = new Request<>("/hello", "socket_controller_hello_test");
                        Response response = socketClient.sendMessage(request);

                        System.out.println(response);
                        assertEquals("Hello socket_controller_hello_test", response.getResBody());

                        sleep(1000);
                    }
                } catch (IOException | InterruptedException exception){
                    System.out.println(exception.getMessage());
                }
            });
            thread.start();
            threadList.add(thread);
        }

        for(Thread thread : threadList){
            thread.join();
        }
    }
}