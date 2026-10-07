import React from "react";
import BankCard from "./BankCard";
import { formatINR } from "../../utils/formatcurrency";

export default function BankAccountsList({ accounts = [] }) {
  const total = accounts.reduce((sum, a) => sum + (Number(a.balance) || 0), 0);

  return (
    <div className="bank-accounts-section-wrapper">
      <div className="bank-section-header">
        <div>
          <h3 style={{ margin: 0 }}>Your Bank Accounts</h3>
          <p className="text-muted-desc">Money saved in each account.</p>
        </div>
        <div className="total-badge-summary">
          <span>Net Assets: </span>
          <strong>{formatINR(total)}</strong>
        </div>
      </div>

      <div className="bank-cards-horizontal-grid">
        {accounts.map((a) => (
          <BankCard
            key={a.id}
            bankName={a.bankName}
            accountType={a.accountType}
            balance={a.balance}
            accountNumber={a.accountNumber}
            themeColor={a.themeColor}
          />
        ))}
      </div>
    </div>
  );
}