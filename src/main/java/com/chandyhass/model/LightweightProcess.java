package com.chandyhass.model;

import com.chandybass.kernel.Orchestrator;
import com.chandyhass.server.SimpleNonBlockingServer;
import com.chandynass.predicate.MessageType;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public interface LightweightProcess extends Runnable {
    Logger logger = LoggerFactory.getLogger(LightweightProcess.class);

    int getAdminPort();

    int getDataPort();

    String getHostName();

    String pid();

    Set<String> getDependentProcesses();

    boolean blocking();

    SimpleNonBlockingServer getAdminServer();

    SimpleNonBlockingServer getDataServer();

    Map<String, Integer> getNumMap();

    Map<String, Boolean> getWaitMap();

    boolean isInitiator();

    String getEngagingQueryPID();

    void setEngagingQueryPID(String pid) ;

    default void start(){
        logger.info("Starting servers ..");
        Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("AdminServer-thread-%d").build()).submit(() -> getAdminServer().listen());
        Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("DataServer-thread-%d").build()).submit(() -> getDataServer().listen());
        logger.info("DONE");
    }

    default ByteBuffer processIncomingMessage(String message) {
        getDependentProcesses().add(message);
        String ack = StringUtils.joinWith(":", pid(),
                getHostName(),
                String.valueOf(getDataPort()),
                String.valueOf(getAdminPort()));
        return ByteBuffer.wrap(ack.getBytes());
    }

    default void query(String i, String j, String k, MessageType messageType) {
        try {
            Socket socket = new Socket(Orchestrator.getHost(k), Orchestrator.getAdminPort(k));
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(StringUtils.joinWith(":", i, j, k, messageType.name()));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    default void sendHelloMsg() {
        ExecutorService executorService = Executors.newFixedThreadPool(getDependentProcesses().size());
        getDependentProcesses().forEach(k -> {
            executorService.submit(() -> {
                        Socket socket = new Socket(StringUtils.split(k, ":")[1],
                                Integer.parseInt(StringUtils.split(k, ":")[3]));
                        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                        oos.writeObject(processIncomingMessage(""));
                        return null;
                    }
            );

        });
    }

}
