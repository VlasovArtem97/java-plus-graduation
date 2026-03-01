package ru.practucum.analyzer.grpccontroller;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.stats.proto.messages.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.messages.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.messages.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.proto.messages.UserPredictionsRequestProto;
import ru.practicum.ewm.stats.proto.services.RecommendationsControllerGrpc;
import ru.practucum.analyzer.service.RecommendationService;

import java.util.List;
import java.util.function.Supplier;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class RecommendationController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

    private final RecommendationService recommendationService;

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        handleRequest(() -> recommendationService.getRecommendationsForUser(request), responseObserver);
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        handleRequest(() -> recommendationService.getSimilarEvents(request), responseObserver);
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        handleRequest(() -> recommendationService.getInteractionsCount(request), responseObserver);
    }

    private void handleRequest(Supplier<List<RecommendedEventProto>> serviceCall, StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            List<RecommendedEventProto> results = serviceCall.get();
            for (RecommendedEventProto event : results) {
                log.debug("Отправляю объект после запроса: {}", event);
                responseObserver.onNext(event);
            }
            log.debug("Все объекты отправлены после обработки запроса");
            responseObserver.onCompleted();
        } catch (IllegalStateException e) {
            log.error("Ошибка в валидации данных: {}", e.getMessage());
            responseObserver.onError(new StatusRuntimeException(
                    Status.INVALID_ARGUMENT
                            .withDescription(e.getMessage())
                            .withCause(e)
            ));
        } catch (Exception e) {
            log.error("Ошибка при работе с Analyzer: {}", e.toString());
            String description = e.getMessage() != null ? e.getMessage() : e.toString();
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription("Ошибка при работе с Analyzer: " + description)
                            .withCause(e)
            ));
        }
    }
}
