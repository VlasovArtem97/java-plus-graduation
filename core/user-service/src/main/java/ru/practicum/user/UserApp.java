package ru.practicum.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.practicum.interaction.feignclient.EnableFeignClientInterface;

@SpringBootApplication
@EnableFeignClientInterface
public class UserApp {

    public static void main(String[] args) {
        SpringApplication.run(UserApp.class, args);
    }
}
