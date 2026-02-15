package ru.practicum.interaction.feignclient;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.interaction.config.Config;
import ru.practicum.interaction.dto.user.UserDto;

@FeignClient(name = "user-service", path = "/admin/users", configuration = Config.class)
public interface UserFeignClient {

    @GetMapping("/{userId}")
    UserDto findUserById(@Positive @NotNull @PathVariable("userId") Long userId);
}
