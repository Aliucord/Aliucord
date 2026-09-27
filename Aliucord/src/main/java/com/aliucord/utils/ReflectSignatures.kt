/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2026 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.utils

import java.lang.reflect.*

/**
* Class for holding signature of a Method
*
* @property clazz The declaring Class
* @property name The name of the Method
* @param argTypes The list of types of the arguments
* @propery argTypes backing field for [argTypes]
*/
class MethodSignature(val clazz: Class<*>, val name: String, vararg argTypes: Class<*>) {
	val argTypes = argTypes

    /**
    * Secondary constructor which extracts signature from existing Method
    *
    * @param m The Method whose signature is to be extracted
    */
	constructor(m: Method): this(m.declaringClass, m.name, *m.parameterTypes)

    /**
    * Provides reference to the Method described by this signature
    *
    * @return The Method described by this signature
    **/
	fun get(): Method {
		return clazz.getDeclaredMethod(name, *argTypes)
	}
}

/**
* Class for holding signature of a Field
*
* @property clazz The declaring Class
* @property name The name of the Field
*/
class FieldSignature(val clazz: Class<*>, val name: String) {

    /**
    * Secondary constructor which extracts signature from existing Field
    *
    * @param f The Field whose signature is to be extracted
    */
	constructor(f: Field): this(f.declaringClass, f.name)

    /**
    * Provides reference to the Field described by this signature
    *
    * @return The Field described by this signature
    **/
	fun get(): Field {
		return clazz.getDeclaredField(name)
	}
}

/**
* Class for holding signature of a Constructor
*
* @property clazz The declaring Class
* @param argTypes The list of types of the arguments
* @propery argTypes backing field for [argTypes]
*/
class ConstructorSignature(val clazz: Class<*>, vararg argTypes: Class<*>) {
	val argTypes = argTypes

	/**
    * Secondary constructor which extracts signature from existing Constructor
    *
    * @param c The Constructor whose signature is to be extracted
    */
	constructor(c: Constructor<*>): this(c.declaringClass, *c.parameterTypes)
	
    /**
    * Provides reference to the Constructor described by this signature
    *
    * @return The Constructor described by this signature
    **/
	fun get(): Constructor<*> {
		return clazz.getDeclaredConstructor(*argTypes)
	}
}

/**
* Extension field that holds signature of a Method
*/
val Method.signature: MethodSignature
    get() {
        return MethodSignature(declaringClass, name, *parameterTypes)
    }

/**
* Extension field that holds signature of a Field
*/
val Field.signature: FieldSignature
    get() {
        return FieldSignature(declaringClass, name)
    }

/**
* Extension field that holds signature of a Constructor
*/
val Constructor<*>.signature: ConstructorSignature
    get() {
        return ConstructorSignature(declaringClass, *parameterTypes)
    }
