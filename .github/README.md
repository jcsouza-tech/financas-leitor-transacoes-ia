# Finanças - Leitor de Transações IA

Microserviço para leitura inteligente de transações financeiras usando IA, com autenticação AWS Cognito e multi-tenancy.

## 🚀 Funcionalidades

- **Leitura de Extratos**: PDF, CSV e outros formatos
- **Classificação IA**: Categorização automática de transações
- **Multi-tenancy**: Isolamento de dados por usuário
- **Autenticação**: AWS Cognito com JWT
- **API Gateway**: Roteamento e autorização
- **Processamento Assíncrono**: SQS para processamento em background

## 🏗️ Arquitetura

```
Cliente (React/Mobile)
    ↓
AWS Cognito (Autenticação)
    ↓
API Gateway (Authorizer Cognito)
    ↓
Backend (leitor-transacoes-ia)
    ↓
MySQL (multi-tenancy por userId)
```

## 🛠️ Tecnologias

- **Backend**: Spring Boot 3.2, Java 17
- **Banco**: MySQL 8.0 + Flyway Migrations
- **IA**: Google Gemini ✅, OpenAI ⚠️, Claude ⚠️
- **Autenticação**: AWS Cognito + JWT
- **Infraestrutura**: Terraform + AWS
- **Processamento**: AWS SQS
- **Monitoramento**: Prometheus + Grafana

## 🚀 Início Rápido

### Pré-requisitos

- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- AWS CLI (para produção)
- Terraform (para infraestrutura)

### 1. Desenvolvimento Local

```bash
# Clonar repositório
git clone <repository-url>
cd financas-leitor-transacoes-ia

# Iniciar dependências
docker-compose up -d

# Migrations são executadas automaticamente via Flyway
# Não é mais necessário executar run-migration.ps1

# Iniciar aplicação
mvn spring-boot:run
```

### 2. Testar Desenvolvimento Local

```bash
# Testar sem autenticação (modo desenvolvimento)
./scripts/test-local-dev.sh

# Acessar Swagger UI
open http://localhost:8080/swagger-ui/index.html
```

### 3. Deploy em Produção

```bash
# Deploy da infraestrutura AWS
cd terraform
terraform init
terraform apply

# Configurar variáveis de ambiente
export SECURITY_ENABLED=true
export AWS_COGNITO_USER_POOL_ID="<user_pool_id>"
export AWS_COGNITO_CLIENT_ID="<client_id>"
export AWS_REGION="us-east-1"
export JWT_ISSUER_URI="<issuer_uri>"
export JWT_JWK_SET_URI="<jwk_set_uri>"

# Iniciar aplicação em produção
java -jar target/leitor-transacoes-ia-1.0.0.jar --spring.profiles.active=prod
```

## 🔐 Autenticação e Segurança

### Modo Desenvolvimento (Local)

Por padrão, a autenticação está **desabilitada** para desenvolvimento:

```yaml
security:
  enabled: false  # Desenvolvimento local
```

- Usuário mock: `local-user`
- Sem necessidade de token JWT
- Todos os endpoints acessíveis

### Modo Produção (AWS Cognito)

Para produção, configure as variáveis de ambiente:

```bash
export SECURITY_ENABLED=true
export AWS_COGNITO_USER_POOL_ID="us-east-1_XXXXXXXXX"
export AWS_COGNITO_CLIENT_ID="XXXXXXXXXXXXXXXXXXXXXXXXXX"
export AWS_REGION="us-east-1"
export JWT_ISSUER_URI="https://cognito-idp.us-east-1.amazonaws.com/us-east-1_XXXXXXXXX"
export JWT_JWK_SET_URI="https://cognito-idp.us-east-1.amazonaws.com/us-east-1_XXXXXXXXX/.well-known/jwks.json"
```

### 🛡️ Segurança de API Keys

**⚠️ IMPORTANTE**: Nunca commite chaves de API no código! 

- Use variáveis de ambiente
- Configure `.env` localmente (não commite)
- Use AWS Secrets Manager em produção
- Veja [Guia de Segurança](SECURITY.md) para mais detalhes

## 🤖 AI Providers

### Status de Implementação

| Provider | Status | Pronto para Produção |
|----------|--------|---------------------|
| **Google Gemini** | ✅ Implementado | Sim |
| **OpenAI** | ⚠️ Parcial | Não |
| **Claude** | ⚠️ Parcial | Não |
| **Placeholder** | ✅ Implementado | Apenas Dev |

### Google Gemini (Recomendado)

**✅ Implementado e pronto para uso**

```bash
# Configuração
AI_PROVIDER=gemini
AI_API_KEY=your-gemini-api-key
GEMINI_MODEL=gemini-1.5-flash
```

**Como funciona:**
- Classificação automática de transações
- Categorização inteligente
- Extração de informações
- Gratuito para desenvolvimento

### OpenAI (Não Implementado)

**⚠️ Estrutura criada, mas não testada**

Para implementar:
1. Adicionar credenciais válidas da OpenAI
2. Testar com dados reais
3. Validar custos
4. Configurar rate limiting

```bash
# Estrutura pronta
AI_PROVIDER=openai
AI_API_KEY=sk-your-openai-key
OPENAI_MODEL=gpt-4
```

### Claude (Não Implementado)

**⚠️ Estrutura criada, mas não testada**

