package br.rs.marcoferreira.rest.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {

    public static String getDataDiferencaDias(Integer qtdDias){
        Calendar cal = Calendar.getInstance(); //Retorna a instância do Calendar representando a data atual
        cal.add(Calendar.DAY_OF_MONTH,qtdDias);
        return getDataFormatada(cal.getTime());
    }

    public static String getDataFormatada(Date data) {
        DateFormat format = new SimpleDateFormat(("dd/MM/yyyy"));
        return format.format(data); //Retorna a data formatada
    }
}
