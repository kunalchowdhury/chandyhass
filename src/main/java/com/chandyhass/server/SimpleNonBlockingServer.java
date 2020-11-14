package com.chandyhass.server;

import com.chandyhass.model.LightweightProcess;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;

public class SimpleNonBlockingServer {
    private final String hostname;
    private final int port;
    private final Predicate<SelectionKey> connectedPredicate;
    private final Predicate<SelectionKey> readablePredicate;
    private final Predicate<SelectionKey> writablePredicate;
    private final Map<SocketChannel, Queue<ByteBuffer>> dataToWrite;
    private final ThreadLocal<ByteBuffer> byteBufferThreadLocal;
    private final LightweightProcess process;
    private final ExecutorService executorService;
    private final ConcurrentLinkedQueue<SocketChannel> toWriteSockets;
    private volatile boolean running;
    private final static Logger logger = LoggerFactory.getLogger(SimpleNonBlockingServer.class);

    public SimpleNonBlockingServer(String hostname, int port, LightweightProcess process) {
        this.hostname = hostname;
        this.port = port;
        this.connectedPredicate = selectionKey ->
                selectionKey.isValid() && selectionKey.isAcceptable();
        this.readablePredicate = SelectionKey::isReadable;
        this.writablePredicate = SelectionKey::isWritable;
        this.dataToWrite = Maps.newConcurrentMap();
        this.byteBufferThreadLocal = ThreadLocal.withInitial(() -> ByteBuffer.allocate(1024));
        this.process = process;
        this.executorService = Executors.newFixedThreadPool(2);
        this.toWriteSockets = Queues.newConcurrentLinkedQueue();
        this.running = true;

    }

    public void listen() {
        try {
            logger.info("Server started at {}:{}", hostname, port);
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(hostname, port));
            serverSocketChannel.configureBlocking(false);
            Selector selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            while (running) {
                selector.select();
                SocketChannel changedSC ;
                while ((changedSC =  toWriteSockets.poll()) != null){
                    changedSC.register(selector, SelectionKey.OP_WRITE);
                }
                for (Iterator<SelectionKey> iterator = selector.selectedKeys().iterator(); iterator.hasNext(); ) {
                    SelectionKey k = iterator.next();
                    iterator.remove();
                    try {
                        if (connectedPredicate.test(k)) {
                            logger.info("Accepting connection - > {}", k);
                            accept(k);
                        } else if (readablePredicate.test(k)) {
                            logger.info("Reading from socket - > {}", k);
                            read(k);
                        } else if (writablePredicate.test(k)) {
                            logger.info("Sending reply  - > {}", k);
                            write(k);
                        }else{

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                };
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        executorService.shutdownNow();

    }

    private void write(SelectionKey selectionKey) throws IOException {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        Queue<ByteBuffer> byteBufferQueue = dataToWrite.get(socketChannel);
        ByteBuffer byteBuffer;
        while ((byteBuffer = byteBufferQueue.peek()) != null) {
            socketChannel.write(byteBuffer);
            if (!byteBuffer.hasRemaining()) {
                byteBufferQueue.poll();
            }else {
                return;
            }
        }
        socketChannel.register(selectionKey.selector(),SelectionKey.OP_READ);
    }

    private void read(SelectionKey selectionKey) throws IOException {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        ByteBuffer byteBuffer = byteBufferThreadLocal.get();
        byteBuffer.clear();
        int read = socketChannel.read(byteBuffer);
        if (read == -1) {
            dataToWrite.remove(socketChannel);
            return;
        }
        executorService.submit(() -> {
            byteBuffer.flip();
            String message = new String(byteBuffer.array());
            if(StringUtils.equals("STOP", message)) {
                dataToWrite.get(socketChannel).add(process.processIncomingMessage(message));
                Selector selector = selectionKey.selector();
                toWriteSockets.add(socketChannel);
                selector.wakeup();
            }
            else {
                running = false;
            }
            return null;

        });

    }

    private void accept(SelectionKey selectionKey) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(selectionKey.selector(), SelectionKey.OP_READ);
        dataToWrite.put(socketChannel, Queues.newConcurrentLinkedQueue());
    }

}
