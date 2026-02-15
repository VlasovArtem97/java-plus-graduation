package ru.practicum.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.practicum.interaction.feignclient.EnableFeignClientInterface;

@SpringBootApplication
@EnableFeignClientInterface
public class EventApp {

    public static void main(String[] args) {
        SpringApplication.run(EventApp.class, args);
    }
}
