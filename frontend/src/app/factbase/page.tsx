import Link from 'next/link';
import { Users, Building2, Scale, Gavel, Landmark, ArrowRight } from 'lucide-react';

export default function FactbasePage() {
  return (
    <div className="container mx-auto px-6 py-12 max-w-5xl">
      {/* Hero Section */}
      <div className="text-center mb-12">
        <h1 className="text-4xl font-bold tracking-tight mb-4">
          Welcome to the Factbase
        </h1>
        <p className="text-lg text-muted-foreground max-w-2xl mx-auto">
          Explore authoritative data about the United States federal government.
          Browse information about elected officials, appointed positions, and
          government organizations across all three branches.
        </p>
      </div>

      {/* Quick Links Grid */}
      <div className="grid md:grid-cols-2 gap-6 mb-12">
        {/* People Section */}
        <div className="border border-border rounded-lg p-6 bg-card">
          <div className="flex items-center gap-3 mb-4">
            <div className="p-2 rounded-md bg-primary/10">
              <Users className="h-6 w-6 text-primary" />
            </div>
            <h2 className="text-xl font-semibold">People</h2>
          </div>
          <p className="text-muted-foreground mb-4">
            Discover information about government officials and their positions.
          </p>
          <ul className="space-y-2">
            <li>
              <Link
                href="/factbase/people/congressional-members"
                className="flex items-center gap-2 text-sm hover:text-primary transition-colors"
              >
                <Scale className="h-4 w-4" />
                Congressional Members
                <ArrowRight className="h-3 w-3 ml-auto" />
              </Link>
            </li>
            <li>
              <Link
                href="/factbase/people/executive-appointees"
                className="flex items-center gap-2 text-sm hover:text-primary transition-colors"
              >
                <Landmark className="h-4 w-4" />
                Executive Appointees
                <ArrowRight className="h-3 w-3 ml-auto" />
              </Link>
            </li>
            <li>
              <Link
                href="/factbase/people/federal-judges"
                className="flex items-center gap-2 text-sm hover:text-primary transition-colors"
              >
                <Gavel className="h-4 w-4" />
                Federal Judges
                <ArrowRight className="h-3 w-3 ml-auto" />
              </Link>
            </li>
          </ul>
        </div>

        {/* Organizations Section */}
        <div className="border border-border rounded-lg p-6 bg-card">
          <div className="flex items-center gap-3 mb-4">
            <div className="p-2 rounded-md bg-primary/10">
              <Building2 className="h-6 w-6 text-primary" />
            </div>
            <h2 className="text-xl font-semibold">Organizations</h2>
          </div>
          <p className="text-muted-foreground mb-4">
            Explore federal agencies, departments, and institutions.
          </p>
          <ul className="space-y-2">
            <li>
              <Link
                href="/factbase/organizations/executive"
                className="flex items-center gap-2 text-sm hover:text-primary transition-colors"
              >
                <Landmark className="h-4 w-4" />
                Executive Branch
                <ArrowRight className="h-3 w-3 ml-auto" />
              </Link>
            </li>
            <li>
              <Link
                href="/factbase/organizations/legislative"
                className="flex items-center gap-2 text-sm hover:text-primary transition-colors"
              >
                <Scale className="h-4 w-4" />
                Legislative Branch
                <ArrowRight className="h-3 w-3 ml-auto" />
              </Link>
            </li>
            <li>
              <Link
                href="/factbase/organizations/judicial"
                className="flex items-center gap-2 text-sm hover:text-primary transition-colors"
              >
                <Gavel className="h-4 w-4" />
                Judicial Branch
                <ArrowRight className="h-3 w-3 ml-auto" />
              </Link>
            </li>
          </ul>
        </div>
      </div>

      {/* About Section */}
      <div className="bg-muted/50 rounded-lg p-6">
        <h2 className="text-lg font-semibold mb-3">About the Factbase</h2>
        <p className="text-sm text-muted-foreground">
          The Factbase aggregates data from authoritative sources including
          Congress.gov, the Federal Register, the Office of Personnel Management,
          and the Federal Judicial Center. All information is sourced from official
          government publications and APIs.
        </p>
      </div>
    </div>
  );
}
