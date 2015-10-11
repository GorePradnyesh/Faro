package com.zik.faro.data.expense;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ExpenseGroup {
    public String groupName;
    public String groupId;

    public ExpenseGroup(String groupName, String groupId) {
        this.groupName = groupName;
        this.groupId = groupId;
    }

    public ExpenseGroup() {
        //this(null, null);
    }
}
