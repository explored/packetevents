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

package com.github.retrooper.packetevents.wrapper.login.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperLoginServerSetCompression extends PacketWrapper<WrapperLoginServerSetCompression> {
    private int threshold;

    public WrapperLoginServerSetCompression(PacketSendEvent event) {
        super(event);
    }

    public WrapperLoginServerSetCompression(int threshold) {
        super(PacketType.Login.Server.SET_COMPRESSION);
        this.threshold = threshold;
    }

    @Override
    public void read() {
        this.threshold = readVarInt();
    }

    @Override
    public void copy(WrapperLoginServerSetCompression wrapper) {
        this.threshold = wrapper.threshold;
    }

    @Override
    public void write() {
        writeVarInt(threshold);
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }
}