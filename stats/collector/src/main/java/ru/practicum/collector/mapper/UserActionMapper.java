package ru.practicum.collector.mapper;

import org.mapstruct.Mapper;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.proto.messages.ActionTypeProto;
import ru.practicum.ewm.stats.proto.messages.UserActionProto;

import java.time.Instant;

@Mapper(componentModel = "spring")
public interface UserActionMapper {

    UserActionAvro toUserActionAvro(UserActionProto actionProto);

    default ActionTypeAvro toActionTypeAvro(ActionTypeProto actionTypeProto) {
        return switch (actionTypeProto) {
            case ACTION_VIEW -> ActionTypeAvro.VIEW;
            case ACTION_REGISTER -> ActionTypeAvro.REGISTER;
            case ACTION_LIKE -> ActionTypeAvro.LIKE;
            case null, default ->
                    throw new IllegalStateException("Некорректно указан тип действия пользователя(UserAction)");
        };
    }

    default Instant mapTimestamp(com.google.protobuf.Timestamp timestamp) {
        return Instant.ofEpochSecond(timestamp.getSeconds(),
                timestamp.getNanos());
    }
}
