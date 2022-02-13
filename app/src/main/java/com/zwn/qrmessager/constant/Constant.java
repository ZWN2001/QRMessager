package com.zwn.qrmessager.constant;

import com.zwn.qrmessager.bean.Settings;

public class Constant {
    private static Settings settings;
    public static Settings getSettings(){
        if(settings==null){
            settings = new Settings();
        }
        return settings;
    }

}
