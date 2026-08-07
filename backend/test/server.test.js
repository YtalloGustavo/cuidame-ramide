import { describe, it, expect, beforeEach, afterAll } from 'vitest';
import { readFileSync, writeFileSync } from 'fs';
import { fileURLToPath } from 'url';
import { dirname, join } from 'path';
import { app, resetMedicamentos } from '../src/app.js';

const __dirname = dirname(fileURLToPath(import.meta.url));
const dataPath = join(__dirname, '..', 'src', 'data', 'medicamentos.json');

const originalFileContent = readFileSync(dataPath, 'utf-8');

const AUTH = { 'x-api-key': 'medapp-demo-2024' };

describe('MedApp API', () => {
  beforeEach(() => {
    resetMedicamentos();
  });

  afterAll(() => {
    writeFileSync(dataPath, originalFileContent, 'utf-8');
  });

  it('GET /health returns status ok and total count (no auth needed)', async () => {
    const res = await app.inject({ method: 'GET', url: '/health' });
    expect(res.statusCode).toBe(200);
    const body = res.json();
    expect(body.status).toBe('ok');
    expect(body.total).toBe(15);
  });

  it('GET /medicamentos returns 401 without API key', async () => {
    const res = await app.inject({ method: 'GET', url: '/medicamentos' });
    expect(res.statusCode).toBe(401);
  });

  it('GET /medicamentos returns 200 with valid API key', async () => {
    const res = await app.inject({ method: 'GET', url: '/medicamentos', headers: AUTH });
    expect(res.statusCode).toBe(200);
  });

  it('GET /medicamento/:ean returns medicine for known EAN (7891000100011)', async () => {
    const res = await app.inject({ method: 'GET', url: '/medicamento/7891000100011', headers: AUTH });
    expect(res.statusCode).toBe(200);
    const body = res.json();
    expect(body.ean).toBe('7891000100011');
    expect(body.nomeProduto).toBe('Losartana Potassica 50mg');
  });

  it('GET /medicamento/:ean returns 404 for unknown EAN', async () => {
    const res = await app.inject({ method: 'GET', url: '/medicamento/0000000000000', headers: AUTH });
    expect(res.statusCode).toBe(404);
    expect(res.json().error).toBeDefined();
  });

  it('GET /medicamento/:ean/bula returns bula for EAN with bula (7891000100011)', async () => {
    const res = await app.inject({ method: 'GET', url: '/medicamento/7891000100011/bula', headers: AUTH });
    expect(res.statusCode).toBe(200);
    const body = res.json();
    expect(body.ean).toBe('7891000100011');
    expect(body.paraQueServe).toBeDefined();
  });

  it('GET /medicamento/:ean/bula returns 404 for EAN without bula (7891000600061)', async () => {
    const res = await app.inject({ method: 'GET', url: '/medicamento/7891000600061/bula', headers: AUTH });
    expect(res.statusCode).toBe(404);
    expect(res.json().error).toBeDefined();
  });

  it('GET /medicamentos returns array with 15 items', async () => {
    const res = await app.inject({ method: 'GET', url: '/medicamentos', headers: AUTH });
    expect(res.statusCode).toBe(200);
    const body = res.json();
    expect(Array.isArray(body)).toBe(true);
    expect(body).toHaveLength(15);
  });

  it('GET /buscar?q=losartana returns matching results', async () => {
    const res = await app.inject({ method: 'GET', url: '/buscar?q=losartana', headers: AUTH });
    expect(res.statusCode).toBe(200);
    const body = res.json();
    expect(Array.isArray(body)).toBe(true);
    expect(body.length).toBeGreaterThanOrEqual(1);
  });

  it('GET /buscar with empty q returns empty array', async () => {
    const res = await app.inject({ method: 'GET', url: '/buscar', headers: AUTH });
    expect(res.statusCode).toBe(200);
    expect(res.json()).toEqual([]);
  });

  it('GET /interacoes?principioAtivo=losartana returns interactions', async () => {
    const res = await app.inject({ method: 'GET', url: '/interacoes?principioAtivo=losartana', headers: AUTH });
    expect(res.statusCode).toBe(200);
    const body = res.json();
    expect(Array.isArray(body)).toBe(true);
    expect(body.length).toBeGreaterThanOrEqual(3);
  });

  it('POST /medicamento creates new medicine (201)', async () => {
    const payload = {
      ean: '7899999999999',
      nomeProduto: 'Test Medicamento',
      razaoSocial: 'Lab Teste',
      principioAtivo: 'Principio Teste',
      categoria: 'Categoria Teste',
    };
    const res = await app.inject({ method: 'POST', url: '/medicamento', payload, headers: AUTH });
    expect(res.statusCode).toBe(201);
    const body = res.json();
    expect(body.ean).toBe('7899999999999');
    expect(body.nomeProduto).toBe('Test Medicamento');
  });

  it('POST /medicamento returns 400 when missing ean', async () => {
    const payload = { nomeProduto: 'Sem Ean' };
    const res = await app.inject({ method: 'POST', url: '/medicamento', payload, headers: AUTH });
    expect(res.statusCode).toBe(400);
    expect(res.json().error).toBeDefined();
  });

  it('POST /medicamento returns 409 when EAN already exists', async () => {
    const payload = {
      ean: '7891000100011',
      nomeProduto: 'Duplicado',
    };
    const res = await app.inject({ method: 'POST', url: '/medicamento', payload, headers: AUTH });
    expect(res.statusCode).toBe(409);
    const body = res.json();
    expect(body.error).toBeDefined();
    expect(body.medicamento.ean).toBe('7891000100011');
  });
});
