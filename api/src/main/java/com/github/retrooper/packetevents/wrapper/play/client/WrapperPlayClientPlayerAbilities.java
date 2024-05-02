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

package com.github.retrooper.packetevents.wrapper.play.client;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

import java.util.Optional;

public class WrapperPlayClientPlayerAbilities extends PacketWrapper<WrapperPlayClientPlayerAbilities> {
    private boolean flying;
    private Optional<Boolean> godMode;
    private Optional<Boolean> flightAllowed;
    private Optional<Boolean> creativeMode;
    private Optional<Float> flySpeed;
    private Optional<Float> walkSpeed;

    public WrapperPlayClientPlayerAbilities(PacketReceiveEvent event) {
        super(event);
    }

    public WrapperPlayClientPlayerAbilities(boolean flying, Optional<Boolean> godMode, Optional<Boolean> flightAllowed,
                                            Optional<Boolean> creativeMode,
                                            Optional<Float> flySpeed, Optional<Float> walkSpeed) {
        super(PacketType.Play.Client.PLAYER_ABILITIES);
        this.flying = flying;
        this.godMode = godMode;
        this.flightAllowed = flightAllowed;
        this.creativeMode = creativeMode;
        this.flySpeed = flySpeed;
        this.walkSpeed = walkSpeed;
    }

    public WrapperPlayClientPlayerAbilities(boolean flying) {
        this(flying, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }

    @Override
    public void read() {
        byte mask = readByte();
        flying = (mask & 0x02) != 0;
        godMode = Optional.empty();
        flightAllowed = Optional.empty();
        creativeMode = Optional.empty();
        flySpeed = Optional.empty();
        walkSpeed = Optional.empty();
    }

    @Override
    public void write() {
        byte mask = (byte) (flying ? 0x02 : 0x00);
        writeByte(mask);
    }

    @Override
    public void copy(WrapperPlayClientPlayerAbilities wrapper) {
        godMode = wrapper.godMode;
        flying = wrapper.flying;
        flightAllowed = wrapper.flightAllowed;
        creativeMode = wrapper.creativeMode;
        flySpeed = wrapper.flySpeed;
        walkSpeed = wrapper.walkSpeed;
    }

    public boolean isFlying() {
        return flying;
    }

    public void setFlying(boolean flying) {
        this.flying = flying;
    }

    public Optional<Boolean> isInGodMode() {
        return godMode;
    }

    public void setInGodMode(Optional<Boolean> godMode) {
        this.godMode = godMode;
    }

    public Optional<Boolean> isFlightAllowed() {
        return flightAllowed;
    }

    public void setFlightAllowed(Optional<Boolean> flightAllowed) {
        this.flightAllowed = flightAllowed;
    }

    public Optional<Boolean> isInCreativeMode() {
        return creativeMode;
    }

    public void setCreativeMode(Optional<Boolean> creativeMode) {
        this.creativeMode = creativeMode;
    }

    public Optional<Float> getFlySpeed() {
        return flySpeed;
    }

    public void setFlySpeed(Optional<Float> flySpeed) {
        this.flySpeed = flySpeed;
    }

    public Optional<Float> getWalkSpeed() {
        return walkSpeed;
    }

    public void setWalkSpeed(Optional<Float> walkSpeed) {
        this.walkSpeed = walkSpeed;
    }
}
