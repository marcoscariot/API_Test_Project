package br.rs.marcoferreira.rest.core;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import org.hamcrest.Matchers;
import org.junit.BeforeClass;

public class BaseTest implements Constantes{ //Herda atributos da interface "constantes"

    @BeforeClass //Executa 1x antes da classe ser instanciada
    public static void setup(){
        RestAssured.baseURI = APP_BASE_URL;
        RestAssured.port = APP_PORT;
        RestAssured.basePath = APP_BASE_PATH;

        //Criado requisição builder como JSON
        RequestSpecBuilder reqBuilder = new RequestSpecBuilder();
        reqBuilder.setContentType(APP_CONTENT_TYPE);
        RestAssured.requestSpecification = reqBuilder.build();

        //Criado response builder e expectativa do tempo de resposta
        ResponseSpecBuilder resBuilder = new ResponseSpecBuilder();
        resBuilder.expectResponseTime(Matchers.lessThan(MAX_TIMEOUT));
        RestAssured.responseSpecification = resBuilder.build();

        //Habilita o log da requisição e da response APENAS se tiver problemas no teste
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
}
