export interface LoginRequest {
  email: string;
  password: string;
}

export interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  roles: string[];
  companyId: number;
  companyName?: string;
  avatarUrl?: string;
}

export interface AuthResponse {
  token: string;
  refreshToken: string;
  user: User;
}

export interface TokenPayload {
  sub: string;
  exp: number;
  iat: number;
  roles: string[];
  companyId: number;
}
