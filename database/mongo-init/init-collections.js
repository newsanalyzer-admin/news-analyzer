// MongoDB initialization script for News Analyzer
// This script creates collections and indexes for the news analysis platform

// Switch to the newsanalyzer database
db = db.getSiblingDB('newsanalyzer');

// Create collections with validation schemas

// Article analyses collection
db.createCollection('article_analyses', {
    validator: {
        $jsonSchema: {
            bsonType: 'object',
            required: ['articleUrl', 'title', 'content', 'sourceId', 'analyzedAt'],
            properties: {
                articleUrl: {
                    bsonType: 'string',
                    description: 'URL of the analyzed article'
                },
                title: {
                    bsonType: 'string',
                    description: 'Article title'
                },
                content: {
                    bsonType: 'string',
                    description: 'Full article content'
                },
                sourceId: {
                    bsonType: 'string',
                    description: 'UUID of the news source'
                },
                analyzedAt: {
                    bsonType: 'date',
                    description: 'When the analysis was performed'
                },
                contentHash: {
                    bsonType: 'string',
                    description: 'SHA-256 hash of content for deduplication'
                },
                claimValidations: {
                    bsonType: 'array',
                    items: {
                        bsonType: 'object',
                        properties: {
                            claimText: { bsonType: 'string' },
                            validationResult: { bsonType: 'string' },
                            confidenceScore: { bsonType: 'double' },
                            authoritativeSource: { bsonType: 'string' }
                        }
                    }
                },
                biasAnalysis: {
                    bsonType: 'object',
                    properties: {
                        overallBiasScore: { bsonType: 'double' },
                        biasIndicators: { bsonType: 'array' },
                        languagePatterns: { bsonType: 'object' }
                    }
                }
            }
        }
    }
});

// External references collection
db.createCollection('external_references', {
    validator: {
        $jsonSchema: {
            bsonType: 'object',
            required: ['sourceType', 'name', 'apiEndpoint'],
            properties: {
                sourceType: {
                    bsonType: 'string',
                    enum: ['government', 'academic', 'legal', 'fact_checker'],
                    description: 'Type of external reference source'
                },
                name: {
                    bsonType: 'string',
                    description: 'Human readable name of the source'
                },
                apiEndpoint: {
                    bsonType: 'string',
                    description: 'API endpoint URL'
                },
                reliability: {
                    bsonType: 'double',
                    minimum: 0,
                    maximum: 1,
                    description: 'Reliability score from 0.0 to 1.0'
                }
            }
        }
    }
});

// Web scraping results collection
db.createCollection('scraping_results', {
    validator: {
        $jsonSchema: {
            bsonType: 'object',
            required: ['url', 'scrapedAt', 'status'],
            properties: {
                url: {
                    bsonType: 'string',
                    description: 'URL that was scraped'
                },
                scrapedAt: {
                    bsonType: 'date',
                    description: 'When scraping was performed'
                },
                status: {
                    bsonType: 'string',
                    enum: ['success', 'failed', 'partial'],
                    description: 'Scraping status'
                },
                content: {
                    bsonType: 'object',
                    properties: {
                        title: { bsonType: 'string' },
                        body: { bsonType: 'string' },
                        author: { bsonType: 'string' },
                        publishDate: { bsonType: 'date' },
                        metadata: { bsonType: 'object' }
                    }
                }
            }
        }
    }
});

// User-generated reports collection
db.createCollection('user_reports', {
    validator: {
        $jsonSchema: {
            bsonType: 'object',
            required: ['userId', 'title', 'createdAt'],
            properties: {
                userId: {
                    bsonType: 'string',
                    description: 'UUID of the user who created the report'
                },
                title: {
                    bsonType: 'string',
                    description: 'Report title'
                },
                createdAt: {
                    bsonType: 'date',
                    description: 'When the report was created'
                },
                isPublic: {
                    bsonType: 'bool',
                    description: 'Whether the report is publicly visible'
                },
                content: {
                    bsonType: 'object',
                    properties: {
                        summary: { bsonType: 'string' },
                        methodology: { bsonType: 'string' },
                        findings: { bsonType: 'array' },
                        sources: { bsonType: 'array' },
                        conclusions: { bsonType: 'string' }
                    }
                }
            }
        }
    }
});

