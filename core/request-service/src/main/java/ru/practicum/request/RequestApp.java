package ru.practicum.request;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.practicum.interaction.feignclient.EnableFeignClientInterface;

@SpringBootApplication(scanBasePackages = {"ru.practicum.request", "ru.practicum.interaction.config"})
@EnableFeignClientInterface
public class RequestApp {
    public static void main(String[] args) {
        SpringApplication.run(RequestApp.class, args);
    }
}
