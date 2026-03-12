"""
OpenTelemetry initialization for the NewsAnalyzer Reasoning Service.

This module configures all three telemetry signals (traces, metrics, logs)
and must be initialized BEFORE the FastAPI app is created. The OTel SDK
reads configuration from OTEL_* environment variables set in Docker Compose.
"""

import logging
import os

from fastapi import FastAPI
from opentelemetry import metrics, trace
from opentelemetry.exporter.otlp.proto.grpc.metric_exporter import OTLPMetricExporter
from opentelemetry.exporter.otlp.proto.grpc.trace_exporter import OTLPSpanExporter
from opentelemetry.instrumentation.httpx import HTTPXClientInstrumentor
from opentelemetry.instrumentation.logging import LoggingInstrumentor
from opentelemetry.sdk.metrics import MeterProvider
from opentelemetry.sdk.metrics.export import PeriodicExportingMetricReader
from opentelemetry.sdk.resources import SERVICE_NAME, Resource
from opentelemetry.sdk.trace import TracerProvider
from opentelemetry.sdk.trace.export import BatchSpanProcessor

LOG_FORMAT = (
    "%(asctime)s %(levelname)s [trace=%(otelTraceID)s span=%(otelSpanID)s] "
    "%(name)s - %(message)s"
)

logger = logging.getLogger(__name__)


def init_telemetry() -> None:
    """Initialize OpenTelemetry providers and global instrumentors.

    Must be called before FastAPI app creation so that instrumentation
    hooks are in place when libraries are first used.

    Configuration is read from OTEL_* environment variables:
    - OTEL_SERVICE_NAME: service identity in telemetry
    - OTEL_EXPORTER_OTLP_ENDPOINT: collector address (auto-read by exporters)
    - OTEL_EXPORTER_OTLP_PROTOCOL: transport protocol (auto-read by exporters)
    """
    resource = Resource.create({
        SERVICE_NAME: os.getenv("OTEL_SERVICE_NAME", "newsanalyzer-reasoning"),
    })

    # Traces — BatchSpanProcessor buffers spans and flushes in batches
    tracer_provider = TracerProvider(resource=resource)
    tracer_provider.add_span_processor(
        BatchSpanProcessor(OTLPSpanExporter())
    )
    trace.set_tracer_provider(tracer_provider)

    # Metrics — PeriodicExportingMetricReader flushes on a configurable interval
    metric_reader = PeriodicExportingMetricReader(OTLPMetricExporter())
    meter_provider = MeterProvider(resource=resource, metric_readers=[metric_reader])
    metrics.set_meter_provider(meter_provider)

    # Configure logging format with OTel trace context placeholders
    logging.basicConfig(level=logging.INFO, format=LOG_FORMAT, force=True)

    # Global instrumentors (not tied to a specific app instance)
    HTTPXClientInstrumentor().instrument()
    LoggingInstrumentor().instrument(set_logging_format=True)

    logger.info("OpenTelemetry initialized for %s", os.getenv("OTEL_SERVICE_NAME"))


def instrument_app(app: FastAPI) -> None:
    """Instrument a FastAPI application instance.

    Must be called after app creation because FastAPIInstrumentor
    needs to wrap the app's route handlers and middleware stack.
    """
    from opentelemetry.instrumentation.fastapi import FastAPIInstrumentor

    FastAPIInstrumentor.instrument_app(app)
    logger.info("FastAPI application instrumented with OpenTelemetry")
