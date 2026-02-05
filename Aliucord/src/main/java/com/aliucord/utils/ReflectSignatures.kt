package com.aliucord.utils

import java.lang.reflect.*

class MethodSignature(
	val clazz: Class<*>,
	val name: String,
	vararg argTypes: Class<*>
){
	val argTypes = argTypes;

	constructor(m: Method):
		this(m.declaringClass, m.name, *m.parameterTypes)
	;
	fun get(): Method{
		return clazz.getDeclaredMethod(this.name, *this.argTypes);
	};
};
class FieldSignature(
	val clazz: Class<*>,
	val name: String
){
	constructor(f: Field):
		this(f.declaringClass, f.name)
	;
	fun get(): Field{
		return clazz.getDeclaredField(this.name);
	};
};
class ConstructorSignature(
	val clazz: Class<*>,
	vararg argTypes: Class<*>
){
	val argTypes = argTypes;

	constructor(c: Constructor<*>):
		this(c.declaringClass, *c.parameterTypes)
	;
	fun get(): Constructor<*>{
		return clazz.getDeclaredConstructor(*this.argTypes);
	};
};

val Method.signature get() = MethodSignature(
	this.declaringClass,
	this.name,
	*this.parameterTypes
);
val Field.signature get() = FieldSignature(
	this.declaringClass,
	this.name
);
val Constructor<*>.signature get() = ConstructorSignature(
	this.declaringClass,
	*this.parameterTypes
);