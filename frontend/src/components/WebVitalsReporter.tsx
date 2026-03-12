'use client';

import { useEffect } from 'react';
import { onLCP, onCLS, onINP } from 'web-vitals';

function sendToCollector(metric: { name: string; value: number; id: string }) {
  const body = JSON.stringify({
    name: `web_vitals.${metric.name.toLowerCase()}`,
    value: metric.value,
    id: metric.id,
  });
  navigator.sendBeacon('/api/vitals', body);
}

export function WebVitalsReporter() {
  useEffect(() => {
    onLCP(sendToCollector);
    onCLS(sendToCollector);
    onINP(sendToCollector);
  }, []);
  return null;
}
