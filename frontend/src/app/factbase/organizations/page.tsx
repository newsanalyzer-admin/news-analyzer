import Link from 'next/link';
import { Building2, Scale, Landmark, Gavel, ArrowRight } from 'lucide-react';
import { ContentPageHeader } from '@/components/public';
import { getPageDescriptionOrDefault } from '@/lib/page-descriptions';

export default function OrganizationsPage() {
  const { title, description } = getPageDescriptionOrDefault('/factbase/organizations');

  return (
    <div className="container mx-auto px-6 py-8 max-w-5xl">
      <ContentPageHeader
        title={title}
        description={description}
        breadcrumbs={[
          { label: 'Factbase', href: '/factbase' },
          { label: 'Organizations' },
        ]}
      />

      {/* Branch Categories */}
      <div className="grid md:grid-cols-3 gap-6">
        {/* Executive Branch */}
        <Link
          href="/factbase/organizations/executive"
          className="group block border border-border rounded-lg p-6 bg-card hover:border-primary/50 hover:shadow-md transition-all"
        >
          <div className="flex items-center gap-3 mb-4">
            <div className="p-2 rounded-md bg-blue-100 group-hover:bg-blue-200 transition-colors">
              <Building2 className="h-6 w-6 text-blue-700" />
            </div>
            <h2 className="text-lg font-semibold group-hover:text-primary transition-colors">
              Executive Branch
            </h2>
          </div>
          <p className="text-sm text-muted-foreground mb-4">
            Federal departments, agencies, and offices that implement and enforce federal laws under the President.
          </p>
          <div className="flex items-center text-sm text-primary font-medium">
            Browse Executive
            <ArrowRight className="h-4 w-4 ml-1 group-hover:translate-x-1 transition-transform" />
          </div>
        </Link>

        {/* Legislative Branch */}
        <Link
          href="/factbase/organizations/legislative"
          className="group block border border-border rounded-lg p-6 bg-card hover:border-primary/50 hover:shadow-md transition-all"
        >
          <div className="flex items-center gap-3 mb-4">
            <div className="p-2 rounded-md bg-amber-100 group-hover:bg-amber-200 transition-colors">
              <Landmark className="h-6 w-6 text-amber-700" />
            </div>
            <h2 className="text-lg font-semibold group-hover:text-primary transition-colors">
              Legislative Branch
            </h2>
          </div>
          <p className="text-sm text-muted-foreground mb-4">
            Congress and its supporting agencies that create federal laws, including the Senate, House, and legislative offices.
          </p>
          <div className="flex items-center text-sm text-primary font-medium">
            Browse Legislative
            <ArrowRight className="h-4 w-4 ml-1 group-hover:translate-x-1 transition-transform" />
          </div>
        </Link>

        {/* Judicial Branch */}
        <Link
          href="/factbase/organizations/judicial"
          className="group block border border-border rounded-lg p-6 bg-card hover:border-primary/50 hover:shadow-md transition-all"
        >
          <div className="flex items-center gap-3 mb-4">
            <div className="p-2 rounded-md bg-purple-100 group-hover:bg-purple-200 transition-colors">
              <Gavel className="h-6 w-6 text-purple-700" />
            </div>
            <h2 className="text-lg font-semibold group-hover:text-primary transition-colors">
              Judicial Branch
            </h2>
          </div>
          <p className="text-sm text-muted-foreground mb-4">
            Federal courts that interpret laws and administer justice, from District Courts to the Supreme Court.
          </p>
          <div className="flex items-center text-sm text-primary font-medium">
            Browse Judicial
            <ArrowRight className="h-4 w-4 ml-1 group-hover:translate-x-1 transition-transform" />
          </div>
        </Link>
      </div>
    </div>
  );
}
