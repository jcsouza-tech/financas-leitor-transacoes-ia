package br.com.financas.leitor_transacoes_ia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class LeitorTransacoesIaApplication {

    public static void main(String[] args) {
        SpringApplication.run(LeitorTransacoesIaApplication.class, args);
    }

}
