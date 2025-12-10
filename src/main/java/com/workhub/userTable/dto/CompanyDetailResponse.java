package com.workhub.userTable.dto;

import com.workhub.userTable.entity.Company;
import com.workhub.userTable.entity.Status;

public record CompanyDetailResponse (
    Long companyId,
    String companyName,
    String companyNumber,
    String tel,
    String address,
    Status status
){
    public static CompanyDetailResponse from(Company company){
        return new CompanyDetailResponse(
                company.getCompanyId(),
                company.getCompanyName(),
                company.getCompanyNumber(),
                company.getTel(),
                company.getAddress(),
                company.getCompanystatus()

        );
    }
}
