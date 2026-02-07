package ru.practicum.ewm.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"ru.korshunov.statsclient"})
public class StatClient {
}
