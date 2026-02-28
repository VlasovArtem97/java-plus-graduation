package ru.practucum.analyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import ru.practucum.analyzer.service.kafka.EventSimilarStarter;
import ru.practucum.analyzer.service.kafka.UserActionStarter;

@SpringBootApplication(scanBasePackages = "ru.practucum.analyzer")
public class AnalyzerApp {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(AnalyzerApp.class, args);

        final UserActionStarter userActionProcessor = context.getBean(UserActionStarter.class);
        EventSimilarStarter eventSimilarStarter = context.getBean(EventSimilarStarter.class);

        Thread userActionsThread = new Thread(userActionProcessor);
        userActionsThread.setName("UserActionHandlerThread");
        userActionsThread.start();

        // В текущем потоке начинаем обработку сходств
        eventSimilarStarter.start();
    }
}
