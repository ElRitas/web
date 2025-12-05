import { describe, it, expect, vi, beforeEach } from 'vitest';
import { authService } from '../authService';

vi.mock('../apiClient', () => {
  const mockApiClient = {
    post: vi.fn(),
    get: vi.fn(),
    put: vi.fn(), 
    patch: vi.fn(),
    delete: vi.fn(),
  };
  return { default: mockApiClient };
});

import apiClient from '../apiClient';

describe('AuthService', () => {
  const localStorageMock = {
    getItem: vi.fn(),
    setItem: vi.fn(),
    removeItem: vi.fn(),
    clear: vi.fn(),
  };

  const atobMock = vi.fn();
  const btoaMock = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
    
    // Мокаем window объекты без global
    Object.defineProperty(window, 'localStorage', {
      value: localStorageMock,
      writable: true
    });

    Object.defineProperty(window, 'atob', {
      value: atobMock,
      writable: true
    });

    Object.defineProperty(window, 'btoa', {
      value: btoaMock,
      writable: true
    });
  });

  describe('register', () => {
    it('должен регистрировать пользователя и сохранять токен', async () => {
      const userData = {
        name: 'Test User',
        email: 'test@example.com',
        password: 'password123',
        address: 'Test Address'
      };

      (apiClient.post as any).mockResolvedValue({ data: { token: 'test-token' } });

      const result = await authService.register(userData);

      expect(apiClient.post).toHaveBeenCalledWith('/users/register', userData);
      expect(localStorageMock.setItem).toHaveBeenCalledWith('authToken', 'test-token');
      expect(result).toEqual({ token: 'test-token' });
    });

    it('должен обрабатывать ошибку регистрации', async () => {
      const userData = {
        name: 'Test User',
        email: 'test@example.com', 
        password: 'password123',
        address: 'Test Address'
      };

      (apiClient.post as any).mockRejectedValue(new Error('Registration failed'));

      await expect(authService.register(userData)).rejects.toThrow('Registration failed');
    });
  });

  describe('login', () => {
    it('должен логинить пользователя и сохранять токен', async () => {
      const credentials = {
        email: 'test@example.com',
        password: 'password123'
      };

      (apiClient.post as any).mockResolvedValue({ data: { token: 'test-token' } });

      const result = await authService.login(credentials);

      expect(apiClient.post).toHaveBeenCalledWith('/users/authorize', credentials);
      expect(localStorageMock.setItem).toHaveBeenCalledWith('authToken', 'test-token');
      expect(result).toEqual({ token: 'test-token' });
    });

    it('должен обрабатывать ошибку логина', async () => {
      const credentials = {
        email: 'test@example.com',
        password: 'password123'
      };

      (apiClient.post as any).mockRejectedValue(new Error('Login failed'));

      await expect(authService.login(credentials)).rejects.toThrow('Login failed');
    });
  });

  describe('addGuest', () => {
    it('должен добавлять гостя', async () => {
      const guestData = {
        fio: 'Guest User',
        email: 'guest@example.com',
        password: 'guest123'
      };

      (apiClient.post as any).mockResolvedValue({ data: {} });

      await authService.addGuest(guestData);

      expect(apiClient.post).toHaveBeenCalledWith('/users/guest', guestData);
    });

    it('должен обрабатывать ошибку добавления гостя', async () => {
      const guestData = {
        fio: 'Guest User',
        email: 'guest@example.com',
        password: 'guest123'
      };

      (apiClient.post as any).mockRejectedValue(new Error('Add guest failed'));

      await expect(authService.addGuest(guestData)).rejects.toThrow('Add guest failed');
    });
  });

  describe('logout', () => {
    it('должен удалять токен из localStorage', () => {
      authService.logout();
      expect(localStorageMock.removeItem).toHaveBeenCalledWith('authToken');
    });
  });

  describe('getToken', () => {
    it('должен возвращать токен из localStorage', () => {
      localStorageMock.getItem.mockReturnValue('test-token');
      const token = authService.getToken();
      expect(localStorageMock.getItem).toHaveBeenCalledWith('authToken');
      expect(token).toBe('test-token');
    });

    it('должен возвращать null если токена нет', () => {
      localStorageMock.getItem.mockReturnValue(null);
      const token = authService.getToken();
      expect(token).toBeNull();
    });
  });

  describe('isAuthenticated', () => {
    it('должен возвращать true если есть токен', () => {
      localStorageMock.getItem.mockReturnValue('test-token');
      const isAuth = authService.isAuthenticated();
      expect(isAuth).toBe(true);
    });

    it('должен возвращать false если нет токена', () => {
      localStorageMock.getItem.mockReturnValue(null);
      const isAuth = authService.isAuthenticated();
      expect(isAuth).toBe(false);
    });
  });

  describe('getCurrentUserFromToken', () => {
    it('должен возвращать null если нет токена', () => {
      localStorageMock.getItem.mockReturnValue(null);
      const user = authService.getCurrentUserFromToken();
      expect(user).toBeNull();
    });

    it('должен парсить валидный JWT токен', () => {
      const payload = {
        id: 'user-123',
        fio: 'Test User',
        email: 'test@example.com',
        residentialId: 'res-123',
        role: 'RESIDENT'
      };
      
      const payloadString = JSON.stringify(payload);
      btoaMock.mockReturnValue('encoded-payload');
      atobMock.mockReturnValue(payloadString);

      localStorageMock.getItem.mockReturnValue('header.encoded-payload.signature');

      const user = authService.getCurrentUserFromToken();

      expect(user).toEqual({
        id: 'user-123',
        fio: 'Test User',
        email: 'test@example.com',
        password: '',
        residentialId: 'res-123',
        role: 'RESIDENT'
      });
    });

    it('должен обрабатывать невалидный JWT токен', () => {
      localStorageMock.getItem.mockReturnValue('invalid-token');
      atobMock.mockImplementation(() => {
        throw new Error('Invalid token');
      });

      const user = authService.getCurrentUserFromToken();
      
      expect(user).toBeNull();
    });

    it('должен обрабатывать некорректный JSON в токене', () => {
      localStorageMock.getItem.mockReturnValue('header.encoded-payload.signature');
      atobMock.mockReturnValue('invalid-json');

      const user = authService.getCurrentUserFromToken();
      
      expect(user).toBeNull();
    });
  });
});