/**
 * Shared Axios Clients with OpenTelemetry Trace Propagation
 *
 * Centralizes HTTP client creation and injects W3C Trace Context (traceparent)
 * headers on all outbound requests for distributed tracing (OBS-1.4).
 */

import axios, { type InternalAxiosRequestConfig } from 'axios';
import { context, propagation } from '@opentelemetry/api';

const BACKEND_URL = process.env.NEXT_PUBLIC_BACKEND_URL || '';
const REASONING_URL = process.env.NEXT_PUBLIC_REASONING_SERVICE_URL || '';

/**
 * Injects trace context headers into outbound requests.
 * This adds the W3C `traceparent` header so downstream services
 * can continue the same distributed trace.
 */
function injectTraceContext(config: InternalAxiosRequestConfig) {
  const headers: Record<string, string> = {};
  propagation.inject(context.active(), headers);
  Object.assign(config.headers, headers);
  return config;
}

/** Axios client for Java backend API calls (default 10s timeout) */
export const backendClient = axios.create({
  baseURL: BACKEND_URL,
  timeout: 10000,
});

backendClient.interceptors.request.use(injectTraceContext);

/** Axios client for Python reasoning service calls (default 10s timeout) */
export const reasoningClient = axios.create({
  baseURL: REASONING_URL,
  timeout: 10000,
});

reasoningClient.interceptors.request.use(injectTraceContext);
