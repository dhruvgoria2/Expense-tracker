import React from 'react';
import { formatINR } from '../utils/formatCurrency';

export default function StatCard({ title, value, bg, note }) {
  // Ensure the app never crashes even if the number calculation isn't ready yet
  const safeValue = typeof value === 'number' ? value : 0;

  return (
    <div className="stat" style={{ background: bg || '#ffffff' }}>
      <p style={{ margin: 0 }}>{title || 'Metric'}</p>
      <h2>{formatINR(safeValue)}</h2>
      {note && <small className="note">{note}</small>}
    </div>
  );
}