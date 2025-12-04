package com.workhub.userTable.service;

import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.userTable.dto.CompanyRegisterRequest;
import com.workhub.userTable.dto.CompanyResponse;
import com.workhub.userTable.entity.Company;
import com.workhub.userTable.repository.CompanyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private CompanyService companyService;

    @Nested
    @DisplayName("registerCompany")
    class RegisterCompany {

        @Test
        @DisplayName("중복 없는 경우 고객사를 저장하고 DTO를 반환한다")
        void success() {
            CompanyRegisterRequest request = new CompanyRegisterRequest(
                    "패스트캠퍼스",
                    "1234567889",
                    "02-123-4556",
                    "서울시 강남구 테헤란로 32"
            );
            Company savedCompany = Company.builder()
                    .companyId(1L)
                    .companyName(request.companyName())
                    .companyNumber(request.companyNumber())
                    .tel(request.tel())
                    .address(request.address())
                    .build();
            given(companyRepository.existsByCompanyNumber(request.companyNumber())).willReturn(false);
            given(companyRepository.save(any(Company.class))).willReturn(savedCompany);

            CompanyResponse response = companyService.registerCompany(request);

            assertThat(response).isEqualTo(CompanyResponse.from(savedCompany));
            verify(companyRepository).existsByCompanyNumber(request.companyNumber());
            verify(companyRepository).save(any(Company.class));
        }

        @Test
        @DisplayName("사업자 번호가 중복이면 예외가 발생한다")
        void fail_duplicateCompanyNumber() {
            CompanyRegisterRequest request = new CompanyRegisterRequest(
                    "패스트캠퍼스",
                    "1234567889",
                    "02-123-4556",
                    "서울시 강남구 테헤란로 32"
            );
            given(companyRepository.existsByCompanyNumber(request.companyNumber())).willReturn(true);

            assertThatThrownBy(() -> companyService.registerCompany(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.COMPANY_ALREADY_EXISTS);

            verify(companyRepository).existsByCompanyNumber(request.companyNumber());
            verify(companyRepository, never()).save(any(Company.class));
        }
    }
}
