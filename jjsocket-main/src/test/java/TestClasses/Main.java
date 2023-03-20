package TestClasses;

import com.jjerome.models.SocketApplication;

public class Main {
    public static void main(String[] args) {
        SocketApplication socketApplication = new SocketApplication(8001);
        socketApplication.run("TestClasses");
    }
}
