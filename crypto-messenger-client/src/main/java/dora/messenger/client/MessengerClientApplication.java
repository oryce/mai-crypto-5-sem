package dora.messenger.client;

import com.formdev.flatlaf.FlatLightLaf;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;

import java.awt.EventQueue;

@SpringBootApplication
public class MessengerClientApplication {

    public static void main(String[] args) {
        FlatLightLaf.setup();

        ApplicationContext context = new SpringApplicationBuilder(MessengerClientApplication.class)
            .headless(false)
            .web(WebApplicationType.NONE)
            .run(args);

        EventQueue.invokeLater(() -> {
            HelloWorldFrame frame = context.getBean(HelloWorldFrame.class);
            frame.setVisible(true);
        });
    }
}
