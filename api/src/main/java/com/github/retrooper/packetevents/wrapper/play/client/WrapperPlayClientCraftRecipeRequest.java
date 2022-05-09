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

package com.github.retrooper.packetevents.wrapper.play.client;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperPlayClientCraftRecipeRequest extends PacketWrapper<WrapperPlayClientCraftRecipeRequest> {
  private int windowId;
  private Object recipe;
  private boolean makeAll;

  public WrapperPlayClientCraftRecipeRequest(PacketReceiveEvent event) {
    super(event);
  }

  public WrapperPlayClientCraftRecipeRequest(int windowId, Object recipe, boolean makeAll) {
    super(PacketType.Play.Client.CRAFT_RECIPE_REQUEST);
    this.windowId = windowId;
    this.recipe = recipe;
    this.makeAll = makeAll;
  }

  @Override
  public void read() {
    if (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_12)) {
      this.windowId = readByte();
      if (serverVersion.isNewerThan(ServerVersion.V_1_12_2)) {
        this.recipe = readString();
      } else {
        this.recipe = readVarInt();
      }
      this.makeAll = readBoolean();
    }
  }

  @Override
  public void copy(WrapperPlayClientCraftRecipeRequest wrapper) {
    this.windowId = wrapper.windowId;
    this.recipe = wrapper.recipe;
    this.makeAll = wrapper.makeAll;
  }

  @Override
  public void write() {
    if (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_12)) {
      writeByte(this.windowId);
      if (serverVersion.isNewerThan(ServerVersion.V_1_12_2)) {
        writeString((String) this.recipe);
      } else {
        writeVarInt((Integer) this.recipe);
      }
      writeBoolean(this.makeAll);
    }
  }

  public int getWindowId() {
    return this.windowId;
  }

  public void setWindowId(int windowId) {
    this.windowId = windowId;
  }

  public Object getRecipe() {
    return recipe;
  }

  public void setRecipe(Object recipe) {
    this.recipe = recipe;
  }

  public boolean isMakeAll() {
    return makeAll;
  }

  public void setMakeAll(boolean makeAll) {
    this.makeAll = makeAll;
  }
}