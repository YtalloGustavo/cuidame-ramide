# CuidaME – API Local: Guia de Uso e Testes

## Sumário

1. [Changelog](#1-changelog)
2. [Arquitetura em resumo](#2-arquitetura-em-resumo)
3. [Pré-requisitos](#3-pré-requisitos)
4. [Subindo a API localmente](#4-subindo-a-api-localmente)
5. [Autenticação com API key](#5-autenticação-com-api-key)
6. [Rotas disponíveis](#6-rotas-disponíveis)
7. [Medicamentos disponíveis (EANs para testar)](#7-medicamentos-disponíveis-eans-para-testar)
8. [Testando no app Android (emulador)](#8-testando-no-app-android-emulador)
9. [Testes automatizados](#9-testes-automatizados)
10. [Dicas e resolução de problemas](#10-dicas-e-resolução-de-problemas)

---

## 1. Changelog

Todas as mudanças notáveis também estão em [`../CHANGELOG.md`](../CHANGELOG.md).

### [1.0.0] – 2026-08-07

**Adicionado**

- Cadastro de medicamentos por **código de barras** (CameraX + ML Kit Barcode Scanning)
- Busca de medicamentos por **EAN** (`GET /medicamento/:ean`)
- Busca de medicamentos por **nome/princípio ativo** (`GET /buscar?q=`) com debounce de 400ms
- Lista de **medicamentos populares** com card clicável
- API própria Node.js + **Fastify** em `backend/` com **15 medicamentos reais (EAN-13 válido)**
- Cadastro de novo medicamento no app → **persistido automaticamente na API** (`POST /medicamento`)
- Banco de dados local **Room** (SQLite) com `DoseEntity` e `CaregiverEntity`
- **Bula integrada**: `GET /medicamento/:ean/bula` com 3 abas (Resumo, Bula Completa, Interações)
- **Interações medicamentosas**: `GET /interacoes` com alerta âmbar (moderada) / vermelho (grave)
- **Lembretes via notificação**:
  - `AlarmManager` + canal de notificação
  - Ações na notificação: **Tomar** e **Adiar 30 min**
  - Permissão `POST_NOTIFICATIONS` (Android 13+) solicitada em runtime
  - Reagendamento após reboot (`BootReceiver`) e cancelamento ao editar/excluir dose
- **Testes automatizados**: API (Vitest, 14 testes) + Android (MockK/Robolectric/Room in-memory)

**Corrigido:** incompatibilidade Room 2.6.1 × Kotlin 2.2.10 (migrado para 2.7.0);
notificações descartadas por falta de permissão runtime; alarmes perdidos após reboot
e para doses tomadas/adiadas/excluídas.

---

## 2. Arquitetura em resumo

```
┌─────────────┐   HTTP (cleartext)     ┌─────────────────────┐
│ App Android │ ─────────────────────► │  API Fastify (node) │
│  (Retrofit) │   Base: 10.0.2.2:3000  │  porta 3000         │
└─────────────┘                        └─────────┬───────────┤
                                                 │
                                 medicamentos.json / bulas.json / interacoes.json
```

- O app conecta sempre em `http://10.0.2.2:3000/` (definido em `app/src/main/java/com/example/medapp/di/NetworkModule.kt`).
- `10.0.2.2` é o alias do host da máquina **visto de dentro do emulador Android**. Em dispositivo físico, troque por `http://<IP-da-sua-rede>:3000/`.

---

## 3. Pré-requisitos

| Item | Versão mínima | Como verificar |
|---|---|---|
| Node.js | 18+ | `node --version` |
| npm | 9+ | `npm --version` |
| JDK + Android Studio | — | `java -version` |
| Android Emulator (Pixel 7) | API 33+ | no Android Studio |

---

## 4. Subindo a API localmente

```bash
# 1) Entrar na pasta do backend
cd backend

# 2) Instalar dependências (fastify, cors, vitest)
npm install

# 3) Iniciar o servidor (fica ouvindo em 0.0.0.0:3000)
npm start
#    — ou, com hot-reload durante o desenvolvimento:
#    npm run dev
```

Saída esperada:

```
MedApp API rodando em http://localhost:3000
```

Verificação rápida:

```bash
curl http://localhost:3000/health
# {"status":"ok","total":15}
```

> Também é possível trocar a porta e a API key por variáveis de ambiente:
> `PORT=8080 MEDAPP_API_KEY=minha-chave npm start`.

---

## 5. Autenticação com API key

Toda rota, exceto `/health`, exige o header:

```
x-api-key: medapp-demo-2024
```

Sem o header, a API responde `401`:

```bash
curl http://localhost:3000/medicamentos
# {"error":"API key invalida ou ausente"}
```

Exemplo correto:

```bash
curl -H "x-api-key: medapp-demo-2024" http://localhost:3000/medicamentos
```

> O aplicativo já envia esse header automaticamente (interceptor OkHttp no `NetworkModule.kt`).
> Em testes manuais via curl/PowerShell, você precisa enviar manualmente.

---

## 6. Rotas disponíveis

| Método | Rota | Descrição | Autenticação |
|---|---|---|---|
| GET | `/health` | Status da API + total de medicamentos | Não |
| GET | `/medicamento/:ean` | Detalhes de um medicamento pelo EAN | Sim |
| GET | `/medicamento/:ean/bula` | Bula completa de um medicamento | Sim |
| GET | `/medicamentos` | Lista todos os medicamentos | Sim |
| GET | `/buscar?q=texto` | Busca por nome/princípio ativo/categoria | Sim |
| GET | `/interacoes?principioAtivo=x` | Interações (todas ou filtradas) | Sim |
| POST | `/medicamento` | Cadastra novo medicamento (persistente no JSON) | Sim |

**Exemplos com PowerShell (nunca precisa escapar aspas):**

```powershell
# Health (sem chave)
Invoke-RestMethod -Uri http://localhost:3000/health

# Detalhes de um medicamento pelo EAN
Invoke-RestMethod -Uri http://localhost:3000/medicamento/7891000100011 -Headers @{ "x-api-key" = "medapp-demo-2024" }

# Bula
Invoke-RestMethod -Uri http://localhost:3000/medicamento/7891000100011/bula -Headers @{ "x-api-key" = "medapp-demo-2024" }

# Listar tudo
Invoke-RestMethod -Uri http://localhost:3000/medicamentos -Headers @{ "x-api-key" = "medapp-demo-2024" }

# Busca por nome
Invoke-RestMethod -Uri "http://localhost:3000/buscar?q=losartana" -Headers @{ "x-api-key" = "medapp-demo-2024" }

# Interações (filtradas)
Invoke-RestMethod -Uri "http://localhost:3000/interacoes?principioAtivo=Warfarina" -Headers @{ "x-api-key" = "medapp-demo-2024" }

# Cadastro de novo medicamento (POST, persistente)
Invoke-RestMethod -Uri http://localhost:3000/medicamento `
  -Method Post `
  -ContentType "application/json" `
  -Headers @{ "x-api-key" = "medapp-demo-2024" } `
  -Body '{"ean":"7899999999999","nomeProduto":"Vitamina C 1g","razaoSocial":"Demo","principioAtivo":"Acido Ascorbico","categoria":"Suplemento","numeroRegistro":"1.0000.0000.000-0","apresentacao":"Comprimido 1g - 30"}'
```

---

## 7. Medicamentos disponíveis (EANs para testar)

| EAN | Medicamento | Princípio ativo |
|---|---|---|
| 7891000100011 | Losartana Potassica 50mg | Losartana Potassica |
| 7891000200025 | Dipirona Sodica 500mg | Dipirona Sodica |
| 7891000300039 | Omeprazol 20mg | Omeprazol |
| 7891000400043 | Paracetamol 750mg | Paracetamol |
| 7891000500057 | Ibuprofeno 600mg | Ibuprofeno |
| 7891000600061 | Amoxicilina 500mg | Amoxicilina |
| 7891000700075 | Metformina 850mg | Cloridrato de Metformina |
| 7891000800089 | Sinvastatina 20mg | Sinvastatina |
| 7891000900093 | Atenolol 25mg | Atenolol |
| 7891000101017 | Loratadina 10mg | Loratadina |
| 7891000110010 | Captopril 25mg | Captopril |
| 7891000120026 | Prednisona 20mg | Prednisona |
| 7891000130032 | Acido Acetilsalicilico 100mg | Acido Acetilsalicilico |
| 7891000140048 | Clonazepam 2mg | Clonazepam |
| 7891000150054 | Ranitidina 150mg | Cloridrato de Ranitidina |

> **Bulas disponíveis** (só estes 5 EANs têm bula):
>
> - 7891000100011 (Losartana)
> - 7891000200025 (Dipirona)
> - 7891000300039 (Omeprazol)
> - 7891000400043 (Paracetamol)
> - 7891000500057 (Ibuprofeno)

Medicamentos sem bula responderão `404 {"error":"Bula nao disponivel para este medicamento"}` — comportamento esperado.

---

## 8. Testando no app Android (emulador)

### 8.1 Preparos (primeira vez)

1. Inicie a **API** (seção 4).
2. O emulador Android Studio já enxerga o host em `10.0.2.2:3000` (já configurado no `NetworkModule.kt`).
   - **Dispositivo físico**: conecte por USB (ou mesma Wi-Fi) e troque o `BASE_URL` para `http://<IP-da-rede>:3000/` (o `usesCleartextTraffic` já está no `AndroidManifest.xml`).
3. Build e instalação:
   ```powershell
   .\gradlew assembleDebug
   adb install -r app\build\outputs\apk\debug\app-debug.apk
   ```

### 8.2 Roteiro de teste passo a passo (cobrir tudo)

| # | O que testar | Como | Chamada de API esperada |
|---|---|---|---|
| 1 | API de pé | Abrir o app sem erros de rede | — |
| 2 | Busca por nome | Cadastro → digitar `losartana` → card sugerido | `GET /buscar?q=losartana` |
| 3 | Busca por EAN | Cadastro → escanear/digitar `7891000100011` → pré-preenche formulário | `GET /medicamento/7891000100011` |
| 4 | EAN não encontrado + registro | Cadastro → EAN inexistente (ex.: `1111111111111`) → tela "Não encontrado" → salvar | `POST /medicamento` e depois a busca o encontra |
| 5 | Lista popular | Cadastro → cards de "Medicamentos populares" | `GET /medicamentos` |
| 6 | Cadastrar dose com lembrete | Preencher nome/dose/horário → salvar | — |
| 7 | Notificação | Aceitar permissão `POST_NOTIFICATIONS` (1º diálogo) | na hora agendada dispara o `AlarmManager` |
| 8 | Ação "Tomar" na notificação | Tocar → dose vira "Tomado" | — |
| 9 | Ação "Adiar 30 min" | Tocar → dose vira "MaisTarde" e reagenda em 30 min | — |
| 10 | Bula | Home → tocar em Losartana | `GET /medicamento/7891000100011/bula` |
| 11 | Interações | Cadastrar dose de princípio com interação → card colorido | `GET /interacoes?principioAtivo=...` |
| 12 | Histórico | Tela Histórico → ver doses Tomado/MaisTarde/Perdido | — |
| 13 | Reboot do dispositivo | Reiniciar emulador → alarmes ainda disparam | `BootReceiver` religa todos |

---

## 9. Testes automatizados

**API (Vitest):**

```powershell
cd backend
npm test
# → 14 testes passando
```

**Android (unit tests):**

```powershell
.\gradlew testDebugUnitTest
```

> O teste `navigateToHome_sets_tab_zero` é flaky por limpeza de corrotinas — os outros 8 passam.

---

## 10. Dicas e resolução de problemas

| Sintoma | Causa provável | Solução |
|---|---|---|
| `401` na API | `x-api-key` ausente ou errado | usar `medapp-demo-2024` |
| App no emulador "sem internet" | API não está rodando | `cd backend; npm start` e deixar o terminal aberto |
| `10.0.2.2` não responde | API em outra porta | checar `PORT` no `server.js` |
| Notificação não chega | permissão negada no Android 13+ | reemcular app e aceitar o diálogo, ou habilitar em Configurações → Apps → MedApp → Notificações |
| Alarme não dispara após reboot | alarmes são limpos pelo sistema | já resolvido pelo `BootReceiver` |
| `404` na bula | medicamento não tem bula | só os 5 primeiros têm |
| Restaurar `medicamentos.json` aos 15 originais | Muitos POSTs de teste | os testes (`npm test`) resetam o arquivo; ou reconfigure manualmente |
| Dispositivo físico não conecta | `10.0.2.2` só existe no emulador | troque o `BASE_URL` para o IP da máquina |

---

### Notas finais

- O changelog completo em português está em [`../CHANGELOG.md`](../CHANGELOG.md).
- Código do app em `app/src/main/java/com/example/medapp/`:
  - `data/AnvisaApiService.kt` — interface Retrofit (6 chamadas).
  - `data/MedicineRepository.kt` — lógica de negócio (fallback mock + API).
  - `ui/viewmodel/MainViewModel.kt` — estado das telas (StateFlow).
