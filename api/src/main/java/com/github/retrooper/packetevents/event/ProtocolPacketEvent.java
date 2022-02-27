/*
 * This file is part of packetevents - https://github.com/retrooper/packetevents
 * Copyright (C) 2021 retrooper and contributors
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

package com.github.retrooper.packetevents.event;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.exception.PacketProcessException;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.netty.buffer.ByteBufHelper;
import com.github.retrooper.packetevents.netty.channel.ChannelHelper;
import com.github.retrooper.packetevents.protocol.ConnectionState;
import com.github.retrooper.packetevents.protocol.PacketSide;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public abstract class ProtocolPacketEvent<T> extends PacketEvent implements PlayerEvent<T>, CancellableEvent {
    private final Object channel;
    private final ConnectionState connectionState;
    private final User user;
    private final T player;
    private Object byteBuf;
    private final int packetID;
    private final PacketTypeCommon packetType;
    private ServerVersion serverVersion;
    private boolean cancel;
    private PacketWrapper<?> lastUsedWrapper;
    private List<Runnable> postTasks = null;
    private boolean cloned;

    public ProtocolPacketEvent(PacketSide packetSide, Object channel,
                               User user, T player, Object byteBuf) throws PacketProcessException {
        this.channel = channel;
        this.user = user;
        this.player = player;

        this.serverVersion = PacketEvents.getAPI().getServerManager().getVersion();

        this.byteBuf = byteBuf;
        int size = ByteBufHelper.readableBytes(byteBuf);
        try {
            this.packetID = ByteBufHelper.readVarInt(byteBuf);
        } catch (Exception e) {
            throw new PacketProcessException(e,
                    PacketProcessException.PacketProcessExceptionReason.PACKET_ID,
                    packetSide, size, null);
        }
        this.packetType = PacketType.getById(packetSide, user.getConnectionState(), this.serverVersion, packetID);

        this.connectionState = user.getConnectionState();
    }

    public ProtocolPacketEvent(int packetID, PacketTypeCommon packetType, ServerVersion serverVersion, Object channel,
                               User user, T player, Object byteBuf) {
        this.channel = channel;
        this.user = user;
        this.player = player;
        this.serverVersion = serverVersion;
        this.byteBuf = byteBuf;
        this.packetID = packetID;
        this.packetType = packetType;
        this.connectionState = user.getConnectionState();
        cloned = true;
    }

    public boolean isClone() {
        return cloned;
    }

    public Object getChannel() {
        return channel;
    }

    public InetSocketAddress getSocketAddress() {
        return (InetSocketAddress) ChannelHelper.remoteAddress(channel);
    }

    @NotNull
    public User getUser() {
        return user;
    }

    @Override
    public T getPlayer() {
        return player;
    }

    public ConnectionState getConnectionState() {
        return connectionState;
    }

    //TODO Possibly put clientversion inside User class and remove here
    @Deprecated
    public ClientVersion getClientVersion() {
        return user.getClientVersion();
    }

    @Deprecated
    public void setClientVersion(@NotNull ClientVersion clientVersion) {
        PacketEvents.getAPI().getLogManager().debug("Setting client version with deprecated method " + clientVersion.getReleaseName());
        user.setClientVersion(clientVersion);
    }

    public ServerVersion getServerVersion() {
        return serverVersion;
    }

    public void setServerVersion(@NotNull ServerVersion serverVersion) {
        this.serverVersion = serverVersion;
    }

    public Object getByteBuf() {
        return byteBuf;
    }

    public void setByteBuf(Object byteBuf) {
        this.byteBuf = byteBuf;
    }

    @Deprecated
    public int getPacketId() {
        return packetID;
    }

    public PacketTypeCommon getPacketType() {
        return packetType;
    }

    @Deprecated
    public String getPacketName() {
        return ((Enum<?>) packetType).name();
    }

    @Override
    public boolean isCancelled() {
        return this.cancel;
    }

    @Override
    public void setCancelled(boolean val) {
        this.cancel = val;
    }

    @Nullable
    public PacketWrapper<?> getLastUsedWrapper() {
        return lastUsedWrapper;
    }

    public void setLastUsedWrapper(@Nullable PacketWrapper<?> lastUsedWrapper) {
        this.lastUsedWrapper = lastUsedWrapper;
    }

    public List<Runnable> getPostTasks() {
        if (postTasks == null) {
            postTasks = new ArrayList<>();
        }
        return postTasks;
    }

    public boolean hasPostTasks() {
        return postTasks != null && !postTasks.isEmpty();
    }

    @Override
    public ProtocolPacketEvent<?> clone() {
        return this instanceof PacketReceiveEvent ? ((PacketReceiveEvent) this).clone()
                : ((PacketSendEvent) this).clone();
    }

    public void cleanUp() {
        if (isClone()) {
            ByteBufHelper.release(byteBuf);
        }
    }
}
