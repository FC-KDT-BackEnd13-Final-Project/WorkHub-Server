package com.workhub.userTable.entity;

public enum CompanyStatus {
    ACTIVE, INACTIVE, SUSPENDED;

    public static Status fromValue(String value){return Enum.valueOf(Status.class, value);}
}
