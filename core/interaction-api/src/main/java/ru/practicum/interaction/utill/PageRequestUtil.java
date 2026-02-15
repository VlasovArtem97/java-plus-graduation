package ru.practicum.interaction.utill;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PageRequestUtil {
    public static Pageable of(int from, int size, Sort sort) {
        int page = from / size;
        return PageRequest.of(page, size, sort);
    }
}