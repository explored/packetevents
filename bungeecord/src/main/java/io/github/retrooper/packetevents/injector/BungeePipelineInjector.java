/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2022 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.retrooper.packetevents.injector;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.injector.ChannelInjector;
import com.github.retrooper.packetevents.protocol.ConnectionState;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.util.reflection.Reflection;
import com.github.retrooper.packetevents.util.reflection.ReflectionObject;
import io.github.retrooper.packetevents.handlers.PacketDecoder;
import io.github.retrooper.packetevents.handlers.PacketEncoder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Thanks to ViaVersion for helping us design this injector.
public class BungeePipelineInjector implements ChannelInjector {
    private static final Field LISTENERS_FIELD;
    private static final Field CONNECTIONS_FIELD;
    private static final Class<?> CHANNEL_WRAPPER_CLASS;

    static {
        LISTENERS_FIELD = Reflection.getField(ProxyServer.getInstance().getClass(), "listeners");
        LISTENERS_FIELD.setAccessible(true);
        CONNECTIONS_FIELD = Reflection.getField(ProxyServer.getInstance().getClass(), "connections");
        CONNECTIONS_FIELD.setAccessible(true);
        CHANNEL_WRAPPER_CLASS = Reflection.getClassByNameWithoutException("net.md_5.bungee.netty.ChannelWrapper");
    }

    private final List<Channel> injectedChannels = new ArrayList<>();

    @Override
    public @Nullable ConnectionState getConnectionState(Object ch) {
        Channel channel = (Channel) ch;
        PacketDecoder decoder = (PacketDecoder) channel.pipeline().get(PacketEvents.DECODER_NAME);
        return decoder.user.getConnectionState();
    }

    @Override
    public void changeConnectionState(Object ch, @Nullable ConnectionState packetState) {
        Channel channel = (Channel) ch;
        PacketDecoder decoder = (PacketDecoder) channel.pipeline().get(PacketEvents.DECODER_NAME);
        decoder.user.setConnectionState(packetState);
    }

    public void injectChannel(Channel channel) {
        channel.pipeline().addFirst(PacketEvents.CONNECTION_NAME,
                new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) throws Exception {
                        Channel channel = (Channel) msg;
                        channel.pipeline().addLast(PacketEvents.SERVER_CHANNEL_HANDLER_NAME, new PreChannelInitializer());
                        super.channelRead(ctx, msg);
                    }
                });

        try {
            Map<?, ?> connectionsMap = (Map<?, ?>) CONNECTIONS_FIELD.get(ProxyServer.getInstance());
            for (Object connection : connectionsMap.values()) {
                ReflectionObject reflectUserConnection = new ReflectionObject(connection);
                Object channelWrapper = reflectUserConnection.readObject(0, CHANNEL_WRAPPER_CLASS);
                ReflectionObject reflectChannelWrapper = new ReflectionObject(channelWrapper);
                Channel connectionChannel = reflectChannelWrapper.readObject(0, Channel.class);
                if (connectionChannel != null &&
                        connectionChannel.localAddress().equals(channel.localAddress())) {
                    connectionChannel.close();
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        injectedChannels.add(channel);
    }

    @Override
    public void inject() {
        try {
            Set<Channel> listeners = (Set<Channel>) LISTENERS_FIELD.get(ProxyServer.getInstance());
            Set<Channel> wrapper = new SetWrapper<>(listeners, this::injectChannel);
            LISTENERS_FIELD.set(ProxyServer.getInstance(), wrapper);

            for (Channel channel : listeners) {
                injectChannel(channel);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void eject() {
        for (Channel channel : injectedChannels) {
            channel.pipeline().remove(PacketEvents.CONNECTION_NAME);
        }
    }

    @Override
    public void injectPlayer(Object p, @Nullable ConnectionState connectionState) {
        ProxiedPlayer player = (ProxiedPlayer) p;
        Channel channel = (Channel) PacketEvents.getAPI().getPlayerManager().getChannel(player);
        PacketDecoder decoder = (PacketDecoder) channel.pipeline().get(PacketEvents.DECODER_NAME);
        decoder.player = player;
        decoder.user.getProfile().setUUID(player.getUniqueId());
        decoder.user.getProfile().setName(player.getName());
        if (connectionState != null) {
            decoder.user.setConnectionState(connectionState);
        }
        PacketEncoder encoder = (PacketEncoder) channel.pipeline().get(PacketEvents.ENCODER_NAME);
        encoder.player = player;
    }

    @Override
    public void updateUser(Object ch, User user) {
        Channel channel = (Channel) ch;
        PacketDecoder decoder = (PacketDecoder) channel.pipeline().get(PacketEvents.DECODER_NAME);
        decoder.user = user;
        PacketEncoder encoder = (PacketEncoder) channel.pipeline().get(PacketEvents.ENCODER_NAME);
        encoder.user = user;
    }

    @Override
    public void ejectPlayer(Object player) {
        Channel channel = (Channel) PacketEvents.getAPI().getPlayerManager().getChannel(player);
        ServerConnectionInitializer.destroyChannel(channel);
    }

    @Override
    public boolean hasInjected(Object player) {
        Channel channel = (Channel) PacketEvents.getAPI().getPlayerManager().getChannel(player);
        PacketDecoder decoder = (PacketDecoder) channel.pipeline().get(PacketEvents.DECODER_NAME);
        PacketEncoder encoder = (PacketEncoder) channel.pipeline().get(PacketEvents.ENCODER_NAME);
        return decoder != null && encoder != null
                && decoder.player != null && encoder.player != null;
    }
}
