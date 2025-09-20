package com.projet.freelencetinder.dto.paiement;

import java.time.LocalDateTime;

public class WithdrawalMethodResponseDTO {

    private Long id;
    private String type;
    private String rib;
    private String walletNumber;
    private boolean principal;
    private LocalDateTime dateAjout;

    /* getters / setters */
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getRib() { return rib; }
    public void setRib(String rib) { this.rib = rib; }

    public String getWalletNumber() { return walletNumber; }
    public void setWalletNumber(String walletNumber) { this.walletNumber = walletNumber; }

    public boolean isPrincipal() { return principal; }
    public void setPrincipal(boolean principal) { this.principal = principal; }

    public LocalDateTime getDateAjout() { return dateAjout; }
    public void setDateAjout(LocalDateTime dateAjout) { this.dateAjout = dateAjout; }
}