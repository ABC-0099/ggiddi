package com.meta12.SS8911.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MyPaymentDTO {

    private String productName;

    private String price;

    private String payType;

    private LocalDateTime payday;

    private String period;
}
