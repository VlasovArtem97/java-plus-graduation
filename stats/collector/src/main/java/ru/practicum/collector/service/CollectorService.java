package ru.practicum.collector.service;

import ru.practicum.ewm.stats.proto.messages.UserActionProto;

public interface CollectorService {

    void addActionUser(UserActionProto actionProto);
}
