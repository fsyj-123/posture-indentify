package site.fsyj.posture_indentify.util;

public class SystemConst {
    /**
     * 任务执行周期，单位为分钟
     */
    public static final int RETRIEVE_PERIOD = 20;

    /**
     * Timer delay
     */
    public static final int COLLECTION_TIME = 10;

    /**
     * 每次获取后，间隔多少秒再次获取
     */
    public static final int COLLECTION_INTERVAL = 30;

    /**
     * 每次的监听持续时间
     */
    public static final int LISTENING_DURATION = 3;


    public static final String WORK_NAME = "detect-service";

    public static final String CONTENT_TITLE = "姿态检测服务";
}
