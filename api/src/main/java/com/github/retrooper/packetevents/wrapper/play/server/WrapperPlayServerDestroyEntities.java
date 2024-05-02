/*
 * This file is part of packetevents - https://github.com/retrooper/packetevents
 * Copyright (C) 2022 retrooper and contributors
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

package com.github.retrooper.packetevents.wrapper.play.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperPlayServerDestroyEntities extends PacketWrapper<WrapperPlayServerDestroyEntities> {
    private int[] entityIDs;

    public WrapperPlayServerDestroyEntities(PacketSendEvent event) {
        super(event);
    }

    public WrapperPlayServerDestroyEntities(int... entityIDs) {
        super(PacketType.Play.Server.DESTROY_ENTITIES);
        this.entityIDs = entityIDs;
    }

    public WrapperPlayServerDestroyEntities(int entityID) {
        super(PacketType.Play.Server.DESTROY_ENTITIES);
        this.entityIDs = new int[]{entityID};
    }

    @Override
    public void read() {
        int entityIDCount = readVarInt();
        entityIDs = new int[entityIDCount];
        for (int i = 0; i < entityIDCount; i++) {
            entityIDs[i] = readVarInt();
        }
    }

    @Override
    public void write() {
        writeVarInt(entityIDs.length);
        for (int entityID : entityIDs) {
            writeVarInt(entityID);
        }
    }

    @Override
    public void copy(WrapperPlayServerDestroyEntities wrapper) {
        entityIDs = wrapper.entityIDs;
    }

    public int[] getEntityIds() {
        return entityIDs;
    }

    public void setEntityIds(int[] entityIDs) {
        this.entityIDs = entityIDs;
    }
}
