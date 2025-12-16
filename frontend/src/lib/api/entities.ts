/**
 * Entity API Client
 *
 * Client for interacting with Python reasoning service (entity extraction)
 * and Java backend (entity storage).
 */

import axios from 'axios';
import type {
  EntityExtractionRequest,
  EntityExtractionResponse,
  Entity,
  CreateEntityRequest,
} from '@/types/entity';

// API base URLs
const REASONING_SERVICE_URL = process.env.NEXT_PUBLIC_REASONING_SERVICE_URL || 'http://localhost:8001';
const BACKEND_URL = process.env.NEXT_PUBLIC_BACKEND_URL || 'http://localhost:8080';

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
    const response = await axios.post<EntityExtractionResponse>(
      `${REASONING_SERVICE_URL}/entities/extract`,
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
    const response = await axios.post<Entity>(`${BACKEND_URL}/api/entities`, request);
    return response.data;
  },

  /**
   * Get entity by ID
   */
  getEntity: async (id: string): Promise<Entity> => {
    const response = await axios.get<Entity>(`${BACKEND_URL}/api/entities/${id}`);
    return response.data;
  },

  /**
   * Get all entities
   */
  getAllEntities: async (): Promise<Entity[]> => {
    const response = await axios.get<Entity[]>(`${BACKEND_URL}/api/entities`);
    return response.data;
  },

  /**
   * Get entities by type
   */
  getEntitiesByType: async (entityType: string): Promise<Entity[]> => {
    const response = await axios.get<Entity[]>(
      `${BACKEND_URL}/api/entities/type/${entityType}`
    );
    return response.data;
  },

  /**
   * Get entities by Schema.org type
   */
  getEntitiesBySchemaOrgType: async (schemaOrgType: string): Promise<Entity[]> => {
    const response = await axios.get<Entity[]>(
      `${BACKEND_URL}/api/entities/schema-org/${schemaOrgType}`
    );
    return response.data;
  },

  /**
   * Search entities by name
   */
  searchEntities: async (query: string): Promise<Entity[]> => {
    const response = await axios.get<Entity[]>(`${BACKEND_URL}/api/entities/search`, {
      params: { q: query },
    });
    return response.data;
  },

  /**
   * Update entity
   */
  updateEntity: async (id: string, request: CreateEntityRequest): Promise<Entity> => {
    const response = await axios.put<Entity>(`${BACKEND_URL}/api/entities/${id}`, request);
    return response.data;
  },

  /**
   * Delete entity
   */
  deleteEntity: async (id: string): Promise<void> => {
    await axios.delete(`${BACKEND_URL}/api/entities/${id}`);
  },

  /**
   * Verify entity
   */
  verifyEntity: async (id: string): Promise<Entity> => {
    const response = await axios.post<Entity>(`${BACKEND_URL}/api/entities/${id}/verify`);
    return response.data;
  },
};
