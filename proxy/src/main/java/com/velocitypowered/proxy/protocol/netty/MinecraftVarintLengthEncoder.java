/*
 * Copyright (C) 2018-2023 Velocity Contributors
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.velocitypowered.proxy.protocol.netty;

import com.velocitypowered.natives.encryption.JavaVelocityCipher;
import com.velocitypowered.natives.util.Natives;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.netty.data.UncompressedPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Handler for appending a length for Minecraft packets.
 */
@ChannelHandler.Sharable
public class MinecraftVarintLengthEncoder extends MessageToByteEncoder<UncompressedPacket> {

  public static final MinecraftVarintLengthEncoder INSTANCE = new MinecraftVarintLengthEncoder();
  public static final boolean IS_JAVA_CIPHER = Natives.cipher.get() == JavaVelocityCipher.FACTORY;

  private MinecraftVarintLengthEncoder() {
  }

  @Override
  protected void encode(ChannelHandlerContext ctx, UncompressedPacket msg, ByteBuf out)
      throws Exception {
    ProtocolUtils.writeVarInt(out, msg.getPacketBuf().readableBytes());
    out.writeBytes(msg.getPacketBuf());
    msg.getPacketBuf().release();
  }

  @Override
  protected ByteBuf allocateBuffer(ChannelHandlerContext ctx, UncompressedPacket msg,
                                   boolean preferDirect) throws Exception {
    int anticipatedRequiredCapacity = ProtocolUtils.varIntBytes(msg.getPacketBuf().readableBytes())
        + msg.getPacketBuf().readableBytes();
    return IS_JAVA_CIPHER
        ? ctx.alloc().heapBuffer(anticipatedRequiredCapacity)
        : ctx.alloc().directBuffer(anticipatedRequiredCapacity);
  }
}
