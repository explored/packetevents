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

package com.github.retrooper.packetevents.protocol.particle.data;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class ParticleBlockStateData extends ParticleData implements LegacyConvertible {
    private WrappedBlockState blockState;

    public ParticleBlockStateData(WrappedBlockState blockState) {
        this.blockState = blockState;
    }

    public WrappedBlockState getBlockState() {
        return blockState;
    }

    public void setBlockState(WrappedBlockState blockState) {
        this.blockState = blockState;
    }

    public static ParticleBlockStateData read(PacketWrapper<?> wrapper) {
        int blockID;
        blockID = wrapper.readVarInt();
        return new ParticleBlockStateData(WrappedBlockState.getByGlobalId(wrapper.getServerVersion()
                .toClientVersion(), blockID));
    }

    public static void write(PacketWrapper<?> wrapper, ParticleBlockStateData data) {
        wrapper.writeVarInt(data.getBlockState().getGlobalId());
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public LegacyParticleData toLegacy(ClientVersion version) {
        return LegacyParticleData.ofOne(blockState.getGlobalId());
    }

}
