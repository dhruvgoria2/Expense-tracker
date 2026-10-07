import React from "react";
import { formatINR } from "../../utils/formatcurrency";

export default function BankCard({ bankName, accountType, balance, accountNumber, themeColor }) {
  return (
    <div
      className="bank-credit-card"
      style={{ background: themeColor || "linear-gradient(135deg, #5200b8 0%, #7a22e6 100%)" }}
    >
      <div className="card-top">
        <div>
          <p className="bank-label-name">{bankName}</p>
          <span className="account-badge-type">{accountType}</span>
        </div>
      </div>

      <div>
        <p className="balance-headline-title">Available Balance</p>
        <h3 className="bank-card-money">{formatINR(balance)}</h3>
      </div>

      <div className="card-bottom">
        <p className="account-mask-digits">•••• {String(accountNumber || "").slice(-4) || "0000"}</p>
        <span className="status-indicator-dot active">Active</span>
      </div>
    </div>
  );
}