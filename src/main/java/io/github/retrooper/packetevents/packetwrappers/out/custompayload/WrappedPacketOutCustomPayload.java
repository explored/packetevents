/*
 * MIT License
 *
 * Copyright (c) 2020 retrooper
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.retrooper.packetevents.packetwrappers.out.custompayload;

import io.github.retrooper.packetevents.packet.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.SendableWrapper;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.utils.bytebuf.ByteBufUtil;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

public class WrappedPacketOutCustomPayload extends WrappedPacket implements SendableWrapper {
    private static Class<?> packetClass;
    private static Constructor<?> constructor;
    private static Constructor<?> packetDataSerializerConstructor;
    private static Constructor<?> minecraftKeyConstructor;
    private static Class<?> byteBufClass;
    private static Class<?> packetDataSerializerClass;
    private static Class<?> minecraftKeyClass;
    private static int minecraftKeyIndexInClass;

    private static byte constructorMode = 0;

    public static void load() {
        packetClass = PacketTypeClasses.Server.CUSTOM_PAYLOAD;
        packetDataSerializerClass = NMSUtils.getNMSClassWithoutException("PacketDataSerializer");
        minecraftKeyClass = NMSUtils.getNMSClassWithoutException("MinecraftKey");

        try {
            byteBufClass = NMSUtils.getNettyClass("buffer.ByteBuf");
        } catch (ClassNotFoundException e) {

        }
        try {
            packetDataSerializerConstructor = packetDataSerializerClass.getConstructor(byteBufClass);
        } catch (NullPointerException e) {
            //Nothing
        } catch (NoSuchMethodException e) {
            //also nothing
        }

        try {
            if (minecraftKeyClass != null) {
                minecraftKeyConstructor = minecraftKeyClass.getConstructor(String.class);
            }
        } catch (NoSuchMethodException e) {
            //Nothing
        }

        //Constructors:

        //String, byte[]

        //String, PacketDataSerializer

        //MinecraftKey, PacketDataSerializer
        try {
            //1.7 constructor
            constructor = packetClass.getConstructor(String.class, byte[].class);
            constructorMode = 0;
        } catch (NoSuchMethodException e) {
            //That's fine, just a newer version
            try {
                constructor = packetClass.getConstructor(String.class, packetDataSerializerClass);
                constructorMode = 1;
            } catch (NoSuchMethodException e2) {
                //That's fine, just an even newer version
                try {
                    //Minecraft key exists

                    for (int i = 0; i < packetClass.getDeclaredFields().length; i++) {
                        Field f = packetClass.getDeclaredFields()[i];
                        if (!Modifier.isStatic(f.getModifiers())) {
                            minecraftKeyIndexInClass = i;
                            break;
                        }
                    }
                    constructor = packetClass.getConstructor(minecraftKeyClass, packetDataSerializerClass);
                    constructorMode = 2;
                } catch (NoSuchMethodException e3) {
                    throw new IllegalStateException("PacketEvents is unable to resolve the PacketPlayOutCustomPayload constructor.");
                }
            }
        }
    }

    private String tag;
    private byte[] data;

    public WrappedPacketOutCustomPayload(String tag, byte[] data) {
        this.tag = tag;
        this.data = data;
    }


    public WrappedPacketOutCustomPayload(Object packet) {
        super(packet);
    }

    @Override
    protected void setup() {
        switch (constructorMode) {
            case 0:
                this.tag = readString(0);
                this.data = readByteArray(0);
                break;
            case 1:
                this.tag = readString(0);
                Object dataSerializer = readObject(0, packetDataSerializerClass);
                WrappedPacket byteBufWrapper = new WrappedPacket(dataSerializer);

                Object byteBuf = byteBufWrapper.readObject(0, byteBufClass);

                this.data = ByteBufUtil.getBytes(byteBuf);
                break;
            case 2:
                Object minecraftKey = readObject(minecraftKeyIndexInClass, minecraftKeyClass);
                WrappedPacket minecraftKeyWrapper = new WrappedPacket(minecraftKey);
                this.tag = minecraftKeyWrapper.readString(1);
                Object dataSerializer2 = readObject(0, packetDataSerializerClass);
                WrappedPacket byteBuf2Wrapper = new WrappedPacket(dataSerializer2);
                Object byteBuf2 = byteBuf2Wrapper.readObject(0, byteBufClass);
                this.data = ByteBufUtil.getBytes(byteBuf2);
                break;
        }
    }

    public String getTag() {
        return tag;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public Object asNMSPacket() {
        Object byteBufObject = ByteBufUtil.copiedBuffer(data);
        switch (constructorMode) {
            case 0:
                try {
                    return constructor.newInstance(tag, data);
                } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            case 1:
                try {
                    Object dataSerializer = packetDataSerializerConstructor.newInstance(byteBufObject);
                    return constructor.newInstance(tag, dataSerializer);
                } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            case 2:

                try {
                    Object minecraftKey = minecraftKeyConstructor.newInstance(tag);
                    Object dataSerializer = packetDataSerializerConstructor.newInstance(byteBufObject);
                    return constructor.newInstance(minecraftKey, dataSerializer);
                } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
        }
        return null;
    }
}
