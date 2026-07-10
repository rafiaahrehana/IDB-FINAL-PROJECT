export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  companyName: string;
  subdomain: string;
  companyPhone?: string;
  country?: string;
  level1?: string;
  level2?: string;
  level3?: string;
  level4?: string;
  streetAddress?: string;
  postalCode?: string;
  apartment?: string;
}
 
export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  userId: number;
  firstName: string;
  email: string;
  role: string;
  companyId: number;
}
 
export interface User {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  image?: string;
  role: string;
  companyId?: number;
  languagePreference?: string;
  customRoleId?: number;
  customRoleName?: string;
}
 
export interface TokenPayload {
  sub: string;
  roles: string[];
  companyId: number;
  exp: number;
  iat: number;
}