package com.aliucord.patcher;

import java.util.ArrayList;

public interface PatchFunction<T> {
    T run(Object _this, ArrayList<Object> args, T ret);
}
