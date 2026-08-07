import Fastify from 'fastify';
import cors from '@fastify/cors';
import { readFileSync, writeFileSync } from 'fs';
import { fileURLToPath } from 'url';
import { dirname, join } from 'path';

const __dirname = dirname(fileURLToPath(import.meta.url));

const dataPath = join(__dirname, 'data', 'medicamentos.json');
let medicamentos = JSON.parse(readFileSync(dataPath, 'utf-8'));

const bulasPath = join(__dirname, 'data', 'bulas.json');
let bulas = JSON.parse(readFileSync(bulasPath, 'utf-8'));

const interacoesPath = join(__dirname, 'data', 'interacoes.json');
const interacoes = JSON.parse(readFileSync(interacoesPath, 'utf-8'));

// Pristine copy used to reset state between tests.
const originalMedicamentos = JSON.parse(JSON.stringify(medicamentos));

function saveToFile() {
  writeFileSync(dataPath, JSON.stringify(medicamentos, null, 2), 'utf-8');
}

export function resetMedicamentos() {
  medicamentos.length = 0;
  medicamentos.push(...JSON.parse(JSON.stringify(originalMedicamentos)));
}

const app = Fastify({ logger: !process.env.VITEST });

await app.register(cors, { origin: '*' });

const API_KEY = process.env.MEDAPP_API_KEY || 'medapp-demo-2024';

app.addHook('preHandler', async (request, reply) => {
  if (request.url.startsWith('/health')) return;
  const key = request.headers['x-api-key'];
  if (key !== API_KEY) {
    return reply.code(401).send({ error: 'API key invalida ou ausente' });
  }
});

app.get('/health', async () => ({ status: 'ok', total: medicamentos.length }));

app.get('/medicamento/:ean', async (request, reply) => {
  const { ean } = request.params;
  const med = medicamentos.find((m) => m.ean === ean);
  if (!med) {
    return reply.code(404).send({ error: 'Medicamento nao encontrado' });
  }
  return med;
});

app.get('/medicamento/:ean/bula', async (request, reply) => {
  const { ean } = request.params;
  const bula = bulas.find((b) => b.ean === ean);
  if (!bula) {
    return reply.code(404).send({ error: 'Bula nao disponivel para este medicamento' });
  }
  return bula;
});

app.get('/medicamentos', async () => medicamentos);

app.get('/buscar', async (request) => {
  const q = (request.query.q || '').toLowerCase().trim();
  if (!q) return [];
  return medicamentos.filter(
    (m) =>
      m.nomeProduto.toLowerCase().includes(q) ||
      m.principioAtivo.toLowerCase().includes(q) ||
      m.categoria.toLowerCase().includes(q)
  );
});

app.get('/interacoes', async (request) => {
  const principio = (request.query.principioAtivo || '').toLowerCase().trim();
  if (!principio) return interacoes;
  return interacoes.filter(
    (i) =>
      i.principioAtivo.toLowerCase().includes(principio) ||
      i.interageCom.toLowerCase().includes(principio)
  );
});

app.post('/medicamento', async (request, reply) => {
  const body = request.body || {};

  if (!body.ean || !body.nomeProduto) {
    return reply.code(400).send({ error: 'ean e nomeProduto sao obrigatorios' });
  }

  const existing = medicamentos.find((m) => m.ean === body.ean);
  if (existing) {
    return reply.code(409).send({ error: 'EAN ja cadastrado', medicamento: existing });
  }

  const newMed = {
    ean: body.ean,
    nomeProduto: body.nomeProduto,
    razaoSocial: body.razaoSocial || 'Nao informado',
    numeroRegistro: body.numeroRegistro || 'Nao informado',
    principioAtivo: body.principioAtivo || body.nomeProduto,
    categoria: body.categoria || 'Nao informado',
    apresentacao: body.apresentacao || null,
  };

  medicamentos.push(newMed);
  saveToFile();
  return reply.code(201).send(newMed);
});

export { app };
