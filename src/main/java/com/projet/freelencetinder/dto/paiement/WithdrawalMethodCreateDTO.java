package com.projet.freelencetinder.dto.paiement;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class WithdrawalMethodCreateDTO {

    @NotNull
    private String type; // RIB or D17

    @Size(max = 30)
    private String rib;

    @Size(max = 20)
    private String walletNumber;

    private boolean principal = false;

    /* getters / setters */
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getRib() { return rib; }
    public void setRib(String rib) { this.rib = rib; }

    public String getWalletNumber() { return walletNumber; }
    public void setWalletNumber(String walletNumber) { this.walletNumber = walletNumber; }

    public boolean isPrincipal() { return principal; }
    public void setPrincipal(boolean principal) { this.principal = principal; }
}