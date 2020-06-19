package io.amal.coronatracker.services;

import io.amal.coronatracker.models.LocationStats;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;


@Service
public class CoronaVirusDataService {

    //Data fetch URL
    private static String VIRUS_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";

    private List<LocationStats> allStats= new ArrayList<>();

    public List<LocationStats> getAllStats() {
        return allStats;
    }



    @PostConstruct
    @Scheduled (cron = "* * 1 * * *")
    public void fetchVirusData() throws IOException, InterruptedException {

        List<LocationStats> newStats = new ArrayList<>();
        //Create new http client
        HttpClient client = HttpClient.newHttpClient();
        //Create new http request using builder pattern
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(VIRUS_DATA_URL))
                .build();
        //Send request and return body as a string in the response
        HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());

        //Reader for String body from response
        StringReader csvBodyReader = new StringReader(httpResponse.body());

        //Loop through String bidy using reader and auto detect Headers
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader);
        for (CSVRecord record : records) {
            LocationStats newStat = new LocationStats();
            newStat.setState(record.get("Province/State"));
            newStat.setCountry(record.get("Country/Region"));
            newStat.setLatestTotalCases(Integer.parseInt(record.get(record.size()-1)));
            System.out.println(newStat);
            newStats.add(newStat);

        }

        this.allStats = newStats;


    }

}
