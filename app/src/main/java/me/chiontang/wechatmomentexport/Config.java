package me.chiontang.wechatmomentexport;

import android.os.Environment;

/**
 * Created by chiontang on 2/4/16.
 */
public class Config {
    static boolean enabled = false;

    static boolean ready = false;

    static String outputFile = Environment.getExternalStorageDirectory() + "/moments_output.json";

    final static String[] VERSIONS = {"6.3.13.49_r4080b63"};

    final static String[] PROTOCAL_SNS_DETAIL_CLASSES = {"com.tencent.mm.protocal.b.atp"};

    final static String[] PROTOCAL_SNS_DETAIL_METHODS = {"a"};

    final static String[] SNS_XML_GENERATOR_CLASSES = {"com.tencent.mm.plugin.sns.f.i"};

    final static String[] SNS_XML_GENERATOR_METHODS = {"a"};

    final static String[] PROTOCAL_SNS_OBJECT_CLASSES = {"com.tencent.mm.protocal.b.aqi"};

    final static String[] PROTOCAL_SNS_OBJECT_METHODS = {"a"};

    final static String[] PROTOCAL_SNS_OBJECT_USERID_FIELDS = {"iYA"};

    final static String[] PROTOCAL_SNS_OBJECT_NICKNAME_FIELDS = {"jyd"};

    final static String[] PROTOCAL_SNS_OBJECT_TIMESTAMP_FIELDS = {"fpL"};

    final static String[] PROTOCAL_SNS_OBJECT_COMMENTS_FIELDS = {"jJX"};

    final static String[] PROTOCAL_SNS_OBJECT_LIKES_FIELDS = {"jJU"};

    final static String[] SNS_OBJECT_EXT_AUTHOR_NAME_FIELDS = {"jyd"};

    final static String[] SNS_OBJECT_EXT_REPLY_TO_FIELDS = {"jJM"};

    final static String[] SNS_OBJECT_EXT_COMMENT_FIELDS = {"fsI"};

    final static String[] SNS_OBJECT_EXT_AUTHOR_ID_FIELDS = {"iYA"};

    static String PROTOCAL_SNS_DETAIL_CLASS;

    static String PROTOCAL_SNS_DETAIL_METHOD;

    static String SNS_XML_GENERATOR_CLASS;

    static String SNS_XML_GENERATOR_METHOD;

    static String PROTOCAL_SNS_OBJECT_CLASS;

    static String PROTOCAL_SNS_OBJECT_METHOD;

    static String PROTOCAL_SNS_OBJECT_USERID_FIELD;

    static String PROTOCAL_SNS_OBJECT_NICKNAME_FIELD;

    static String PROTOCAL_SNS_OBJECT_TIMESTAMP_FIELD;

    static String PROTOCAL_SNS_OBJECT_COMMENTS_FIELD;

    static String PROTOCAL_SNS_OBJECT_LIKES_FIELD;

    static String SNS_OBJECT_EXT_AUTHOR_NAME_FIELD;

    static String SNS_OBJECT_EXT_REPLY_TO_FIELD;

    static String SNS_OBJECT_EXT_COMMENT_FIELD;

    static String SNS_OBJECT_EXT_AUTHOR_ID_FIELD;

    static void checkWeChatVersion(String version) {
        for (int i=0;i<VERSIONS.length;i++) {
            if (VERSIONS[i].equals(version)) {
                Config.setConstants(i);
                Config.ready = true;
                return;
            }
        }
        Config.ready = false;
    }

    static void setConstants(int index) {
        PROTOCAL_SNS_DETAIL_CLASS = PROTOCAL_SNS_DETAIL_CLASSES[index];
        PROTOCAL_SNS_DETAIL_METHOD = PROTOCAL_SNS_DETAIL_METHODS[index];
        SNS_XML_GENERATOR_CLASS = SNS_XML_GENERATOR_CLASSES[index];
        SNS_XML_GENERATOR_METHOD = SNS_XML_GENERATOR_METHODS[index];
        PROTOCAL_SNS_OBJECT_CLASS = PROTOCAL_SNS_OBJECT_CLASSES[index];
        PROTOCAL_SNS_OBJECT_METHOD = PROTOCAL_SNS_OBJECT_METHODS[index];
        PROTOCAL_SNS_OBJECT_USERID_FIELD = PROTOCAL_SNS_OBJECT_USERID_FIELDS[index];
        PROTOCAL_SNS_OBJECT_NICKNAME_FIELD = PROTOCAL_SNS_OBJECT_NICKNAME_FIELDS[index];
        PROTOCAL_SNS_OBJECT_TIMESTAMP_FIELD = PROTOCAL_SNS_OBJECT_TIMESTAMP_FIELDS[index];
        PROTOCAL_SNS_OBJECT_COMMENTS_FIELD = PROTOCAL_SNS_OBJECT_COMMENTS_FIELDS[index];
        PROTOCAL_SNS_OBJECT_LIKES_FIELD = PROTOCAL_SNS_OBJECT_LIKES_FIELDS[index];
        SNS_OBJECT_EXT_AUTHOR_NAME_FIELD = SNS_OBJECT_EXT_AUTHOR_NAME_FIELDS[index];
        SNS_OBJECT_EXT_REPLY_TO_FIELD = SNS_OBJECT_EXT_REPLY_TO_FIELDS[index];
        SNS_OBJECT_EXT_COMMENT_FIELD = SNS_OBJECT_EXT_COMMENT_FIELDS[index];
        SNS_OBJECT_EXT_AUTHOR_ID_FIELD = SNS_OBJECT_EXT_AUTHOR_ID_FIELDS[index];
    }

}
