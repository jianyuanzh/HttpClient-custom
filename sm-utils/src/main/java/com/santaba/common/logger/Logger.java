package com.santaba.common.logger;

/**
 * Created by vincent on 9/7/15.
 */
public class Logger {
    public static void debug(String s, String s1) {
        System.out.println(String.format("DummyLogger:debug:%s, %s", s, s1));
    }

    public static void trace2(String s) {
        System.out.println(String.format("DummyLogger:trace:%s", s));

    }

    public static void debug2(String s) {
        System.out.println(String.format("DummyLogger:debug:%s", s));
    }

    public static void error2(String s) {
        System.out.println(String.format("DummyLogger:error2:%s", s));

    }

    public static void warn2(String s) {
        System.out.println(String.format("DummyLogger:warn2:%s", s));
    }

    public static void error(String s, String s1) {
        System.out.println(String.format("DummyLogger:error:%s, %s", s, s1));

    }

    public static void warn(String s, String s1) {
        System.out.println(String.format("DummyLogger:warn:%s, %s", s, s1));
    }

    public static void warn(String s, String s1, Throwable t) {
        System.out.println(String.format("DummyLogger:warn:%s, %s", s, s1));
        t.printStackTrace();
    }

    public static void info(String s, String s1, String s2) {
        System.out.println(String.format("DummyLogger:info:%s, %s, %s", s, s1, s2));
    }

    public static void error(String s, String s1, Exception e) {
        System.out.println(String.format("DummyLogger:error:%s, %s", s, s1));
        e.printStackTrace();
    }
}
