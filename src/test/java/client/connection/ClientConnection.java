package client.connection;

import common.connection.CommandDecoder;
import common.connection.CommandEncoder;
import common.Messages;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class ClientConnection {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private Channel channel;
    private EventLoopGroup group;

    private final String host;
    private final int port;
    private final MessageHandler messageHandler = new MessageHandler();

    public ClientConnection(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public ClientConnection connect() {
        // Configure the client.
        group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();

                            pipeline.addLast("decoder", new CommandDecoder(Messages.registeredMessages));
                            pipeline.addLast("encoder", new CommandEncoder(Messages.registeredMessages));
                            pipeline.addLast("messageHandler", messageHandler);
                        }
                    });
            channel = bootstrap.connect(new InetSocketAddress(host, port)).syncUninterruptibly().channel();
        } catch (Exception e) {
            log.error(e.toString(), e);
            group.shutdownGracefully().syncUninterruptibly();
        }
        return this;
    }

    public void send(Object message) {
        channel.writeAndFlush(message);
    }

    public <RESPONSE> RESPONSE request(Object message, Class<RESPONSE> responseClass) {
        channel.writeAndFlush(message);
        Object response = null;
        try {
            response = incomingMessages().poll(2000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ignored) {
        }
        Assertions.assertNotNull(response);
        Assertions.assertSame(response.getClass(), responseClass);
        return (RESPONSE) response;
    }

    public boolean isActive() {
        return channel!= null && channel.isActive();
    }

    public BlockingQueue<Object> incomingMessages() {
        return messageHandler.incomingMessages;
    }

    public void disconnect() {
        if (channel.isActive()) {
            channel.disconnect().syncUninterruptibly();
            group.shutdownGracefully().syncUninterruptibly();
        }
    }

}
