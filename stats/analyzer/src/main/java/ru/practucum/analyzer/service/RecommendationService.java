package ru.practucum.analyzer.service;

import ru.practicum.ewm.stats.proto.messages.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.messages.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.messages.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.proto.messages.UserPredictionsRequestProto;

import java.util.List;

public interface RecommendationService {

    List<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto proto);

    List<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto proto);

    List<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto proto);
}