// Create indexes for performance

// Article analyses indexes
db.article_analyses.createIndex({ 'articleUrl': 1 }, { unique: true });
db.article_analyses.createIndex({ 'sourceId': 1, 'analyzedAt': -1 });
db.article_analyses.createIndex({ 'contentHash': 1 });
db.article_analyses.createIndex({ 'analyzedAt': -1 });
db.article_analyses.createIndex({ 'claimValidations.validationResult': 1 });
db.article_analyses.createIndex({ 'biasAnalysis.overallBiasScore': 1 });

// Text search index for article content
db.article_analyses.createIndex({
    'title': 'text',
    'content': 'text',
    'claimValidations.claimText': 'text'
}, {
    weights: {
        'title': 10,
        'claimValidations.claimText': 5,
        'content': 1
    },
    name: 'article_text_search'
});

// External references indexes
db.external_references.createIndex({ 'sourceType': 1 });
db.external_references.createIndex({ 'name': 1 });
db.external_references.createIndex({ 'reliability': -1 });

// Scraping results indexes
db.scraping_results.createIndex({ 'url': 1 });
db.scraping_results.createIndex({ 'scrapedAt': -1 });
db.scraping_results.createIndex({ 'status': 1 });
db.scraping_results.createIndex({ 'content.publishDate': -1 });

// User reports indexes
db.user_reports.createIndex({ 'userId': 1, 'createdAt': -1 });
db.user_reports.createIndex({ 'isPublic': 1, 'createdAt': -1 });
db.user_reports.createIndex({ 'title': 'text', 'content.summary': 'text' });

// Insert sample data

// Sample external references
db.external_references.insertMany([
    {
        sourceType: 'government',
        name: 'Congress.gov API',
        apiEndpoint: 'https://api.congress.gov/v3',
        description: 'Official US Congressional data',
        reliability: 0.98,
        accessMethod: 'api',
        rateLimit: '5000 per hour',
        requiresAuth: true,
        createdAt: new Date()
    },
    {
        sourceType: 'academic',
        name: 'PubMed API',
        apiEndpoint: 'https://eutils.ncbi.nlm.nih.gov/entrez/eutils',
        description: 'Medical research database',
        reliability: 0.92,
        accessMethod: 'api',
        rateLimit: '3 per second',
        requiresAuth: false,
        createdAt: new Date()
    },
    {
        sourceType: 'fact_checker',
        name: 'Snopes API',
        apiEndpoint: 'https://www.snopes.com/api',
        description: 'Fact-checking database',
        reliability: 0.85,
        accessMethod: 'api',
        rateLimit: '1000 per day',
        requiresAuth: true,
        createdAt: new Date()
    }
]);

