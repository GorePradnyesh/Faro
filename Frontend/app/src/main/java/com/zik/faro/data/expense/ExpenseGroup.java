package com.zik.faro.data.expense;

public class ExpenseGroup {
    public final String groupName;
    public final String groupId;

    public ExpenseGroup(String groupName, String groupId) {
        this.groupName = groupName;
        this.groupId = groupId;
    }

    public ExpenseGroup() {
        this(null, null);
    }
}
