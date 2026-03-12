import { NextRequest, NextResponse } from 'next/server';

const COLLECTOR_ENDPOINT = process.env.OTEL_EXPORTER_OTLP_ENDPOINT
  ? process.env.OTEL_EXPORTER_OTLP_ENDPOINT.replace(':4317', ':4318')
  : 'http://otel-collector:4318';

export async function POST(request: NextRequest) {
  try {
    const metric = await request.json();

    // Convert web vital to OTLP metric format
    const now = Date.now() * 1_000_000; // nanoseconds
    const otlpPayload = {
      resourceMetrics: [
        {
          resource: {
            attributes: [
              { key: 'service.name', value: { stringValue: 'newsanalyzer-frontend' } },
              { key: 'telemetry.sdk.language', value: { stringValue: 'webjs' } },
            ],
          },
          scopeMetrics: [
            {
              scope: { name: 'web-vitals' },
              metrics: [
                {
                  name: metric.name,
                  gauge: {
                    dataPoints: [
                      {
                        asDouble: metric.value,
                        timeUnixNano: now.toString(),
                        attributes: [
                          { key: 'metric.id', value: { stringValue: metric.id } },
                        ],
                      },
                    ],
                  },
                },
              ],
            },
          ],
        },
      ],
    };

    await fetch(`${COLLECTOR_ENDPOINT}/v1/metrics`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(otlpPayload),
    });

    return NextResponse.json({ ok: true });
  } catch {
    return NextResponse.json({ ok: false }, { status: 500 });
  }
}
