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

package io.github.retrooper.packetevents.injector;

import io.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.event.impl.PlayerEjectEvent;
import io.github.retrooper.packetevents.event.impl.PlayerInjectEvent;
import io.github.retrooper.packetevents.injector.legacy.early.EarlyChannelInjectorLegacy;
import io.github.retrooper.packetevents.injector.legacy.late.LateChannelInjectorLegacy;
import io.github.retrooper.packetevents.injector.modern.early.EarlyChannelInjectorModern;
import io.github.retrooper.packetevents.injector.modern.late.LateChannelInjectorModern;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import net.abyssdev.spoof.api.SpoofAPI;
import org.bukkit.entity.Player;

public class GlobalChannelInjector {
    private ChannelInjector injector;
    private boolean spoof;

    public void load() {
        boolean legacy = NMSUtils.legacyNettyImportMode;
        if (!PacketEvents.get().getSettings().shouldUseCompatibilityInjector()) {
            injector = legacy ? new EarlyChannelInjectorLegacy() : new EarlyChannelInjectorModern();
        } else {
            injector = legacy ? new LateChannelInjectorLegacy() : new LateChannelInjectorModern();
        }
        spoof = PacketEvents.get().getPlugin().getServer().getPluginManager().isPluginEnabled("ExploitPatcher");
    }

    public boolean isBound() {
        return injector.isBound();
    }

    public void inject() {
        try {
            //Try inject...
            injector.inject();
        } catch (Exception ex) {
            //Failed to inject! Let us revert to the compatibility injector and re-inject.
            if (injector instanceof EarlyInjector) {
                PacketEvents.get().getSettings().compatInjector(true);
                load();
                injector.inject();
                PacketEvents.get().getPlugin().getLogger().warning("[packetevents] Failed to inject with the Early Injector. Reverting to the Compatibility/Late Injector... This is just a warning, but please report this!");
                ex.printStackTrace();
            }
        }
    }

    public void eject() {
        injector.eject();
    }

    public void injectPlayer(Player player) {
        if (this.spoof && SpoofAPI.getPlayerProvider().hasSpoofedChannel(player)) {
            return;
        }
        PlayerInjectEvent injectEvent = new PlayerInjectEvent(player);
        PacketEvents.get().callEvent(injectEvent);
        if (!injectEvent.isCancelled()) {
            injector.injectPlayer(player);
        }
    }

    public void ejectPlayer(Player player) {
        PlayerEjectEvent ejectEvent = new PlayerEjectEvent(player);
        PacketEvents.get().callEvent(ejectEvent);
        if (!ejectEvent.isCancelled()) {
            injector.ejectPlayer(player);
        }
    }

    public boolean hasInjected(Player player) {
        return injector.hasInjected(player);
    }

    public void writePacket(Object ch, Object rawNMSPacket) {
        injector.writePacket(ch, rawNMSPacket);
    }

    public void flushPackets(Object ch) {
        injector.flushPackets(ch);
    }

    public void sendPacket(Object ch, Object rawNMSPacket) {
        injector.sendPacket(ch, rawNMSPacket);
    }
}
