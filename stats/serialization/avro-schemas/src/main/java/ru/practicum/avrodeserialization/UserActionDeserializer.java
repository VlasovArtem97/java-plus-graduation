package ru.practicum.avrodeserialization;

import ru.practicum.ewm.stats.avro.UserActionAvro;

public class UserActionDeserializer extends BaseAvroDeserializer {

    public UserActionDeserializer() {
        super(UserActionAvro.getClassSchema());
    }
}
