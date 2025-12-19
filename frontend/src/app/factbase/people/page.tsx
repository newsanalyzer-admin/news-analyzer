import Link from 'next/link';
import { Scale, Landmark, Gavel, ArrowRight } from 'lucide-react';
import { ContentPageHeader } from '@/components/public';
import { getPageDescriptionOrDefault } from '@/lib/page-descriptions';

export default function PeoplePage() {
  const { title, description } = getPageDescriptionOrDefault('/factbase/people');

  return (
    <div className="container mx-auto px-6 py-8 max-w-5xl">
      <ContentPageHeader
        title={title}
        description={description}
        breadcrumbs={[
          { label: 'Factbase', href: '/factbase' },
          { label: 'People' },
        ]}
      />

      {/* People Categories */}
      <div className="grid md:grid-cols-3 gap-6">
        {/* Congressional Members */}
        <Link
          href="/factbase/people/congressional-members"
          className="group block border border-border rounded-lg p-6 bg-card hover:border-primary/50 hover:shadow-md transition-all"
        >
          <div className="flex items-center gap-3 mb-4">
            <div className="p-2 rounded-md bg-blue-100 group-hover:bg-blue-200 transition-colors">
              <Scale className="h-6 w-6 text-blue-700" />
            </div>
            <h2 className="text-lg font-semibold group-hover:text-primary transition-colors">
              Congressional Members
            </h2>
          </div>
          <p className="text-sm text-muted-foreground mb-4">
            535 elected officials serving in the U.S. Congress - 100 Senators and 435 Representatives.
          </p>
          <div className="flex items-center text-sm text-primary font-medium">
            Browse Members
            <ArrowRight className="h-4 w-4 ml-1 group-hover:translate-x-1 transition-transform" />
          </div>
        </Link>

        {/* Executive Appointees */}
        <Link
          href="/factbase/people/executive-appointees"
          className="group block border border-border rounded-lg p-6 bg-card hover:border-primary/50 hover:shadow-md transition-all"
        >
          <div className="flex items-center gap-3 mb-4">
            <div className="p-2 rounded-md bg-amber-100 group-hover:bg-amber-200 transition-colors">
              <Landmark className="h-6 w-6 text-amber-700" />
            </div>
            <h2 className="text-lg font-semibold group-hover:text-primary transition-colors">
              Executive Appointees
            </h2>
          </div>
          <p className="text-sm text-muted-foreground mb-4">
            Presidential appointees serving in leadership positions across federal agencies.
          </p>
          <div className="flex items-center text-sm text-primary font-medium">
            Browse Appointees
            <ArrowRight className="h-4 w-4 ml-1 group-hover:translate-x-1 transition-transform" />
          </div>
        </Link>

        {/* Federal Judges */}
        <Link
          href="/factbase/people/federal-judges"
          className="group block border border-border rounded-lg p-6 bg-card hover:border-primary/50 hover:shadow-md transition-all"
        >
          <div className="flex items-center gap-3 mb-4">
            <div className="p-2 rounded-md bg-purple-100 group-hover:bg-purple-200 transition-colors">
              <Gavel className="h-6 w-6 text-purple-700" />
            </div>
            <h2 className="text-lg font-semibold group-hover:text-primary transition-colors">
              Federal Judges
            </h2>
          </div>
          <p className="text-sm text-muted-foreground mb-4">
            Judges and Justices serving on federal courts including Supreme, Appeals, and District Courts.
          </p>
          <div className="flex items-center text-sm text-primary font-medium">
            Browse Judges
            <ArrowRight className="h-4 w-4 ml-1 group-hover:translate-x-1 transition-transform" />
          </div>
        </Link>
      </div>
    </div>
  );
}
