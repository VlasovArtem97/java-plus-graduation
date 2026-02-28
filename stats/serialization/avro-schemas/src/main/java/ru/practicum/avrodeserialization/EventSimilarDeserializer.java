package ru.practicum.avrodeserialization;

import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

public class EventSimilarDeserializer extends BaseAvroDeserializer {

    public EventSimilarDeserializer(EventSimilarityAvro avro) {
        super(EventSimilarityAvro.getClassSchema());
    }
}
