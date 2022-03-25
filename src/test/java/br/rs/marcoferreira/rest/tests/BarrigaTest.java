package br.rs.marcoferreira.rest.tests;

import br.rs.marcoferreira.rest.core.BaseTest;
import br.rs.marcoferreira.rest.utils.DateUtils;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING) //Maneira feia de garantir ordem dos testes mas é o que tem por agora
public class BarrigaTest extends BaseTest { //Extendido BaseTest da classe CORE. Vai colocar todos os atributos estáticos no RestAssured que foram configurados na classe BaseTest

    private String TOKEN;
    private static String CONTA_NAME = "Conta " + System.nanoTime(); //Nome da conta + o tempo atual em nano segundos. Garante que a cada execução o nome da conta será diferente.
    private static Integer CONTA_ID; // ID da nova conta criada
    private static Integer MOV_ID; //ID da nova movimentação

    @Before //Vai instanciar 1x antes de cada 1 dos testes. Vai fazer o login e vai pro método depois
    public void login() {
        Map<String, String> login = new HashMap<>();
        login.put("email", "seu-email-aqui"); //Criar a prória conta em https://seubarriga.wcaquino.me/cadastro
        login.put("senha", "crie-a-sua-conta");

        //Extrair o token
        TOKEN = given()
                .body(login)

                .when()
                .post("/signin")

                .then()
                .statusCode(200) //Resposta de statusCode de OK
                .extract().path("token");
    }

    @Test
    public void t01_naoDeveAcessarAPISemToken() {
        given()

                .when()
                .get("/contas")//Recurso utilizado

                .then()
                .statusCode(401) //Resposta de statusCode de não autorizado
        ;
    }

    @Test
    public void t02_deveIncluirContaComSucesso() {
        Map<String, String> login = new HashMap<>();
        login.put("email", "marco.scariot@gmail.com"); //Criar a prória conta em https://seubarriga.wcaquino.me/cadastro
        login.put("senha", "102030");

        //Extrair o token
        String token = given()
                .body(login)

                .when()
                .post("/signin")

                .then()
                .statusCode(200) //Resposta de statusCode de OK para inserção
                .extract().path("token");

        CONTA_ID = given()
                .header("Authorization", "JWT " + token) //APIs mais recentes seria "bearer" ao invés de JWT
                .body("{\"nome\":\"" + CONTA_NAME + "\"}") //Conta que será incluída

                .when()
                .post("/contas")

                .then()
                .statusCode(201) //Resposta OK de inclusão
                .extract().path("id") //Extrai o ID da nova conta
        ;
    }

    @Test
    public void t03_deveAlterarContaComSucesso() {
        given()
                .header("Authorization", "JWT " + TOKEN) //APIs mais recentes seria "bearer" ao invés de JWT
                .body("{\"nome\":\"" + CONTA_NAME + " alterada\"}") //Novo nome da conta
                .pathParam("id", CONTA_ID) //Cria um parâmetro para saber qual conta alterar

                .when()
                .put("/contas/{id}") //id da conta que se quer alterar passado pelo parâmetro acima

                .then()
                .log().all()
                .statusCode(200) //Resposta OK de alteração
                .body("nome", is("" + CONTA_NAME + " alterada"))
        ;
    }

    @Test
    public void t04_naoDeveIncluirContaComNomeRepetido() {
        given()
                .header("Authorization", "JWT " + TOKEN) //APIs mais recentes seria "bearer" ao invés de JWT
                .body("{\"nome\":\"" + CONTA_NAME + " alterada\"}") //Nome da conta existente que vai procurar

                .when()
                .post("/contas/") //Onde vai ser o post

                .then()
                .statusCode(400) //Resposta NÃO OK de inclusão
                .body("error", is("Já existe uma conta com esse nome!"))
        ;
    }

    @Test
    public void t05_deveInserirMovimentacaoComSucesso() {
        Movimentacao mov = getMovimentacaoValida();

        MOV_ID = given()
                .header("Authorization", "JWT " + TOKEN) //APIs mais recentes seria "bearer" ao invés de JWT
                .body(mov) //Como já está subentendido que o contentType é JSON então esse objeto vai ser convertido para um JSON

                .when()
                .post("/transacoes/") //Onde vai ser o post

                .then()
                .statusCode(201) //Resposta de statusCode de OK para inserção
                .extract().path("id")
        ;
    }

