package com.invision.web.Invision.dto;

public record DashboardMetricsDTO(int availableAssets,int loanedAssets, int activeLoans,  int totalAssets,int pendingLoanRequests,int overdueLoans) {
}
