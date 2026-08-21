package com.ruoyi.crm.datajob.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.crm.customer.domain.CrmContact;
import com.ruoyi.crm.followup.domain.CrmFollowUp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("联系人和跟进记录雪花 ID 序列化测试")
class ImportedEntityIdSerializationTest
{
    @Test
    void serializesSnowflakeIdsAsStrings() throws Exception
    {
        CrmContact contact = new CrmContact();
        contact.setContactId(481268411817660417L);
        CrmFollowUp followUp = new CrmFollowUp();
        followUp.setFollowUpId(481268411817660418L);

        ObjectMapper mapper = new ObjectMapper();
        assertTrue(mapper.writeValueAsString(contact)
                .contains("\"contactId\":\"481268411817660417\""));
        assertTrue(mapper.writeValueAsString(followUp)
                .contains("\"followUpId\":\"481268411817660418\""));
    }
}
