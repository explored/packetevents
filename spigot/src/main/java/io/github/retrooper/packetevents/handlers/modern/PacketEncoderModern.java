/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
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

package io.github.retrooper.packetevents.handlers.modern;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.exception.PacketProcessException;
import com.github.retrooper.packetevents.netty.buffer.ByteBufHelper;
import com.github.retrooper.packetevents.protocol.ConnectionState;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.util.EventCreationUtil;
import com.github.retrooper.packetevents.util.ExceptionUtil;
import io.github.retrooper.packetevents.utils.SpigotReflectionUtil;
import io.github.retrooper.packetevents.utils.dependencies.viaversion.CustomPipelineUtil;
import io.github.retrooper.packetevents.utils.dependencies.viaversion.ViaVersionUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

@ChannelHandler.Sharable
public class PacketEncoderModern extends MessageToByteEncoder<Object> {
    public User user;
    public volatile Player player;
    public MessageToByteEncoder<?> wrappedEncoder;
    public MessageToByteEncoder<?> vanillaEncoder;
    public List<MessageToByteEncoder<Object>> encoders = new ArrayList<>();

    public PacketEncoderModern(User user) {
        this.user = user;
    }

    public void writeMessage(Object ctx, Object msg) throws Exception {
        write((ChannelHandlerContext) ctx, msg, ((ChannelHandlerContext) ctx).newPromise());
    }

    public void read(ChannelHandlerContext ctx, ByteBuf buffer) throws Exception {
        int preProcessIndex = buffer.readerIndex();
        PacketSendEvent packetSendEvent = EventCreationUtil.createSendEvent(ctx.channel(), user, player, buffer);
        int processIndex = buffer.readerIndex();
        PacketEvents.getAPI().getEventManager().callEvent(packetSendEvent, () -> {
            buffer.readerIndex(processIndex);
        });
        if (!packetSendEvent.isCancelled()) {
            if (packetSendEvent.getLastUsedWrapper() != null) {
                ByteBufHelper.clear(packetSendEvent.getByteBuf());
                packetSendEvent.getLastUsedWrapper().writeVarInt(packetSendEvent.getPacketId());
                packetSendEvent.getLastUsedWrapper().writeData();
            }
            buffer.readerIndex(preProcessIndex);
        } else {
            //Make the buffer unreadable for the next handlers
            buffer.clear();
        }
        if (packetSendEvent.hasPostTasks()) {
            for (Runnable task : packetSendEvent.getPostTasks()) {
                task.run();
            }
        }
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object o, ByteBuf out) throws Exception {
        ByteBuf transformed = ctx.alloc().buffer();
        try {
            if (!(o instanceof ByteBuf)) {
                //Convert NMS object to bytes, so we can process it right away.
                if (vanillaEncoder == null) return;
                CustomPipelineUtil.callEncode(vanillaEncoder, ctx, o, transformed);
            } else {
                ByteBuf in = (ByteBuf) o;
                if (in.readableBytes() == 0) return;
                transformed.writeBytes(in);
            }
            read(ctx, transformed);
            //Our packetevents unfortunately cancelled this packet, so we won't pass it on.
            if (!transformed.isReadable()) {
                return;
            }
            for (MessageToByteEncoder<Object> encoder : encoders) {
                CustomPipelineUtil.callEncode(encoder, ctx, transformed, out);
                if (out.isReadable()) {
                    transformed.clear().writeBytes(out);
                    out.clear();
                } else {
                    //This encoder decided to discard this packet.
                    break;
                }
            }

            if (wrappedEncoder != vanillaEncoder) {
                CustomPipelineUtil.callEncode(wrappedEncoder, ctx, transformed, out);
            } else {
                out.writeBytes(transformed);
            }
        } finally {
            //Release to prevent memory leaks
            transformed.release();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (ViaVersionUtil.isAvailable()
                && ExceptionUtil.isException(cause, InvocationTargetException.class)) {
          return;
        }
        super.exceptionCaught(ctx, cause);
        //Check if the minecraft server will already print our exception for us.
        if (ExceptionUtil.isException(cause, PacketProcessException.class)
                && !SpigotReflectionUtil.isMinecraftServerInstanceDebugging()
                && (user == null || user.getConnectionState() != ConnectionState.HANDSHAKING)) {
            cause.printStackTrace();
        }
    }
}
