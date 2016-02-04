package me.chiontang.wechatmomentexport;

import java.lang.reflect.Field;
import java.util.LinkedList;

import de.robv.android.xposed.XposedBridge;

/**
 * Created by chiontang on 2/4/16.
 */
public class Utils {

    static public void dumpObject(Object object) {
        for (Field field: object.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            Object value = null;
            try {
                value = field.get(object);
                if (value == null) {
                    continue;
                }
                XposedBridge.log("field=" + field.getName() + "; string=" + value.toString());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    static private void dumpAqi(Object aqiObject) throws Throwable{

        Field field = aqiObject.getClass().getField("jJX");
        LinkedList list = (LinkedList)field.get(aqiObject);
        for (int i=0;i<list.size();i++) {
            Object childObject = list.get(i);
            dumpObject(childObject);
        }

        field = aqiObject.getClass().getField("jJU");
        LinkedList likeList = (LinkedList)field.get(aqiObject);
        for (int i=0;i<likeList.size();i++) {
            Object likeObject = likeList.get(i);
            dumpObject(likeObject);
        }

        field = aqiObject.getClass().getField("jKa");
        list = (LinkedList)field.get(aqiObject);
        for (int i=0;i<likeList.size();i++) {
            Object object = list.get(i);
            dumpObject(object);
        }

        dumpObject(aqiObject);
    }
}