// Sample article analysis
db.article_analyses.insertOne({
    articleUrl: 'https://example.com/sample-article',
    title: 'Sample News Article for Testing',
    content: 'This is a sample article content for testing the analysis system. It contains various claims that can be fact-checked.',
    sourceId: '123e4567-e89b-12d3-a456-426614174000',
    author: 'John Reporter',
    publishedDate: new Date('2024-01-15'),
    analyzedAt: new Date(),
    contentHash: 'sha256hashexample123',

    claimValidations: [
        {
            claimText: 'The unemployment rate is 3.5%',
            validationResult: 'accurate',
            confidenceScore: 0.95,
            authoritativeSource: 'https://bls.gov/data',
            lastVerified: new Date()
        },
        {
            claimText: 'This policy will definitely work',
            validationResult: 'unverifiable',
            confidenceScore: 0.1,
            authoritativeSource: null,
            lastVerified: new Date()
        }
    ],

    biasAnalysis: {
        overallBiasScore: 0.3,
        biasIndicators: [
            {
                type: 'emotional_language',
                text: 'devastating impact',
                severity: 'moderate',
                position: 245,
                score: 0.7
            },
            {
                type: 'loaded_terms',
                text: 'failed policy',
                severity: 'high',
                position: 180,
                score: 0.8
            }
        ],
        languagePatterns: {
            emotionalWords: 5,
            factualStatements: 12,
            opinions: 3,
            uncertainQualifiers: 2
        }
    },

    sourceReliability: {
        score: 0.82,
        historicalAccuracy: 0.79,
        biasScore: 0.15,
        lastUpdated: new Date()
    },

    logicalFallacies: [
        {
            type: 'ad_hominem',
            text: 'Critics, who are clearly biased...',
            confidence: 0.75,
            position: 156,
            explanation: 'Attacking the person rather than addressing the argument'
        }
    ],

    processingMetadata: {
        processingTimeMs: 2340,
        version: '1.0',
        modelVersions: {
            biasDetection: 'v2.1',
            factValidation: 'v1.5',
            logicalFallacyDetection: 'v1.2'
        }
    }
});

// Sample scraping result
db.scraping_results.insertOne({
    url: 'https://example.com/news-article',
    scrapedAt: new Date(),
    status: 'success',
    responseTime: 1250,
    content: {
        title: 'Breaking News: Important Development',
        body: 'Full article content goes here...',
        author: 'Jane Journalist',
        publishDate: new Date('2024-01-15'),
        metadata: {
            wordCount: 850,
            readingTime: 4,
            tags: ['politics', 'economics'],
            images: 2,
            videos: 0
        }
    },
    scrapingMetadata: {
        userAgent: 'NewsAnalyzer Bot 1.0',
        method: 'selenium',
        javascriptEnabled: true,
        cookies: false
    }
});

// Sample user report
db.user_reports.insertOne({
    userId: '456e7890-e89b-12d3-a456-426614174001',
    title: 'Analysis of Climate Change Claims in Media',
    createdAt: new Date(),
    updatedAt: new Date(),
    isPublic: true,
    status: 'published',
    content: {
        summary: 'This report analyzes the accuracy of climate change claims across major news outlets during January 2024.',
        methodology: 'Used automated fact-checking against IPCC reports and peer-reviewed research.',
        findings: [
            {
                finding: '85% of climate claims were factually accurate',
                evidence: 'Cross-referenced with IPCC AR6 report',
                confidence: 0.92
            },
            {
                finding: 'Conservative outlets showed 23% higher bias in climate reporting',
                evidence: 'Language analysis of 500 articles',
                confidence: 0.78
            }
        ],
        sources: [
            {
                type: 'government',
                name: 'IPCC Sixth Assessment Report',
                url: 'https://www.ipcc.ch/report/ar6/wg1/',
                reliability: 0.98
            },
            {
                type: 'academic',
                name: 'Nature Climate Change Journal',
                url: 'https://www.nature.com/nclimate/',
                reliability: 0.95
            }
        ],
        conclusions: 'While most climate reporting is factually accurate, significant bias exists in how facts are presented and contextualized.',
        recommendations: [
            'Readers should consult multiple sources across the political spectrum',
            'Pay attention to language choices and emotional framing',
            'Verify claims against authoritative scientific sources'
        ]
    },
    statistics: {
        articlesAnalyzed: 500,
        claimsVerified: 1250,
        averageAccuracy: 0.82,
        averageBias: 0.25
    },
    tags: ['climate', 'media-analysis', 'fact-checking'],
    visibility: 'public',
    allowComments: true
});

print('MongoDB collections created and sample data inserted successfully!');
print('Collections created:');
print('- article_analyses: ' + db.article_analyses.countDocuments());
print('- external_references: ' + db.external_references.countDocuments());
print('- scraping_results: ' + db.scraping_results.countDocuments());
print('- user_reports: ' + db.user_reports.countDocuments());
print('');
print('Indexes created for optimal query performance.');
print('Database initialization complete!');