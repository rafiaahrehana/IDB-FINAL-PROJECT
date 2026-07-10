export interface LoginRequest {
  email: string;
  password: string;
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
}

export interface TokenPayload {
  sub: string;
  exp: number;
  iat: number;
  roles: string[];
  companyId: number;
}
