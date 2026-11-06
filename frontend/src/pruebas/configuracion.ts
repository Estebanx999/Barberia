import '@testing-library/jest-dom/vitest';
import { cleanup } from '@testing-library/react';
import { afterEach, vi } from 'vitest';
import { borrarToken } from '../sesion/almacen';

afterEach(() => {
  cleanup();
  borrarToken();
  vi.restoreAllMocks();
  vi.unstubAllGlobals();
});
