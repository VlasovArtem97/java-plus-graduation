package ru.practicum.rating;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.practicum.interaction.feignclient.EnableFeignClientInterface;

@SpringBootApplication
@EnableFeignClientInterface
public class RatingApp {

    public static void main(String[] args) {
        SpringApplication.run(RatingApp.class, args);
    }
}
