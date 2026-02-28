package ru.practicum.statsclient;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.proto.messages.UserActionProto;
import ru.practicum.ewm.stats.proto.services.UserActionControllerGrpc;

@Service
public class CollectorClient {

    @GrpcClient("collector")
    private UserActionControllerGrpc.UserActionControllerBlockingStub client;

    public void collectUserAction(UserActionProto action) {
        client.collectUserAction(action);
    }
}
