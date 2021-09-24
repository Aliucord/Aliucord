package com.aliucord.annotations;

import java.lang.annotation.*;

/**
 * Annotates the entrypoint of a plugin, used by manifest.json generation
 */
@Target(ElementType.TYPE)
public @interface AliucordPlugin {
}
