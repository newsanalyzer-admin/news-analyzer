/**
 * Page Descriptions Configuration
 *
 * Central configuration for all factbase page titles and educational descriptions.
 * Used by ContentPageHeader to maintain consistent messaging across the site.
 */

export interface PageDescription {
  /** Page title displayed as h1 */
  title: string;
  /** Educational description explaining the page content */
  description: string;
}

/**
 * Page descriptions keyed by route path.
 */
export const pageDescriptions: Record<string, PageDescription> = {
  // ===================
  // People Section
  // ===================

  '/factbase/people': {
    title: 'People',
    description:
      'Explore information about government officials serving in the federal government. Browse Congressional members, executive appointees, and federal judges across all three branches of government.',
  },

  '/factbase/people/congressional-members': {
    title: 'Congressional Members',
    description:
      'Congressional Members are the 535 elected officials who serve in the United States Congress—100 Senators (2 per state) and 435 Representatives (apportioned by population). Congress is the legislative branch of government, responsible for writing and passing federal laws, controlling the federal budget, and providing oversight of the executive branch. Members serve terms of 6 years (Senate) or 2 years (House).',
  },

  '/factbase/people/executive-appointees': {
    title: 'Executive Appointees',
    description:
      'Executive Appointees are individuals appointed by the President to serve in leadership positions across the executive branch. This includes Cabinet secretaries, agency heads, ambassadors, and thousands of other positions that help run the federal government. Some positions require Senate confirmation (PAS - Presidential Appointment with Senate Confirmation), while others do not. The Plum Book, published after each presidential election, catalogs these positions.',
  },

  '/factbase/people/federal-judges': {
    title: 'Federal Judges & Justices',
    description:
      'Federal Judges and Justices serve in the judicial branch of the United States government. This includes the 9 Supreme Court Justices, approximately 179 judges on the Courts of Appeals (Circuit Courts), and roughly 670 judges on the District Courts. Federal judges are nominated by the President and confirmed by the Senate, serving lifetime appointments "during good behavior." Data is sourced from the Federal Judicial Center\'s Biographical Directory of Article III Federal Judges.',
  },

  // ===================
  // Organizations Section
  // ===================

  '/factbase/organizations': {
    title: 'Federal Government Organizations',
    description:
      'Explore the structure of the United States federal government. Browse agencies, departments, and institutions across the Executive, Legislative, and Judicial branches.',
  },

  '/factbase/organizations/executive': {
    title: 'Executive Branch',
    description:
      'The Executive Branch is headed by the President and is responsible for implementing and enforcing federal laws. It includes 15 executive departments (such as State, Treasury, and Defense), hundreds of agencies, and millions of federal employees. The President also oversees independent agencies like the EPA, NASA, and CIA, as well as regulatory commissions that operate with some independence from presidential control.',
  },

  '/factbase/organizations/legislative': {
    title: 'Legislative Branch',
    description:
      'The Legislative Branch consists of the United States Congress, which is divided into two chambers: the Senate (100 members, 2 per state) and the House of Representatives (435 members, apportioned by population). Congress is responsible for writing federal laws, declaring war, regulating commerce, controlling federal spending, and providing oversight of the executive branch. Supporting organizations include the Library of Congress, Government Accountability Office, and Congressional Budget Office.',
  },

  '/factbase/organizations/judicial': {
    title: 'Judicial Branch',
    description:
      'The Judicial Branch interprets the Constitution and federal laws through the federal court system. It consists of the Supreme Court (the highest court), 13 Courts of Appeals (Circuit Courts), 94 District Courts, and specialized courts like the Court of International Trade and Bankruptcy Courts. The Administrative Office of the U.S. Courts and Federal Judicial Center support court operations and judicial education.',
  },
};

/**
 * Get page description by path.
 * Returns undefined if path not found.
 */
export function getPageDescription(path: string): PageDescription | undefined {
  return pageDescriptions[path];
}

/**
 * Get page description with fallback.
 * Returns a default description if path not found.
 */
export function getPageDescriptionOrDefault(
  path: string,
  fallback: PageDescription = { title: 'Factbase', description: '' }
): PageDescription {
  return pageDescriptions[path] ?? fallback;
}
