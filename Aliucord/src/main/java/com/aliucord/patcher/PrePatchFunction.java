package com.aliucord.patcher;

import java.util.ArrayList;

public interface PrePatchFunction {
    PrePatchRes run(Object _this, ArrayList<Object> args);
}