Para implementar:
1. Adicionar credenciais válidas da Anthropic
2. Testar com dados reais
3. Validar custos
4. Configurar rate limiting

```bash
# Estrutura pronta
AI_PROVIDER=claude
AI_API_KEY=sk-ant-your-claude-key
CLAUDE_MODEL=claude-3-sonnet-20240229
```

### Placeholder (Desenvolvimento)

**✅ Implementado para testes**

Uso apenas em desenvolvimento, retorna dados de exemplo:

```bash
AI_PROVIDER=placeholder
```

## 📡 API Endpoints

### Endpoints Públicos (sem autenticação)
- `GET /actuator/health` - Health check
- `GET /swagger-ui/**` - Documentação Swagger
- `GET /v3/api-docs/**` - OpenAPI spec

### Endpoints Protegidos (com autenticação)
- `GET /api/v1/leitor/transacoes` - Listar transações
- `GET /api/v1/leitor/processamentos` - Listar processamentos
- `POST /api/v1/leitor/processar` - Processar documento
- `GET /api/v1/leitor/processamentos/{id}` - Status do processamento

## 🧪 Testes

### Teste Local

```bash
# Testar endpoints sem autenticação
curl http://localhost:8080/api/v1/leitor/transacoes
```

### Teste com Cognito

```bash
# Criar usuários de teste
./scripts/create-test-users.sh <USER_POOL_ID> <CLIENT_ID>

# Testar autenticação JWT
./scripts/test-jwt-auth.sh <USER_POOL_ID> <CLIENT_ID> <API_GATEWAY_URL>
```

## 🏗️ Infraestrutura

### Terraform

A infraestrutura AWS é gerenciada via Terraform:

```bash
cd terraform

# Deploy
terraform init
terraform apply

# Outputs importantes
terraform output cognito_user_pool_id
terraform output api_gateway_url
```

### Recursos AWS

- **Cognito User Pool**: Autenticação de usuários
- **API Gateway**: Roteamento e autorização
- **CloudWatch Logs**: Monitoramento
- **SQS**: Processamento assíncrono

## 📊 Monitoramento

### Métricas

- Prometheus: `http://localhost:8080/actuator/prometheus`
- Health: `http://localhost:8080/actuator/health`
- Swagger: `http://localhost:8080/swagger-ui/index.html`

### Logs

```bash
# Logs da aplicação
tail -f logs/application.log

# Logs de autenticação
grep "UserContext\|JWT\|Authentication" logs/application.log
```

## 🔧 Configuração

### Variáveis de Ambiente

| Variável | Descrição | Padrão |
|----------|-----------|--------|
| `SECURITY_ENABLED` | Habilitar autenticação | `false` |
| `AWS_COGNITO_USER_POOL_ID` | ID do User Pool | - |
| `AWS_COGNITO_CLIENT_ID` | ID do Client | - |
| `AWS_REGION` | Região AWS | `us-east-1` |
| `JWT_ISSUER_URI` | URI do issuer JWT | - |
| `JWT_JWK_SET_URI` | URI do JWK Set | - |

### Perfis Spring

- **dev**: Desenvolvimento local (sem autenticação)
- **prod**: Produção AWS (com autenticação)

## 🚨 Troubleshooting

### Problemas Comuns

#### 1. Aplicação não inicia
```bash
# Verificar se o banco está rodando
docker-compose ps

# Verificar logs
docker-compose logs mysql_db
```

#### 2. Erro de autenticação
```bash
# Verificar variáveis de ambiente
echo $SECURITY_ENABLED
echo $AWS_COGNITO_USER_POOL_ID

# Testar token JWT
./scripts/test-jwt-auth.sh
```

#### 3. CORS Errors
```bash
# Verificar configuração CORS
curl -H "Origin: http://localhost:3000" \
     -H "Access-Control-Request-Method: GET" \
     -X OPTIONS \
     "http://localhost:8080/api/v1/leitor/transacoes"
```

## 📚 Documentação Completa

### 🚀 Deploy e Infraestrutura
- [AWS Setup Guide](AWS-SETUP-GUIDE.md) - Configuração completa da AWS
- [Deploy Guide](DEPLOY-AWS.md) - Deploy em ECS Fargate
- [Cognito + API Gateway](COGNITO-API-GATEWAY-SETUP.md) - Autenticação e Gateway
- [Database Migrations](MIGRATIONS.md) - Migrations com Flyway
- [Terraform](terraform/README.md) - Infraestrutura como código

### 🏗️ Arquitetura e Segurança
- [Arquitetura](ARQUITETURA.md) - Diagramas e fluxos
- [Segurança](SECURITY.md) - Melhores práticas de segurança

## 🤝 Contribuição

1. Fork o projeto
2. Crie uma branch (`git checkout -b feature/nova-funcionalidade`)
3. Commit suas mudanças (`git commit -am 'Adiciona nova funcionalidade'`)
4. Push para a branch (`git push origin feature/nova-funcionalidade`)
5. Abra um Pull Request

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para detalhes.

## 🆘 Suporte

Para dúvidas ou problemas:

1. Verifique a documentação
2. Consulte os logs da aplicação
3. Teste com os scripts fornecidos
4. Abra uma issue no GitHub

---

**Desenvolvido com ❤️ para gestão financeira inteligente**
