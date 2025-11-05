package com.platemate.dto;

import java.time.LocalDateTime;

public class PayoutDtos {

    public static class StatementRequest {
        private Long providerId;
        private LocalDateTime from;
        private LocalDateTime to;
        public Long getProviderId() { return providerId; }
        public void setProviderId(Long providerId) { this.providerId = providerId; }
        public LocalDateTime getFrom() { return from; }
        public void setFrom(LocalDateTime from) { this.from = from; }
        public LocalDateTime getTo() { return to; }
        public void setTo(LocalDateTime to) { this.to = to; }
    }

    public static class StatementResponse {
        private Double gross;
        private Double platformCommission;
        private Double netPayable;
        public Double getGross() { return gross; }
        public void setGross(Double gross) { this.gross = gross; }
        public Double getPlatformCommission() { return platformCommission; }
        public void setPlatformCommission(Double platformCommission) { this.platformCommission = platformCommission; }
        public Double getNetPayable() { return netPayable; }
        public void setNetPayable(Double netPayable) { this.netPayable = netPayable; }
    }
}


