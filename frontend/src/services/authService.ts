import apiClient from './apiClient';
import type { RegisterRequest, LoginRequest, AddGuestRequest, AuthResponse, User } from '../models/user';

class AuthService {
  async register(userData: RegisterRequest): Promise<AuthResponse> {
    const response = await apiClient.post<{ token: string }>('/users/register', userData);
    
    if (response.data.token) {
      localStorage.setItem('authToken', response.data.token);
    }
    
    return { token: response.data.token };
  }

  async login(credentials: LoginRequest): Promise<AuthResponse> {
    const response = await apiClient.post<{ token: string }>('/users/authorize', credentials);
    
    if (response.data.token) {
      localStorage.setItem('authToken', response.data.token);
    }
    
    return { token: response.data.token };
  }

  async addGuest(guestData: AddGuestRequest): Promise<void> {
    await apiClient.post('/users/guest', guestData);
  }

  logout(): void {
    localStorage.removeItem('authToken');
  }

  getToken(): string | null {
    return localStorage.getItem('authToken');
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  getCurrentUserFromToken(): User | null {
    const token = this.getToken();
    if (!token) return null;

    try {
      const payloadBase64 = token.split('.')[1];
      const base64 = payloadBase64.replace(/-/g, '+').replace(/_/g, '/');
      const paddedBase64 = base64.padEnd(base64.length + (4 - base64.length % 4) % 4, '=');
      
      const payloadJson = atob(paddedBase64);
      const payload = JSON.parse(payloadJson);
      
      console.log('Raw payload from JWT:', payload);

      const fixRussianEncoding = (text: string): string => {
        if (!text) return text;
        
        if (text.includes('Ð') || text.includes('Ñ')) {
          try {
            const textBytes = new Uint8Array(text.length);
            for (let i = 0; i < text.length; i++) {
              textBytes[i] = text.charCodeAt(i);
            }
            const decoder = new TextDecoder('UTF-8');
            return decoder.decode(textBytes);
          } catch (e) {
            console.warn('Failed to fix Russian encoding:', e);
            return text;
          }
        }
        
        return text;
      };

      const fixedUser = {
        id: payload.id,
        fio: fixRussianEncoding(payload.fio),
        email: payload.email,
        password: '',
        residentialId: payload.residentialId,
        role: payload.role
      };

      console.log('Fixed user data:', fixedUser);
      
      return fixedUser;
    } catch (error) {
      console.error('Error parsing token:', error);
      return null;
    }
  }
}

export const authService = new AuthService();