/**
 * Entity Extraction Page
 *
 * Page for extracting entities from text using Schema.org mapping
 */

'use client';

import { useState } from 'react';
import { reasoningApi } from '@/lib/api/entities';
import { EntityCard } from '@/components/EntityCard';
import type { ExtractedEntity, EntityType } from '@/types/entity';
import { ENTITY_TYPE_METADATA } from '@/types/entity';

export default function EntitiesPage() {
  const [text, setText] = useState('');
  const [confidenceThreshold, setConfidenceThreshold] = useState(0.7);
  const [entities, setEntities] = useState<ExtractedEntity[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedType, setSelectedType] = useState<EntityType | 'all'>('all');

  const handleExtract = async () => {
    if (!text.trim()) {
      setError('Please enter some text to analyze');
      return;
    }

    setIsLoading(true);
    setError(null);

    try {
      const response = await reasoningApi.extractEntities({
        text,
        confidence_threshold: confidenceThreshold,
      });

      setEntities(response.entities);
    } catch (err) {
      setError(
        err instanceof Error ? err.message : 'Failed to extract entities. Is the reasoning service running?'
      );
    } finally {
      setIsLoading(false);
    }
  };

  const filteredEntities =
    selectedType === 'all'
      ? entities
      : entities.filter((e) => e.entity_type === selectedType);

  const entityCounts = entities.reduce(
    (acc, entity) => {
      acc[entity.entity_type] = (acc[entity.entity_type] || 0) + 1;
      return acc;
    },
    {} as Record<EntityType, number>
  );

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-7xl mx-auto px-4">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold mb-2">Entity Extraction</h1>
          <p className="text-gray-600">
            Extract entities from text using spaCy NLP with Schema.org mapping
          </p>
        </div>

        {/* Input Section */}
        <div className="bg-white rounded-lg shadow p-6 mb-6">
          <label className="block text-sm font-medium mb-2">
            Text to Analyze
          </label>
          <textarea
            value={text}
            onChange={(e) => setText(e.target.value)}
            placeholder="Enter news article text or any text containing entities (people, organizations, locations, etc.)

Example: Senator Elizabeth Warren criticized the EPA's new regulations during a hearing in Washington, D.C. The Democratic Party leader called for stronger environmental protections."
            className="w-full h-40 px-3 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none"
          />

          <div className="mt-4 flex items-center gap-4">
            <div className="flex-1">
              <label className="block text-sm font-medium mb-2">
                Confidence Threshold: {Math.round(confidenceThreshold * 100)}%
              </label>
              <input
                type="range"
                min="0"
                max="1"
                step="0.05"
                value={confidenceThreshold}
                onChange={(e) => setConfidenceThreshold(parseFloat(e.target.value))}
                className="w-full"
              />
            </div>

            <button
              onClick={handleExtract}
              disabled={isLoading || !text.trim()}
              className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed font-medium"
            >
              {isLoading ? 'Extracting...' : 'Extract Entities'}
            </button>
          </div>

          {error && (
            <div className="mt-4 p-3 bg-red-50 border border-red-200 rounded-lg text-red-700 text-sm">
              {error}
            </div>
          )}
        </div>

        {/* Results */}
        {entities.length > 0 && (
          <>
            {/* Statistics */}
            <div className="bg-white rounded-lg shadow p-6 mb-6">
              <h2 className="text-lg font-semibold mb-4">
                Found {entities.length} {entities.length === 1 ? 'entity' : 'entities'}
              </h2>

              <div className="flex flex-wrap gap-2">
                <button
                  onClick={() => setSelectedType('all')}
                  className={`px-3 py-1 rounded-full text-sm font-medium transition-colors ${
                    selectedType === 'all'
                      ? 'bg-gray-800 text-white'
                      : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                  }`}
                >
                  All ({entities.length})
                </button>

                {(Object.keys(entityCounts) as EntityType[]).map((type) => {
                  const metadata = ENTITY_TYPE_METADATA[type];
                  return (
                    <button
                      key={type}
                      onClick={() => setSelectedType(type)}
                      className={`px-3 py-1 rounded-full text-sm font-medium transition-colors ${
                        selectedType === type
                          ? `${metadata.color} ${metadata.bgColor}`
                          : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                      }`}
                    >
                      {metadata.icon} {metadata.label} ({entityCounts[type]})
                    </button>
                  );
                })}
              </div>
            </div>

            {/* Entity List */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {filteredEntities.map((entity, index) => (
                <EntityCard key={index} entity={entity} showJsonLd={true} />
              ))}
            </div>
          </>
        )}

        {/* Empty State */}
        {entities.length === 0 && !isLoading && !error && (
          <div className="bg-white rounded-lg shadow p-12 text-center">
            <div className="text-6xl mb-4">🔍</div>
            <h3 className="text-xl font-semibold mb-2">No entities extracted yet</h3>
            <p className="text-gray-600">
              Enter some text above and click &quot;Extract Entities&quot; to get started
            </p>
          </div>
        )}
      </div>
    </div>
  );
}
