/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.patcher;

import java.util.List;

import kotlin.jvm.functions.Function2;

@Deprecated
@SuppressWarnings("DeprecatedIsStillUsed")
public interface PrePatchFunction extends Function2<Object, List<Object>, PrePatchRes> {}
