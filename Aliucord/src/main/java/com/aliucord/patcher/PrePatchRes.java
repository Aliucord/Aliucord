package com.aliucord.patcher;

import java.util.ArrayList;

public class PrePatchRes {
    public ArrayList<Object> args;
    public boolean callOriginalMethod = true;
    public Object ret;

    public PrePatchRes(ArrayList<Object> args) { this.args = args; }
    public PrePatchRes(ArrayList<Object> args, Object ret) {
        this.args = args;
        callOriginalMethod = false;
        this.ret = ret;
    }
}
