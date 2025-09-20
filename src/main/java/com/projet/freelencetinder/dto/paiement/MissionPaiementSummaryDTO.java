/* ===== MissionPaiementSummaryDTO.java ===== */
package com.projet.freelencetinder.dto.paiement;

import java.math.BigDecimal;
import java.util.List;
import com.projet.freelencetinder.models.Mission.ClosurePolicy;

public class MissionPaiementSummaryDTO {

    private Long missionId;
    private String titreMission;
    private BigDecimal totalBrut;
    private BigDecimal totalCommission;
    private BigDecimal totalNetFreelance;
    private List<TranchePaiementResponseDTO> tranches;

    // Enrichissements pour UI de clôture
    private ClosurePolicy closurePolicy;
    private BigDecimal contractTotalAmount;
    private BigDecimal paidTotal;
    private boolean allRequiredPaidAndAccepted;
    private boolean finalTranchePaidAndAccepted;
    private Long finalTrancheId;
    private boolean closedByClient;
    private boolean closedByFreelancer;

    /* ---------- getters / setters ---------- */
    public Long getMissionId() { return missionId; }
    public void setMissionId(Long missionId) { this.missionId = missionId; }

    public String getTitreMission() { return titreMission; }
    public void setTitreMission(String titreMission) { this.titreMission = titreMission; }

    public BigDecimal getTotalBrut() { return totalBrut; }
    public void setTotalBrut(BigDecimal totalBrut) { this.totalBrut = totalBrut; }

    public BigDecimal getTotalCommission() { return totalCommission; }
    public void setTotalCommission(BigDecimal totalCommission) { this.totalCommission = totalCommission; }

    public BigDecimal getTotalNetFreelance() { return totalNetFreelance; }
    public void setTotalNetFreelance(BigDecimal totalNetFreelance) { this.totalNetFreelance = totalNetFreelance; }

    public List<TranchePaiementResponseDTO> getTranches() { return tranches; }
    public void setTranches(List<TranchePaiementResponseDTO> tranches) { this.tranches = tranches; }

    public ClosurePolicy getClosurePolicy() { return closurePolicy; }
    public void setClosurePolicy(ClosurePolicy closurePolicy) { this.closurePolicy = closurePolicy; }
    public BigDecimal getContractTotalAmount() { return contractTotalAmount; }
    public void setContractTotalAmount(BigDecimal contractTotalAmount) { this.contractTotalAmount = contractTotalAmount; }
    public BigDecimal getPaidTotal() { return paidTotal; }
    public void setPaidTotal(BigDecimal paidTotal) { this.paidTotal = paidTotal; }
    public boolean isAllRequiredPaidAndAccepted() { return allRequiredPaidAndAccepted; }
    public void setAllRequiredPaidAndAccepted(boolean allRequiredPaidAndAccepted) { this.allRequiredPaidAndAccepted = allRequiredPaidAndAccepted; }
    public boolean isFinalTranchePaidAndAccepted() { return finalTranchePaidAndAccepted; }
    public void setFinalTranchePaidAndAccepted(boolean finalTranchePaidAndAccepted) { this.finalTranchePaidAndAccepted = finalTranchePaidAndAccepted; }
    public Long getFinalTrancheId() { return finalTrancheId; }
    public void setFinalTrancheId(Long finalTrancheId) { this.finalTrancheId = finalTrancheId; }
    public boolean isClosedByClient() { return closedByClient; }
    public void setClosedByClient(boolean closedByClient) { this.closedByClient = closedByClient; }
    public boolean isClosedByFreelancer() { return closedByFreelancer; }
    public void setClosedByFreelancer(boolean closedByFreelancer) { this.closedByFreelancer = closedByFreelancer; }
}
