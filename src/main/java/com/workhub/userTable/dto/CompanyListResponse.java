package com.workhub.userTable.dto;

import com.workhub.userTable.entity.Company;
import com.workhub.userTable.entity.Status;

public record CompanyListResponse (
        Long companyId,
        String companyName,
        String companyNumber,
        String tel,
        String address,
        Status status
){
    public static CompanyListResponse from(Company company){
        return new CompanyListResponse(
                company.getCompanyId(),
                company.getCompanyName(),
                company.getCompanyNumber(),
                company.getTel(),
                company.getAddress(),
                company.getCompanystatus()
        );
    }
}
