/**
 * Guarda el token en sessionStorage: sobrevive a recargar la página y se borra
 * al cerrar la pestaña. El acceso puede fallar (modo privado, almacenamiento
 * bloqueado); en ese caso la sesión solo dura mientras la página esté abierta.
 */
const CLAVE = 'barberia.token';
let tokenEnMemoria: string | null = null;

export function leerToken(): string | null {
  try {
    return sessionStorage.getItem(CLAVE) ?? tokenEnMemoria;
  } catch {
    return tokenEnMemoria;
  }
}

export function guardarToken(token: string): void {
  tokenEnMemoria = token;
  try {
    sessionStorage.setItem(CLAVE, token);
  } catch {
    // Sin almacenamiento disponible: queda solo en memoria.
  }
}

export function borrarToken(): void {
  tokenEnMemoria = null;
  try {
    sessionStorage.removeItem(CLAVE);
  } catch {
    // Nada que borrar.
  }
}
