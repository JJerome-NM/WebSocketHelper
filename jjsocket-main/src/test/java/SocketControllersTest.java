import clientClasses.SocketClient;
import com.jjerome.models.Request;
import com.jjerome.models.Response;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SocketControllersTest {
    @Test
    public void send_socket_message_to_test() throws InterruptedException {
        Thread thread = new Thread(() -> {
            try{
                SocketClient client = new SocketClient();
                client.startConnection("127.0.0.1", 8001);

                for (int i = 0; i < 5; i++) {
                    Response response = client.sendMessage(new Request("" +
                            "/test", "Its '/test' request. My name"));

                    System.out.println(response);
                    Thread.sleep(1000);
                }
            } catch (IOException | InterruptedException exception){
                System.out.println(exception.getMessage());
            }
        });

        thread.start();
        thread.join();
    }

    @Test
    public void send_socket_message_to_test2() throws InterruptedException {
        Thread thread = new Thread(() -> {
            try{
                SocketClient client = new SocketClient();
                client.startConnection("127.0.0.1", 8001);

                for (int i = 0; i < 5; i++) {
                    Response response = client.sendMessage(new Request(
                            "/test/2", "Its '/test/2' request. My name send_socket_message_to_test2"));

                    System.out.println(response);
                    Thread.sleep(1000);
                }
            } catch (IOException | InterruptedException exception){
                System.out.println(exception.getMessage());
            }
        });

        thread.start();
        thread.join();
    }

    @Test
    public void send_socket_message_to_SocketControllerTest() throws InterruptedException {
        Thread thread = new Thread(() -> {
            try{
                SocketClient client = new SocketClient();
                client.startConnection("127.0.0.1", 8001);

                for (int i = 0; i < 5; i++) {
                    Response response = client.sendMessage(new Request(
                            "/SocketControllerTest/", "Its '/SocketControllerTest/' request. My name send_socket_message_to_SocketControllerTest"));

                    System.out.println(response);
                    Thread.sleep(1000);
                }
            } catch (IOException | InterruptedException exception){
                System.out.println(exception.getMessage());
            }
        });

        thread.start();
        thread.join();
    }


    @Test
    public void multi_thread_socket_send_message() throws InterruptedException{
        List<Thread> threadList = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            Thread thread = new Thread(() -> {
                try{
                    SocketClient client = new SocketClient();
                    client.startConnection("127.0.0.1", 8001);

                    for (int j = 0; j < 5; j++) {
                        Response response = client.sendMessage(new Request(
                                "/threads", "Its '/threads' request. My name " + Thread.currentThread().getName()));

                        System.out.println(response);
                        Thread.sleep(1000);
                    }
                } catch (IOException | InterruptedException exception){
                    System.out.println(exception.getMessage());
                }
            });
            thread.start();

            threadList.add(thread);
        }

        for (Thread thread : threadList){
            thread.join();
        }
    }
}