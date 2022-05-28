package com.zx.sms.entity;

import java.io.Serializable;

public class CmppReportMessage implements Serializable {

    private String id;

    private String srcId;

    private String destId;

    private String msgId;

    private String stat;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSrcId() {
        return srcId;
    }

    public void setSrcId(String srcId) {
        this.srcId = srcId;
    }

    public String getDestId() {
        return destId;
    }

    public void setDestId(String destId) {
        this.destId = destId;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getStat() {
        return stat;
    }

    public void setStat(String stat) {
        this.stat = stat;
    }


    @Override
    public String toString() {
        return "CmppReportMessage{" +
                "id='" + id + '\'' +
                ", srcId='" + srcId + '\'' +
                ", destId='" + destId + '\'' +
                ", msgId='" + msgId + '\'' +
                ", stat='" + stat + '\'' +
                '}';
    }
}
