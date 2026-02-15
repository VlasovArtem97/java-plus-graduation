package ru.practicum.interaction.feignclient;

import org.springframework.cloud.openfeign.EnableFeignClients;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
Создал общий интерфейс, чтобы в главном классе других сервисах не прописывать постоянно путь, если путь поменяется,
удобно сделать это в одном месте, тем более в общей библиотеке
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@EnableFeignClients(basePackages = "ru.practicum.interaction.feignclient")
public @interface EnableFeignClientInterface {
}
