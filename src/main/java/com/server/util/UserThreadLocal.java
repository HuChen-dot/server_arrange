package com.server.util;


import com.server.pojo.entity.UserInfo;

public class UserThreadLocal {

    private static final ThreadLocal<UserInfo> userLocal = new ThreadLocal();


    public static void set(UserInfo userInfo) {
        userLocal.remove();
        userLocal.set(userInfo);
    }


    public static UserInfo get() {
        return userLocal.get();
    }


    public static void remove() {
        userLocal.remove();
    }
}
