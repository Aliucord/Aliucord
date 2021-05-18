package com.aliucord.patcher;

import java.util.List;

import kotlin.jvm.functions.Function2;

public interface PrePatchFunction extends Function2<Object, List<Object>, PrePatchRes> {}
