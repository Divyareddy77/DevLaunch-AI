/**
 * Unit tests for the password strength utilities shared by the
 * reset-password form and the strength meter.
 *
 * @author DevLaunch
 */

import { describe, it, expect } from 'vitest';
import { MESSAGES } from '../constants/messages';
import { evaluatePasswordStrength, PASSWORD_RULES, passwordFieldSchema } from './validation';

describe('evaluatePasswordStrength', () => {
  it('returns score 0 for an empty password', () => {
    const { score, checks } = evaluatePasswordStrength('');
    expect(score).toBe(0);
    expect(checks.every((check) => !check.met)).toBe(true);
  });

  it('returns score 1 for a short password meeting few rules', () => {
    const { score } = evaluatePasswordStrength('abc');
    expect(score).toBe(1);
  });

  it('returns score 2 for a password meeting three rules', () => {
    const { score } = evaluatePasswordStrength('Abcdefgh');
    // Length + uppercase + lowercase = 3 rules.
    expect(score).toBe(2);
  });

  it('returns score 3 for a password meeting four rules', () => {
    const { score } = evaluatePasswordStrength('Abcdefg1');
    expect(score).toBe(3);
  });

  it('returns score 4 for a fully compliant password', () => {
    const { score, checks } = evaluatePasswordStrength('Password@123');
    expect(score).toBe(4);
    expect(checks.every((check) => check.met)).toBe(true);
  });

  it('flags a missing special character', () => {
    const { checks } = evaluatePasswordStrength('Password123');
    const special = checks.find((check) => check.pattern === PASSWORD_RULES.SPECIAL);
    expect(special?.met).toBe(false);
  });

  it('flags a missing uppercase letter', () => {
    const { checks } = evaluatePasswordStrength('password@123');
    const upper = checks.find((check) => check.pattern === PASSWORD_RULES.UPPERCASE);
    expect(upper?.met).toBe(false);
  });

  it('flags a missing number', () => {
    const { checks } = evaluatePasswordStrength('Password@abc');
    const number = checks.find((check) => check.pattern === PASSWORD_RULES.NUMBER);
    expect(number?.met).toBe(false);
  });
});

describe('passwordFieldSchema', () => {
  it('accepts a fully compliant password', () => {
    const result = passwordFieldSchema().safeParse('Password@123');
    expect(result.success).toBe(true);
  });

  it('rejects a password that is too short', () => {
    const result = passwordFieldSchema().safeParse('Abc@1');
    expect(result.success).toBe(false);
  });

  it('rejects a password longer than 100 characters', () => {
    const result = passwordFieldSchema().safeParse(`Password@1${'x'.repeat(100)}`);
    expect(result.success).toBe(false);
  });

  it('rejects a password with no uppercase letter', () => {
    const result = passwordFieldSchema().safeParse('password@123');
    expect(result.success).toBe(false);
  });

  it('rejects a password with no lowercase letter', () => {
    const result = passwordFieldSchema().safeParse('PASSWORD@123');
    expect(result.success).toBe(false);
  });

  it('rejects a password with no number', () => {
    const result = passwordFieldSchema().safeParse('Password@abc');
    expect(result.success).toBe(false);
  });

  it('rejects a password with no special character', () => {
    const result = passwordFieldSchema().safeParse('Password123');
    expect(result.success).toBe(false);
  });

  it('rejects a common weak password like divya123', () => {
    const result = passwordFieldSchema().safeParse('divya123');
    expect(result.success).toBe(false);
  });

  it('surfaces the user-friendly message for a missing uppercase letter', () => {
    const result = passwordFieldSchema().safeParse('password@123');
    expect(result.success).toBe(false);
    if (!result.success) {
      const messages = result.error.issues.map((issue) => issue.message);
      expect(messages).toContain(MESSAGES.PASSWORD_UPPERCASE);
    }
  });
});
