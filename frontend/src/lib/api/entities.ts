/**
 * Entity API Client
 *
 * Client for interacting with Python reasoning service (entity extraction)
 * and Java backend (entity storage).
 */

import { backendClient, reasoningClient } from './client';
import type {
  EntityExtractionRequest,
  EntityExtractionResponse,
  Entity,
  CreateEntityRequest,
} from '@/types/entity';

/**
 * Python Reasoning Service API
 */
export const reasoningApi = {
  /**
   * Extract entities from text using spaCy NLP with Schema.org mapping
   */
  extractEntities: async (
    request: EntityExtractionRequest
  ): Promise<EntityExtractionResponse> => {
    const response = await reasoningClient.post<EntityExtractionResponse>(
      '/entities/extract',
      request
    );
    return response.data;
  },
};

/**
 * Java Backend API
 */
export const backendApi = {
  /**
   * Create a new entity in the database
   */
  createEntity: async (request: CreateEntityRequest): Promise<Entity> => {
    const response = await backendClient.post<Entity>('/api/entities', request);
    return response.data;
  },

  /**
   * Get entity by ID
   */
  getEntity: async (id: string): Promise<Entity> => {
    const response = await backendClient.get<Entity>(`/api/entities/${id}`);
    return response.data;
  },

  /**
   * Get all entities
   */
  getAllEntities: async (): Promise<Entity[]> => {
    const response = await backendClient.get<Entity[]>('/api/entities');
    return response.data;
  },

  /**
   * Get entities by type
   */
  getEntitiesByType: async (entityType: string): Promise<Entity[]> => {
    const response = await backendClient.get<Entity[]>(
      `/api/entities/type/${entityType}`
    );
    return response.data;
  },

  /**
   * Get entities by Schema.org type
   */
  getEntitiesBySchemaOrgType: async (schemaOrgType: string): Promise<Entity[]> => {
    const response = await backendClient.get<Entity[]>(
      `/api/entities/schema-org/${schemaOrgType}`
    );
    return response.data;
  },

  /**
   * Search entities by name
   */
  searchEntities: async (query: string): Promise<Entity[]> => {
    const response = await backendClient.get<Entity[]>('/api/entities/search', {
      params: { q: query },
    });
    return response.data;
  },

  /**
   * Update entity
   */
  updateEntity: async (id: string, request: CreateEntityRequest): Promise<Entity> => {
    const response = await backendClient.put<Entity>(`/api/entities/${id}`, request);
    return response.data;
  },

  /**
   * Delete entity
   */
  deleteEntity: async (id: string): Promise<void> => {
    await backendClient.delete(`/api/entities/${id}`);
  },

  /**
   * Verify entity
   */
  verifyEntity: async (id: string): Promise<Entity> => {
    const response = await backendClient.post<Entity>(`/api/entities/${id}/verify`);
    return response.data;
  },
};
