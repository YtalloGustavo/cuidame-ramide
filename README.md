# MedApp

Aplicativo Android para cadastro e lembretes de medicamentos com busca por código de barras (EAN), bula e interações medicamentosas, consumindo uma API própria em Node.js.

---

# Changelog

Todas as mudanças notáveis do projeto MedApp serão documentadas neste arquivo.

O formato é baseado no [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/),
e este projeto adere ao [Versionamento Semântico](https://semver.org/lang/pt-BR/).

## [1.0.0] - 2026-08-07

### Adicionado

- **Cadastro de medicamentos por código de barras** (CameraX + ML Kit Barcode Scanning)
  - Tela de escaneamento em tela cheia com permissão de câmera
  - Feedback visual com caixa de scan e botão de flash
- **Busca de medicamentos**
  - Por EAN (código de barras) via API
  - Por nome com debounce de 400ms e dropdown de sugestões
  - Lista de medicamentos populares com cards (nome, laboratório, categoria, EAN)
  - Indicador de carregamento durante a busca
- **API própria de medicamentos** (Node.js + Fastify) em `backend/`
  - 15 medicamentos reais com EAN-13 válido em `medicamentos.json`
  - Rotas: `GET /health`, `GET /medicamento/:ean`, `GET /medicamento/:ean/bula`,
    `GET /medicamentos`, `GET /buscar`, `GET /interacoes`, `POST /medicamento`
  - Autenticação por API key (`x-api-key: medapp-demo-2024`)
  - Persistência em arquivo JSON (medicamentos cadastrados pelo app)
  - 5 bulas detalhadas em `bulas.json` e 10 pares de interações em `interacoes.json`
  - Testes automatizados com Vitest (14 testes)
- **Banco de dados local Room** (SQLite)
  - `DoseEntity` e `CaregiverEntity` com DAOs
  - Repositórios migrados de SharedPreferences para Room (suspenso)
  - Metadados do medicamento (EAN, laboratório, princípio ativo) salvos na dose
- **Bula integrada à API**
  - Tela com 3 abas: Resumo, Bula Completa e Interações
  - Lista dinâmica dos medicamentos disponíveis
- **Lembretes e notificações**
  - Agendamento com `AlarmManager` (`ReminderScheduler`)
  - Notificação com ações "Tomar" e "Adiar 30 minutos" (`ReminderReceiver` + `DoseActionReceiver`)
  - Permissão `POST_NOTIFICATIONS` solicitada em runtime no Android 13+
  - Reagendamento de alarmes após reboot do dispositivo (`BootReceiver`)
  - Cancelamento do alarme ao confirmar, adiar, marcar como perdida, editar ou excluir a dose
- **Aviso de interações medicamentosas**
  - Consulta de interações por princípio ativo ao cadastrar nova dose
  - Card colorido: âmbar (interação moderada) e vermelho (grave)
- **Fluxo de cadastro de medicamento não encontrado**
  - Estado "Não encontrado" com opção de cadastro manual
  - Registro automático na API em background
- **Testes unitários Android** (JUnit + MockK + Robolectric + Room in-memory)
  - `MainViewModelTest` com 9 testes cobrindo busca, cadastro e navegação

### Alterado

- Tela inicial com tip card "Como cadastrar" no lugar da lista de EANs de teste
- Busca de medicamento reformulada com seção "Resultados" e "Medicamentos populares"
- Botão de limpar busca (✕) no campo de pesquisa

### Corrigido

- Incompatibilidade Room 2.6.1 com Kotlin 2.2.10/KSP — migrado para Room 2.7.0
- Notificações eram descartadas silenciosamente no Android 13+ (faltava permissão runtime)
- Alarmes perdidos após reinicialização do dispositivo
- Notificações continuavam disparando para doses tomadas/adiadas/excluídas

### Dependências principais

- Android Gradle Plugin 9.1.1, Kotlin 2.2.10, KSP, Compose BOM 2026.02.01
- Hilt 2.60.1 (DI), Room 2.7.0, CameraX 1.4.2, ML Kit 17.3.0
- Retrofit 2.11.0, OkHttp 4.12.0, lifecycle-runtime-compose 2.10.0
- Backend: Node.js + Fastify + Vitest

---

## Como rodar a API localmente

Veja o guia completo em [`backend/README.md`](backend/README.md) — rotas, EANs para teste, exemplos com PowerShell e roteiro de testes no emulador.

[1.0.0]: https://github.com/YtalloGustavo/MedApp/releases/tag/v1.0.0