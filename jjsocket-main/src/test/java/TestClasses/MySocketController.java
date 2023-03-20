package TestClasses;

import com.jjerome.annotations.SocketController;
import com.jjerome.annotations.SocketMapping;
import com.jjerome.models.Request;
import com.jjerome.models.Response;
import org.springframework.http.HttpStatus;

@SocketController
public class MySocketController {

    @SocketMapping(reqPath = "/hello")
    public Response helloMapping(Request<String> request){
        System.out.println(request);
        return new Response("/hello", "Hello " + request.getReqBody(), HttpStatus.OK);
    }

    @SocketMapping(reqPath = "/controller/name", resPath = "/changeName")
    public Response getNameMapping(Request<String> request){
        System.out.println(request);
        return new Response("MySocketController", HttpStatus.OK);
    }
}
