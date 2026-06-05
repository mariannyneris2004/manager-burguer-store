# Manager - Sistema de Gestão para Hamburgueria

Sistema web desenvolvido em **Java 17** com **Spring Boot**, **Thymeleaf** e **MySQL**, voltado para gestão de uma hamburgueria.  
O projeto contempla cadastros, compras, estoque, pedidos e relatórios financeiros.

## Funcionalidades

- Cadastro e manutenção de **produtos**
- Cadastro e manutenção de **ingredientes**
- Cadastro e manutenção de **adicionais**
- Vinculação de ingredientes e adicionais aos produtos
- Registro de **compras de ingredientes**
- Controle de **estoque** e movimentações
- Cadastro de **despesas gerais**
- Lançamento e acompanhamento de **pedidos**
- **Pedidos por item**, com:
  - produto + quantidade
  - adicionais por item
  - retirada de ingredientes por item
  - histórico do pedido com snapshot de nome, preço e custo no momento da venda
- Cálculo de:
  - valor total
  - custo total
  - lucro bruto
- Relatório financeiro

## Regra principal dos pedidos

Cada item do pedido é tratado de forma independente. Isso permite que o mesmo produto apareça mais de uma vez no mesmo pedido com configurações diferentes.

Exemplo:

- 1 Bitokas com adicional de salada
- 1 Bitokas sem bacon e com adicional de maionese

As retiradas:

- reduzem o custo do item
- baixam o estoque apenas dos ingredientes efetivamente consumidos
- não alteram o cadastro original do produto

## Tecnologias

- Java 17
- Spring Boot
- Spring MVC
- Spring Data JPA
- Thymeleaf
- MySQL
- Flyway
- Maven
- Lombok

## Requisitos

- Java 17 ou superior
- Maven 3.9+ (ou uso do Maven Wrapper)
- MySQL 8+
- Banco de dados configurado para a aplicação

## Configuração do banco

A configuração padrão está em `src/main/resources/application.properties`.

As migrations do Flyway estão em:

```text
src/main/resources/db/migration
```

## Principais rotas

- `/home` — página inicial
- `/produtos` — cadastro de produtos
- `/ingredientes` — cadastro de ingredientes
- `/adicionais` — cadastro de adicionais
- `/compras` — compras de ingredientes
- `/estoque` — visão de estoque
- `/despesas` — despesas gerais
- `/pedidos` — pedidos
- `/relatorios/financeiro` — relatório financeiro

## Estrutura do projeto

```text
src/main/java/com/bitokas/manager
├── controller
├── dto
├── model
├── service
└── ManagerApplication.java

src/main/resources
├── application.properties
├── db/migration
└── templates
```

## Observações

- O projeto utiliza **Flyway** para versionamento do banco.
- Os templates são renderizados com **Thymeleaf**.
- O sistema foi ajustado para trabalhar com pedidos itemizados, permitindo adicionais e retiradas por produto.

## Licença

Projeto interno / uso específico do sistema.
