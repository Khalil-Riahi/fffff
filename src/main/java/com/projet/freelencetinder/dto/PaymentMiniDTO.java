package com.projet.freelencetinder.dto;

import java.math.BigDecimal;
import java.util.List;

public class PaymentMiniDTO {

    private BigDecimal totalBrut;
    private BigDecimal totalNetFreelance;
    private BigDecimal paidTotal;
    private int progressionPct;
    private List<TrancheMiniDTO> tranches;

    public BigDecimal getTotalBrut() { return totalBrut; }
    public void setTotalBrut(BigDecimal totalBrut) { this.totalBrut = totalBrut; }
    public BigDecimal getTotalNetFreelance() { return totalNetFreelance; }
    public void setTotalNetFreelance(BigDecimal totalNetFreelance) { this.totalNetFreelance = totalNetFreelance; }
    public BigDecimal getPaidTotal() { return paidTotal; }
    public void setPaidTotal(BigDecimal paidTotal) { this.paidTotal = paidTotal; }
    public int getProgressionPct() { return progressionPct; }
    public void setProgressionPct(int progressionPct) { this.progressionPct = progressionPct; }
    public List<TrancheMiniDTO> getTranches() { return tranches; }
    public void setTranches(List<TrancheMiniDTO> tranches) { this.tranches = tranches; }
}


