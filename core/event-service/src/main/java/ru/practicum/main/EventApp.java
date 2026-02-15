package ru.practicum.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.practicum.interaction.feignclient.EnableFeignClientInterface;

@SpringBootApplication
//Кастомный интерфейс по добавлению @EnableFeignClients c указанием пакета
@EnableFeignClientInterface
public class EventApp {

    public static void main(String[] args) {
        SpringApplication.run(EventApp.class, args);
    }
}
