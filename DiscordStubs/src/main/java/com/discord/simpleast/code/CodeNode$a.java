package com.discord.simpleast.code;

import com.discord.simpleast.core.node.Node;

import java.util.List;

import kotlin.jvm.internal.DefaultConstructorMarker;

// (abstract) Content
// https://github.com/discord/SimpleAST/blob/master/simpleast-core/src/main/java/com/discord/simpleast/code/CodeNode.kt#L24-L27
@SuppressWarnings("unused")
public abstract class CodeNode$a {
    public final String a;

    // Parsed
    public static final class a<RC> extends CodeNode$a {
        public final List<Node<RC>> b;

        public a(String raw, List<Node<RC>> children) {
            super(raw, null);
            b = children;
        }
    }

    // Raw
    public static final class b<RC> extends CodeNode$a {
        public b(String body) {
            super(body, null);
        }
    }

    public CodeNode$a(String body, DefaultConstructorMarker defaultConstructorMarker) {
        a = body;
    }
}
