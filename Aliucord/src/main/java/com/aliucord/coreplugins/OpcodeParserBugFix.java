package com.aliucord.coreplugins;

import android.content.Context;

import com.aliucord.entities.Plugin;
import com.aliucord.patcher.Patcher;
import com.aliucord.patcher.PreHook;
import com.aliucord.utils.ReflectUtils;
import com.discord.gateway.GatewaySocket;
import com.discord.gateway.io.IncomingParser;
import com.discord.gateway.opcodes.Opcode;
import com.discord.models.domain.Model;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class OpcodeParserBugFix extends Plugin {
    public OpcodeParserBugFix() {
        super(new Manifest("OpcodeParserBugFix"));
    }

    private static int indexOfBytes(byte[] outerArray, byte[] smallerArray) {
        for (int i = 0; i < outerArray.length - smallerArray.length + 1; ++i) {
            boolean found = true;
            for (int j = 0; j < smallerArray.length; ++j) {
                if (outerArray[i + j] != smallerArray[j]) {
                    found = false;
                    break;
                }
            }
            if (found) return i;
        }
        return -1;
    }

    @Override
    public void start(Context context) throws Throwable {
        Patcher.addPatch(IncomingParser.class, "assignField", new Class<?>[]{ Model.JsonReader.class }, new PreHook(param -> {
            try {
                if (ReflectUtils.getField(param.thisObject, "opcode") != null)
                    return;

                var rootReader = ((GatewaySocket.SizeRecordingInputStreamReader) ((Model.JsonReader) param.args[0]).in.k).getSource();
                var decoder = ReflectUtils.getField(rootReader, "sd"); // StreamDecoder
                var in = (ByteArrayInputStream) ReflectUtils.getField(decoder, "in");

                var srcBytes = (byte[]) ReflectUtils.getField(in, "buf");
                var findBytes = "\"op\":".getBytes(StandardCharsets.UTF_8);
                var findBytesIdx = indexOfBytes(srcBytes, findBytes);
                if (findBytesIdx < 0) return;

                StringBuilder sb = new StringBuilder(2);
                char c;
                int i = findBytesIdx + findBytes.length;
                while (i < srcBytes.length && Character.isDigit((c = (char) srcBytes[i++]))) {
                    sb.append(c);
                }

                int opcode = Integer.parseInt(sb.toString());
                Opcode opcodeEnum = Opcode.Companion.fromApiInt(opcode);

                ReflectUtils.setField(param.thisObject, "opcode", opcodeEnum);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
    }

    @Override
    public void stop(Context context) {}
}
