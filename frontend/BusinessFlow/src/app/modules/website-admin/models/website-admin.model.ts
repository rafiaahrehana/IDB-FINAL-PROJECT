export interface SocialLink {
  platform: string;
  url: string;
  icon: string;
}

export interface Stat {
  value: string;
  label: string;
  icon: string;
}

export interface WebsiteSettings {
  id?: number;
  companyId?: number;
  companyName?: string;
  logoUrl?: string;
  faviconUrl?: string;
  tagline?: string;

  // Theme / branding
  primaryColor?: string;
  secondaryColor?: string;
  gradient?: string;
  font?: string;
  radius?: number;
  buttonStyle?: string; // rounded | pill | square
  darkMode?: boolean;
  navbarStyle?: string; // solid | transparent | glass
  footerStyle?: string; // dark | light
  animations?: boolean;
  spacing?: number;

  // Hero
  heroHeading?: string;
  heroSubheading?: string;
  heroImageUrl?: string;
  heroImages: string[];

  // Content
  aboutText?: string;
  mission?: string;
  vision?: string;

  // Contact
  email?: string;
  phone?: string;
  address?: string;
  mapEmbedUrl?: string;
  whatsapp?: string;

  socialLinks: SocialLink[];
  stats: Stat[];
  copyright?: string;

  // SEO
  seoTitle?: string;
  seoDescription?: string;
  seoKeywords?: string;
  ogImage?: string;
}

export type ContentType = 'PAGE' | 'POST';

export interface WebsiteContent {
  id?: number;
  companyId?: number;
  type?: ContentType;
  slug: string;
  title: string;
  body?: string;
  excerpt?: string;
  coverImageUrl?: string;
  author?: string;
  publishedAt?: string;
  category?: string;
  readMinutes?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface Faq {
  id?: number;
  companyId?: number;
  question: string;
  answer: string;
  category?: string;
}

export interface NavItem {
  id?: number;
  companyId?: number;
  label: string;
  url: string;
  external?: boolean;
  parentId?: number | null;
  sortOrder?: number;
  mega?: boolean;
}

export type PersonType = 'TEAM_MEMBER' | 'TESTIMONIAL';

export interface WebsitePerson {
  id?: number;
  companyId?: number;
  type?: PersonType;
  name: string;
  role?: string;
  // TEAM_MEMBER fields
  bio?: string;
  photoUrl?: string;
  email?: string;
  // TESTIMONIAL fields
  company?: string;
  quote?: string;
  avatarUrl?: string;
  rating?: number;
}

export interface PortalProject {
  id?: number;
  companyId?: number;
  title: string;
  summary?: string;
  description?: string;
  coverImageUrl?: string;
  client?: string;
  category?: string;
  year?: number;
  tags: string[];
}

export interface PricingPlan {
  id?: number;
  companyId?: number;
  name: string;
  description?: string;
  price?: string;
  period?: string;
  cta?: string;
  featured?: boolean;
  features: string[];
}

export interface WebsiteService {
  id?: number;
  companyId?: number;
  slug: string;
  title: string;
  summary?: string;
  description?: string;
  icon?: string;
  imageUrl?: string;
  categoryName?: string;
  startingPrice?: string;
  estimatedTime?: string;
  requirements?: string;
  features: string[];
}
