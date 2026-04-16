package com.tcon.financial_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Data
@Component
@ConfigurationProperties(prefix = "platform.fee")
public class PlatformFeeConfig {

    private BigDecimal defaultRate;     // 25%
    private BigDecimal negotiatedRate;  // 20%
}