    @Test
    public void t06_deveValidarCamposObrigatoriosMovimentacao() {
        given()
                .header("Authorization", "JWT " + TOKEN) //APIs mais recentes seria "bearer" ao invés de JWT
                .body("{}") //Enviando objeto vazio (todos os valores vinham encapsulados neste objeto)

                .when()
                .post("/transacoes/") //Onde vai ser o post

                .then()
                .statusCode(400) //Resposta de statusCode de NÃO OK para inserção
                .body("$", hasSize(8)) //8 validações de campos que são obrigatórios. É bom colocar a validação de tamanho para que, caso num futuro venham mais, o cenário não deve aceitar.
                .body("msg", hasItems(
                        "Data da Movimentação é obrigatório",
                        "Data do pagamento é obrigatório",
                        "Descrição é obrigatório",
                        "Interessado é obrigatório",
                        "Valor é obrigatório",
                        "Valor deve ser um número",
                        "Conta é obrigatório",
                        "Situação é obrigatório"
                ))
        ;
    }

    @Test
    public void t07_NaoDeveInserirMovimentacaoComDataFutura() {
        Movimentacao mov = getMovimentacaoValida();
//        mov.setData_transacao("13/01/2077"); //Data futura modo antigo
        mov.setData_transacao(DateUtils.getDataDiferencaDias(2)); //Data futura usando o utils e dando um overide no getMovimentacao com uma data futura

        given()
                .header("Authorization", "JWT " + TOKEN) //APIs mais recentes seria "bearer" ao invés de JWT
                .body(mov) //Como já está subentendido que o contentType é JSON então esse objeto vai ser convertido para um JSON

                .when()
                .post("/transacoes/") //Onde vai ser o post

                .then()
                .statusCode(400) //Resposta NÃO OK de inclusão
                .body("$", hasSize(1)) //Deve retornar apenas 1 erro
                .body("msg", hasItem("Data da Movimentação deve ser menor ou igual à data atual")) //Uso do hasItem pois podem haver várias mensagems nesse array que retorna
        ;
    }

    @Test
    public void t08_NaoDeveRemoverContaComMovimentacao() {
        given()
                .header("Authorization", "JWT " + TOKEN) //APIs mais recentes seria "bearer" ao invés de JWT
                .pathParam("id", CONTA_ID)

                .when()
                .delete("/Contas/{id}") //Onde vai ser o delete e a conta a remover

                .then()
                .statusCode(500) //Resposta de sem sucesso
                .body("constraint", is("transacoes_conta_id_foreign")) //Esse foi o retorno do log quando tentei excluir uma conta com movimentação, então vou validar
        ;
    }

    @Test
    public void t09_deveCalcularSaldoContas() {
        given()
                .header("Authorization", "JWT " + TOKEN) //APIs mais recentes seria "bearer" ao invés de JWT

                .when()
                .get("/saldo") //Onde vai ser o get

                .then()
                .statusCode(200)
                .body("find{it.conta_id == " + CONTA_ID + "}.saldo", is("100.00")) //Verifico se na conta em questão temos o valor string em questão
        ;
    }

    @Test
    public void t10_deveRemoverMovimentacao() {
        given()
                .header("Authorization", "JWT " + TOKEN) //APIs mais recentes seria "bearer" ao invés de JWT
                .pathParam("id", MOV_ID)

                .when()
                .delete("/transacoes/{id}") //Onde vai ser o delete e qual ID da transação

                .then()
                .statusCode(204)
        ;
    }

    //Método auxiliar
    private Movimentacao getMovimentacaoValida() {
        Movimentacao mov = new Movimentacao(); //Instanciou a clase Movimentação
        //Serão inseridos todos os valores obrigatórios aqui para a transação
        mov.setConta_id(CONTA_ID);
        mov.setDescricao("Descricao da movimentacao");
        mov.setEnvolvido("Envolvido na movimentacao");
        mov.setTipo("REC"); //Receita
        mov.setData_transacao(DateUtils.getDataDiferencaDias(-1)); //Data como se fosse hoje sempre
        mov.setData_pagamento(DateUtils.getDataDiferencaDias(5));
        mov.setValor(100f); //"f" indica Float
        mov.setStatus(true); //True = conta paga
        return mov; //Vai retonar a movimentação, aqui denominada apenas mov
    }
}
