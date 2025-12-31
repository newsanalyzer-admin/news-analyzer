import Link from 'next/link';

export default function Home() {
  return (
    <main className="flex min-h-screen flex-col items-center justify-center p-24">
      <div className="z-10 max-w-5xl w-full items-center justify-center font-mono text-sm">
        <h1 className="text-4xl font-bold text-center mb-4">
          NewsAnalyzer v2
        </h1>
        <p className="text-center text-muted-foreground mb-8">
          Independent news analysis, fact-checking, and bias detection
        </p>
        <div className="flex flex-wrap justify-center gap-4 mb-6">
          <Link
            href="/knowledge-base"
            className="px-8 py-4 bg-primary text-primary-foreground rounded-lg hover:bg-primary/90 font-semibold text-lg shadow-lg transition-all hover:shadow-xl"
          >
            Explore Knowledge Base →
          </Link>
          <Link
            href="/article-analyzer"
            className="px-8 py-4 bg-secondary text-secondary-foreground rounded-lg hover:bg-secondary/90 font-semibold text-lg shadow-lg transition-all hover:shadow-xl border"
          >
            Article Analyzer →
          </Link>
        </div>
        <div className="flex flex-wrap justify-center gap-4 mb-8">
          <Link
            href="/entities"
            className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-medium text-base"
          >
            Try Entity Extraction →
          </Link>
          <Link
            href="/members"
            className="px-6 py-3 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 font-medium text-base"
          >
            Browse Members →
          </Link>
          <Link
            href="/committees"
            className="px-6 py-3 bg-amber-600 text-white rounded-lg hover:bg-amber-700 font-medium text-base"
          >
            Browse Committees →
          </Link>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mt-8">
          <div className="border rounded-lg p-6">
            <h2 className="text-xl font-semibold mb-2">✅ Factual Accuracy</h2>
            <p className="text-sm text-muted-foreground">
              Cross-reference claims against authoritative sources
            </p>
          </div>
          <div className="border rounded-lg p-6">
            <h2 className="text-xl font-semibold mb-2">🧠 Logical Fallacies</h2>
            <p className="text-sm text-muted-foreground">
              Identify errors in reasoning using Prolog
            </p>
          </div>
          <div className="border rounded-lg p-6">
            <h2 className="text-xl font-semibold mb-2">🎭 Cognitive Biases</h2>
            <p className="text-sm text-muted-foreground">
              Detect emotional manipulation and framing
            </p>
          </div>
          <div className="border rounded-lg p-6">
            <h2 className="text-xl font-semibold mb-2">📊 Source Reliability</h2>
            <p className="text-sm text-muted-foreground">
              Track historical accuracy of news outlets
            </p>
          </div>
        </div>
        <div className="mt-8 text-center text-sm text-muted-foreground">
          <p>🇪🇺 Hosted independently in Europe • 🔓 Open Source • 🤝 Community Driven</p>
        </div>
      </div>
    </main>
  )
}
