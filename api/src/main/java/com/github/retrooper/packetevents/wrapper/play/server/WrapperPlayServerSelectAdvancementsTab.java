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

package com.github.retrooper.packetevents.wrapper.play.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jetbrains.annotations.Nullable;

public class WrapperPlayServerSelectAdvancementsTab extends PacketWrapper<WrapperPlayServerSelectAdvancementsTab> {
    private @Nullable ResourceLocation identifier;

    public WrapperPlayServerSelectAdvancementsTab(PacketSendEvent event) {
        super(event);
    }

    public WrapperPlayServerSelectAdvancementsTab(@Nullable ResourceLocation identifier) {
        super(PacketType.Play.Server.SELECT_ADVANCEMENTS_TAB);
        this.identifier = identifier;
    }

    @Override
    public void read() {
        identifier = readOptional(PacketWrapper::readIdentifier);
    }

    @Override
    public void write() {
        writeOptional(identifier, PacketWrapper::writeIdentifier);
    }

    @Override
    public void copy(WrapperPlayServerSelectAdvancementsTab wrapper) {
        this.identifier = wrapper.identifier;
    }

    public @Nullable ResourceLocation getIdentifier() {
        return identifier;
    }

    public void setIdentifier(@Nullable ResourceLocation identifier) {
        this.identifier = identifier;
    }
}
