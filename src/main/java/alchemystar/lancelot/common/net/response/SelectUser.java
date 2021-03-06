/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.lancelot.common.net.response;

import alchemystar.lancelot.common.net.handler.frontend.FrontendConnection;
import alchemystar.lancelot.common.net.proto.mysql.EOFPacket;
import alchemystar.lancelot.common.net.proto.mysql.ErrorPacket;
import alchemystar.lancelot.common.net.proto.mysql.FieldPacket;
import alchemystar.lancelot.common.net.proto.mysql.ResultSetHeaderPacket;
import alchemystar.lancelot.common.net.proto.mysql.RowDataPacket;
import alchemystar.lancelot.common.net.proto.util.Fields;
import alchemystar.lancelot.common.net.proto.util.PacketUtil;
import alchemystar.lancelot.common.net.proto.util.StringUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * SelectUser
 *
 * @Author lizhuyang
 */
public class SelectUser {

    private static final int FIELD_COUNT = 1;
    private static final ResultSetHeaderPacket header = PacketUtil.getHeader(FIELD_COUNT);
    private static final FieldPacket[] fields = new FieldPacket[FIELD_COUNT];
    private static final EOFPacket eof = new EOFPacket();
    private static final ErrorPacket error = PacketUtil.getShutdown();

    static {
        int i = 0;
        byte packetId = 0;
        header.packetId = ++packetId;
        fields[i] = PacketUtil.getField("USER()", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;
        eof.packetId = ++packetId;
    }

    public static void response(FrontendConnection c) {
        ChannelHandlerContext ctx = c.getCtx();
        ByteBuf buffer = ctx.alloc().buffer();
        buffer = header.writeBuf(buffer, ctx);
        for (FieldPacket field : fields) {
            buffer = field.writeBuf(buffer, ctx);
        }
        buffer = eof.writeBuf(buffer, ctx);
        byte packetId = eof.packetId;
        RowDataPacket row = new RowDataPacket(FIELD_COUNT);
        row.add(getUser(c));
        row.packetId = ++packetId;
        buffer = row.writeBuf(buffer, ctx);
        EOFPacket lastEof = new EOFPacket();
        lastEof.packetId = ++packetId;
        buffer = lastEof.writeBuf(buffer, ctx);
        ctx.writeAndFlush(buffer);

    }

    private static byte[] getUser(FrontendConnection c) {
        StringBuilder sb = new StringBuilder();
        sb.append(c.getUser()).append('@').append(c.getHost());
        return StringUtil.encode(sb.toString(), c.getCharset());
    }

}