package com.conversion;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;

@RestController
public class ConversionServiceController {

    @Autowired
    private ReadValueConfig readValueConfig;

    @Autowired
    private CurrencyExchangeFeign feign;

    @GetMapping(value = "readProperty")
    public ResponseEntity readProperty() {
        ReadValueConfigDto dto = new ReadValueConfigDto();
        dto.setKey1(readValueConfig.getKey1());
        dto.setKey2(readValueConfig.getKey2());
        return new ResponseEntity(dto, HttpStatus.OK);
    }


    @GetMapping("/currency-conversion/from/{from}/to/{to}/quantity/{quantity}")
    public CurrencyConversion calculateCurrencyConversion(@PathVariable String from,
                                                          @PathVariable String to,
                                                          @PathVariable BigDecimal quantity) {
        HashMap<String, String> uriVariables = new HashMap<>();
        uriVariables.put("from", from);
        uriVariables.put("to", to);
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8000/currency-exchange/from/{from}/to/{to}";
        ResponseEntity<CurrencyExchange> responseEntity =
                restTemplate.getForEntity(url, CurrencyExchange.class, uriVariables);
        CurrencyExchange currencyExchange = responseEntity.getBody();
        return new CurrencyConversion(currencyExchange.getId(),
                from, to, quantity,
                currencyExchange.getConversionMultiple(),
                quantity.multiply(currencyExchange.getConversionMultiple()),
                currencyExchange.getEnvironment() + " " + "rest template");
    }




    @GetMapping("/currency-conversion-feign/from/{from}/to/{to}/quantity/{quantity}")
    public CurrencyConversion calculateCurrencyConversionF(@PathVariable String from,
                                                           @PathVariable String to,
                                                           @PathVariable BigDecimal quantity) {

        CurrencyExchange exchange = feign.retrieveExchangeValue(from, to);
        return new CurrencyConversion(exchange.getId(),
                from, to, quantity,
                exchange.getConversionMultiple(),
                quantity.multiply(exchange.getConversionMultiple()),
                exchange.getEnvironment() + " " + "rest template");
    }


}
