export async function register() {
  if (process.env.NEXT_RUNTIME === 'nodejs') {
    const { NodeSDK } = await import('@opentelemetry/sdk-node');
    const { OTLPTraceExporter } = await import('@opentelemetry/exporter-trace-otlp-grpc');
    const { OTLPMetricExporter } = await import('@opentelemetry/exporter-metrics-otlp-grpc');
    const { getNodeAutoInstrumentations } = await import('@opentelemetry/auto-instrumentations-node');
    const { resourceFromAttributes } = await import('@opentelemetry/resources');
    const { PeriodicExportingMetricReader } = await import('@opentelemetry/sdk-metrics');

    const serviceName = process.env.OTEL_SERVICE_NAME || 'newsanalyzer-frontend';

    const sdk = new NodeSDK({
      resource: resourceFromAttributes({
        'service.name': serviceName,
      }),
      traceExporter: new OTLPTraceExporter(),
      metricReader: new PeriodicExportingMetricReader({
        exporter: new OTLPMetricExporter(),
      }),
      instrumentations: [
        getNodeAutoInstrumentations({
          // fs instrumentation causes crashes in Next.js — it intercepts every
          // file read during module resolution and startup, creating circular
          // initialization issues. Disable it; HTTP/fetch is what we care about.
          '@opentelemetry/instrumentation-fs': { enabled: false },
        }),
      ],
    });

    sdk.start();

    console.log(`OpenTelemetry initialized for ${serviceName}`);
  }
}
