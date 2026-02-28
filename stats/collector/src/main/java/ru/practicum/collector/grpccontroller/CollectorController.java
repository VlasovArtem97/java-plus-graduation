package ru.practicum.collector.grpccontroller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.collector.service.CollectorService;
import ru.practicum.ewm.stats.proto.messages.UserActionProto;
import ru.practicum.ewm.stats.proto.services.UserActionControllerGrpc;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class CollectorController extends UserActionControllerGrpc.UserActionControllerImplBase {

    private final CollectorService collectorService;

    @Override
    public void collectUserAction(UserActionProto request, StreamObserver<Empty> responseObserver) {
        try {
            log.info("Получен запрос на добавления действия пользователя: {}", request);
            collectorService.addActionUser(request);
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (IllegalStateException e) {
            log.error("Ошибка в валидации данных: {}", e.getMessage());
            responseObserver.onError(new StatusRuntimeException(
                    Status.INVALID_ARGUMENT
                            .withDescription(e.getMessage())
                            .withCause(e)
            ));
        } catch (Exception e) {
            log.error("Ошибка при работе с Collector: {}", e.toString());
            String description = e.getMessage() != null ? e.getMessage(): e.toString();
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription("Ошибка при работе с Collector: " + description)
                            .withCause(e)
            ));
        }
    }
}
