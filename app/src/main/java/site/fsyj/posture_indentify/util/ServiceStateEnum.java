package site.fsyj.posture_indentify.util;

public enum ServiceStateEnum {
    RUNNING("运行中");

    private final String state;
    ServiceStateEnum(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }
}
