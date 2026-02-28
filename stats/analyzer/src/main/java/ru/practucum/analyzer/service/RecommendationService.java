package ru.practucum.analyzer.service;

import ru.practicum.ewm.stats.proto.messages.*;

import java.util.List;

public interface RecommendationService {

    List<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto proto);

    List<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto proto);

    List<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto proto);
}
