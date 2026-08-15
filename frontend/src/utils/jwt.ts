/**
 * JWT helper utility functions.
 *
 * Provides client-side helpers for decoding and inspecting JWT tokens
 * without a full library dependency. Used primarily to check token
 * expiration before making API calls.
 *
 * Note: Token signature is NOT verified client-side — that is the
 * backend's responsibility. These helpers only decode the payload
 * for display and expiry checking purposes.
 *
 * @author DevLaunch
 */

/**
 * Decodes the payload portion of a JWT token without verifying
 * the signature. Returns null if the token is malformed.
 */
export function decodeJwtPayload(token: string): Record<string, unknown> | null {
  try {
    const parts = token.split('.');
    if (parts.length !== 3) return null;

    const payload = parts[1];
    const decoded = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
    return JSON.parse(decoded) as Record<string, unknown>;
  } catch {
    return null;
  }
}

/**
 * Extracts the expiration timestamp (in milliseconds since epoch)
 * from a JWT token. Returns null if the token is invalid or has
 * no exp claim.
 */
export function getTokenExpiry(token: string): number | null {
  const payload = decodeJwtPayload(token);
  if (!payload || typeof payload.exp !== 'number') return null;
  return payload.exp * 1000; // Convert seconds to milliseconds
}

/**
 * Checks whether a JWT token has expired.
 * Returns true if the token is invalid, missing, or past its expiry.
 */
export function isTokenExpired(token: string | null | undefined): boolean {
  if (!token) return true;
  const expiry = getTokenExpiry(token);
  if (expiry === null) return true;
  return Date.now() >= expiry;
}

/**
 * Extracts the username (subject) from a JWT token.
 * The backend sets the subject to the user's email.
 */
export function getTokenSubject(token: string): string | null {
  const payload = decodeJwtPayload(token);
  if (!payload || typeof payload.sub !== 'string') return null;
  return payload.sub;
}

/**
 * Calculates the number of seconds until the token expires.
 * Returns 0 if the token is already expired or invalid.
 */
export function getTokenTimeToLive(token: string): number {
  const expiry = getTokenExpiry(token);
  if (expiry === null) return 0;
  const remaining = expiry - Date.now();
  return Math.max(0, Math.floor(remaining / 1000));
}
