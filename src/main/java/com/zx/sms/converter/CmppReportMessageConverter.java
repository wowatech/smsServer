package com.zx.sms.converter;

import com.zx.sms.codec.cmpp.msg.CmppDeliverRequestMessage;
import com.zx.sms.codec.cmpp.msg.CmppReportRequestMessage;
import com.zx.sms.common.util.CachedMillisecondClock;
import com.zx.sms.common.util.MsgId;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.entity.CmppReportMessage;
import org.apache.commons.lang3.time.DateFormatUtils;

public class CmppReportMessageConverter {

    public static CmppReportMessage reConvert(CmppDeliverRequestMessage cmppDeliverRequestMessage, EndpointEntity entity){
        CmppReportMessage cmppReportMessage=new CmppReportMessage();
        cmppReportMessage.setId(entity.getId());
        cmppReportMessage.setDestId(cmppDeliverRequestMessage.getDestId());
        cmppReportMessage.setSrcId(cmppDeliverRequestMessage.getSrcterminalId());
        cmppReportMessage.setMsgId(cmppDeliverRequestMessage.getReportRequestMessage().getMsgId().toString());
        cmppReportMessage.setStat(cmppDeliverRequestMessage.getReportRequestMessage().getStat());
        return cmppReportMessage;
    }

    public static CmppDeliverRequestMessage convert(CmppReportMessage cmppReportMessage){
        CmppDeliverRequestMessage cmppDeliverRequestMessage=new CmppDeliverRequestMessage();
        cmppDeliverRequestMessage.setDestId(cmppReportMessage.getDestId());
        cmppDeliverRequestMessage.setSrcterminalId(cmppReportMessage.getSrcId());

        CmppReportRequestMessage report = new CmppReportRequestMessage();
        report.setDestterminalId(cmppDeliverRequestMessage.getSrcterminalId());
        report.setMsgId(new MsgId(cmppReportMessage.getMsgId()));
        String t = DateFormatUtils.format(CachedMillisecondClock.INS.now(), "yyMMddHHmm");
        report.setSubmitTime(t);
        report.setDoneTime(t);
        report.setStat(cmppReportMessage.getStat());
        report.setSmscSequence(0);
        cmppDeliverRequestMessage.setReportRequestMessage(report);
        return cmppDeliverRequestMessage;
    }
}
